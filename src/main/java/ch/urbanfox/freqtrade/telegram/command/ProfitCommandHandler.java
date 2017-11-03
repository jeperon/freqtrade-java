package ch.urbanfox.freqtrade.telegram.command;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.apache.commons.lang3.tuple.Pair;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import ch.urbanfox.freqtrade.exchange.FreqTradeExchangeService;
import ch.urbanfox.freqtrade.telegram.TelegramService;
import ch.urbanfox.freqtrade.trade.TradeEntity;
import ch.urbanfox.freqtrade.trade.TradeService;

/**
 * Handler for /profit
 */
@Component
public class ProfitCommandHandler extends AbstractCommandHandler {

    @Autowired
    private FreqTradeExchangeService exchangeService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private TelegramService telegramService;

    @Override
    public String getCommandName() {
        return "/profit";
    }

    /**
     * Returns a cumulative profit statistics
     *
     * @throws IOException if any error occurs while communicating with the exchange
     * @throws TelegramApiException if any error occur while using the Telegram API
     */
    @Override
    protected void handleInternal(String[] params) throws Exception {
        List<TradeEntity> trades = tradeService.findAllOrderById();

        List<BigDecimal> profitAmounts = new ArrayList<>();
        List<BigDecimal> profits = new ArrayList<>();
        LongStream.Builder durations = LongStream.builder();
        for (TradeEntity trade : trades) {
            if (Objects.nonNull(trade.getCloseDate())) {
                durations.add(trade.getOpenDate().until(trade.getCloseDate(), ChronoUnit.SECONDS));
            }

            final BigDecimal profit;
            if (Objects.nonNull(trade.getCloseProfit())) {
                profit = trade.getCloseProfit();
            } else {
                // Get current rate
                BigDecimal currentRate = exchangeService.getTicker(new CurrencyPair(trade.getPair())).getBid();
                profit = currentRate.subtract(trade.getOpenRate()).divide(trade.getOpenRate())
                        .multiply(BigDecimal.valueOf(100));
            }

            profitAmounts.add(profit.divide(BigDecimal.valueOf(100)).multiply(trade.getStakeAmount()));
            profits.add(profit);
        }

        Map<CurrencyPair, BigDecimal> pairRates = trades.stream()
                .collect(Collectors.toMap(t -> new CurrencyPair(t.getPair()),
                        TradeEntity::getCloseProfit,
                        (leftProfit, rightProfit) -> leftProfit.add(rightProfit)));

        Optional<Pair<CurrencyPair, BigDecimal>> bestPair = pairRates.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(Pair::getRight))
                .sorted(Comparator.reverseOrder())
                .findFirst();

        if (!bestPair.isPresent()) {
            telegramService.sendMessage("*Status:* `no closed trade`");
            return;
        }

        CurrencyPair bpPair = bestPair.get().getLeft();
        BigDecimal bpRate = bestPair.get().getRight();
        StringBuilder markdownMessage = new StringBuilder();
        BigDecimal profitBtc = profitAmounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .round(new MathContext(8));
        BigDecimal profit = profits.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .round(new MathContext(2));
        markdownMessage.append("*ROI:* `").append(profitBtc).append(" (").append(profit).append("%)`\n");
        markdownMessage.append("*Trade Count:* `").append(trades.size()).append("`\n");
        LocalDateTime firstTradeDate = trades.get(0).getOpenDate();
        markdownMessage.append("*First Trade opened:* `").append(firstTradeDate).append("`\n");
        LocalDateTime latestTradeDate = trades.get(trades.size() - 1).getOpenDate();
        markdownMessage.append("*Latest Trade opened:* `").append(latestTradeDate).append("`\n");
        long avgDuration = Math.round(durations.build().average().getAsDouble());
        markdownMessage.append("*Avg. Duration:* `").append(avgDuration).append("`\n");
        String bestRate = bpRate.round(new MathContext(2)).toString();
        markdownMessage.append("*Best Performing:* `").append(bpPair).append(": ").append(bestRate).append("%`");

        telegramService.sendMessage(markdownMessage.toString());
    }

}
