package ch.urbanfox.freqtrade.telegram.command;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import ch.urbanfox.freqtrade.FreqTradeMainRunner;
import ch.urbanfox.freqtrade.telegram.TelegramService;
import ch.urbanfox.freqtrade.trade.TradeEntity;
import ch.urbanfox.freqtrade.trade.TradeService;
import ch.urbanfox.freqtrade.type.State;

/**
 * Handler for /performance
 */
@Component
public class PerformanceCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceCommandHandler.class);

    @Autowired
    private FreqTradeMainRunner runner;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private TelegramService telegramService;

    @Override
    public String getCommandName() {
        return "/performance";
    }

    /**
     * Shows a performance statistic from finished trades
     *
     * @throws TelegramApiException if any error occur while using Telegram API
     */
    @Override
    protected void handleInternal(String[] params) throws Exception {

        if (runner.getState() != State.RUNNING) {
            telegramService.sendMessage("`trader is not running`");
            return;
        }

        List<TradeEntity> trades = tradeService.findAllClosedTrade();

        Map<CurrencyPair, BigDecimal> pairRates = trades.stream()
                .collect(Collectors.toMap(t -> new CurrencyPair(t.getPair()),
                        TradeEntity::getCloseProfit,
                        (leftProfit, rightProfit) -> leftProfit.add(rightProfit)));

        List<Pair<CurrencyPair, BigDecimal>> pairRatesList = pairRates.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(Pair::getRight))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        StringBuilder messageBuilder = new StringBuilder("<b>Performance:</b>\n");
        for (int i = 0; i < pairRatesList.size(); i++) {
            Pair<CurrencyPair, BigDecimal> pairProfit = pairRatesList.get(i);
            messageBuilder.append(i + 1).append('.');
            messageBuilder.append("<code>").append(pairProfit.getLeft()).append('\t');
            messageBuilder.append(pairProfit.getRight().round(new MathContext(2)).toString()).append("%</code>\n");
        }
        messageBuilder.append("\n");

        String message = messageBuilder.toString();
        LOGGER.debug(message);
        telegramService.sendMessage(message, ParseMode.HTML);
    }

}
