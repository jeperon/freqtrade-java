package ch.urbanfox.freqtrade.telegram.command;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import ch.urbanfox.freqtrade.FreqTradeMainRunner;
import ch.urbanfox.freqtrade.exchange.FreqTradeExchangeService;
import ch.urbanfox.freqtrade.telegram.TelegramService;
import ch.urbanfox.freqtrade.trade.TradeEntity;
import ch.urbanfox.freqtrade.trade.TradeService;
import ch.urbanfox.freqtrade.type.State;

/**
 * Handler for /status
 */
@Component
public class StatusCommandHandler extends AbstractCommandHandler {

    @Autowired
    private FreqTradeMainRunner runner;

    @Autowired
    private FreqTradeExchangeService exchangeService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private TelegramService telegramService;

    @Override
    public String getCommandName() {
        return "/status";
    }

    /**
     * Returns the current status
     *
     * @throws IOException if any error occurs while contacting the exchange
     * @throws TelegramApiException if any error occurs while using the Telegram API
     */
    @Override
    protected void handleInternal(String[] params) throws Exception {
        // Fetch open trade
        List<TradeEntity> trades = tradeService.findAllOpenTrade();
        if (runner.getState() != State.RUNNING) {
            telegramService.sendMessage("*Status:* `trader is not running`");
        } else if (trades.isEmpty()) {
            telegramService.sendMessage("*Status:* `no active order`");
        } else {
            for (TradeEntity trade : trades) {
                // calculate profit and send message to user
                BigDecimal currentRate = exchangeService.getTicker(new CurrencyPair(trade.getPair())).getBid();
                BigDecimal currentProfit = currentRate.subtract(trade.getOpenRate())
                        .divide(trade.getOpenRate())
                        .multiply(new BigDecimal(100));
                List<LimitOrder> orders = exchangeService.getOpenOrders(new CurrencyPair(trade.getPair()));
                Optional<LimitOrder> order = orders.stream()
                        .filter(o -> Objects.equals(o.getId(), trade.getOpenOrderId()))
                        .findFirst();

                String fmtCloseProfit = null;
                if (Objects.nonNull(trade.getCloseProfit())) {
                    fmtCloseProfit = trade.getCloseProfit().round(new MathContext(2)).toString();
                    fmtCloseProfit += "%";
                }

                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append("*Trade ID:* `").append(trade.getId()).append("`\n");
                messageBuilder.append("*Current Pair:* [").append(trade.getPair()).append("](")
                        .append(exchangeService.getPairDetailUrl(trade.getPair())).append(")\n");
                messageBuilder.append("*Open Since:* `").append(trade.getOpenDate()).append("`\n");
                messageBuilder.append("*Amount:* `").append(trade.getAmount().round(new MathContext(8))).append("`\n");
                messageBuilder.append("*Open Rate:* `").append(trade.getOpenRate()).append("`\n");
                messageBuilder.append("*Close Rate:* `").append(trade.getCloseRate()).append("`\n");
                messageBuilder.append("*Current Rate:* `").append(currentRate).append("`\n");
                messageBuilder.append("*Close Profit:* `").append(fmtCloseProfit).append("`\n");
                messageBuilder.append("*Current Profit:* `").append(currentProfit.round(new MathContext(2))).append("%`\n");

                String openOrder = order.map(o -> String.format("%s (%s)", o.getRemainingAmount(), o.getType())).orElse(null);
                messageBuilder.append("*Open Order:* `").append(openOrder).append("`\n");

                telegramService.sendMessage(messageBuilder.toString());
            }
        }
    }

}
