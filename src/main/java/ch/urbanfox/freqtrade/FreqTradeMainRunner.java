package ch.urbanfox.freqtrade;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.knowm.xchange.bittrex.dto.marketdata.BittrexChartData;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import ch.urbanfox.freqtrade.analyze.AnalyzeService;
import ch.urbanfox.freqtrade.exchange.FreqTradeExchangeService;
import ch.urbanfox.freqtrade.exchange.converter.BittrexDataConverter;
import ch.urbanfox.freqtrade.telegram.TelegramService;
import ch.urbanfox.freqtrade.trade.TradeEntity;
import ch.urbanfox.freqtrade.trade.TradeService;
import ch.urbanfox.freqtrade.type.State;
import eu.verdelhan.ta4j.TimeSeries;

@Component
public class FreqTradeMainRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(FreqTradeMainRunner.class);

    private State state = State.RUNNING;

    @Autowired
    private FreqTradeProperties properties;

    @Autowired
    private AnalyzeService analyzeService;

    @Autowired
    private FreqTradeExchangeService exchangeService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private TelegramService telegramService;

    /**
     * Queries the persistence layer for open trades and handles them, otherwise a new trade is created.
     */
    private void process() {
        try {
            // Query trades from persistence layer
            List<TradeEntity> trades = tradeService.findAllOpenTrade();
            if (trades.size() < properties.getMaxOpenTrades()) {
                // Create entity and execute trade
                Optional<TradeEntity> trade = createTrade(properties.getStakeAmount(), "bittrex");
                if (trade.isPresent()) {
                    tradeService.save(trade.get());
                } else {
                    LOGGER.info("Got no buy signal...");
                }
            }

            for (TradeEntity trade : trades) {
                // Check if there is already an open order for this trade
                List<LimitOrder> orders = exchangeService.getOpenOrders(new CurrencyPair(trade.getPair()));
                orders = orders.stream()
                        .filter(o -> Objects.equals(o.getId(), trade.getOpenOrderId()))
                        .collect(Collectors.toList());
                if (!orders.isEmpty()) {
                    LOGGER.info("There is an open order for: {}", orders.get(0));
                } else {
                    // Update state
                    trade.setOpenOrderId(null);
                    tradeService.save(trade);

                    // Check if this trade can be closed
                    if (!closeTradeIfFulfilled(trade)) {
                        // Check if we can sell our current pair
                        handleTrade(trade);
                    }
                }
            }
        } catch (IOException | TelegramApiException e) {
            LOGGER.warn("Error in process", e);
        }
    }

    /**
     * Checks if the trade is closable, and if so it is being closed.
     *
     * @param trade Trade
     * @return true if trade has been closed else False
     */
    private boolean closeTradeIfFulfilled(TradeEntity trade) {
        // If we don't have an open order and the close rate is already set,
        // we can close this trade.
        if (Objects.nonNull(trade.getCloseProfit())
                && Objects.nonNull(trade.getCloseDate())
                && Objects.nonNull(trade.getCloseRate())
                && Objects.isNull(trade.getOpenOrderId())) {
            trade.setOpen(false);
            tradeService.save(trade);
            LOGGER.info("No open orders found and trade is fulfilled. Marking {} as closed ...", trade);
            return true;
        }
        return false;
    }

    /**
     * Executes a sell for the given trade and current rate
     *
     * @param trade Trade instance
     * @param currentRate current rate
     *
     * @throws IOException if any I/O error occurs while contacting the exchange
     * @throws TelegramApiException if any error occur while using the Telegram API
     */
    private void executeSell(TradeEntity trade, BigDecimal currentRate) throws IOException, TelegramApiException {
        // Get available balance
        String currency = trade.getPair().split("/")[1];
        BigDecimal balance = exchangeService.getBalance(Currency.getInstance(currency));
        List<CurrencyPair> whitelist = properties.getPairWhitelist();

        BigDecimal profit = tradeService.executeSellOrder(trade, currentRate, balance);
        whitelist.add(new CurrencyPair(trade.getPair()));
        String message = String.format("*%s:* Selling [%s](%s) at rate `%s (profit: %s%%)`",
                trade.getExchange(),
                trade.getPair(),
                exchangeService.getPairDetailUrl(trade.getPair()),
                trade.getCloseRate(),
                profit.round(new MathContext(2)));
        LOGGER.info(message);
        telegramService.sendMessage(message);
    }

    /**
     * Based an earlier trade and current price and configuration, decides whether bot should sell
     *
     * @return true if bot should sell at current rate
     */
    private boolean shouldSell(TradeEntity trade, BigDecimal currentRate, LocalDateTime currentTime) {
        BigDecimal currentProfit = currentRate.subtract(trade.getOpenRate()).divide(trade.getOpenRate());

        if (Objects.nonNull(properties.getStopLoss()) && currentProfit.compareTo(properties.getStopLoss()) < 0) {
            LOGGER.debug("Stop loss hit.");
            return true;
        }

        Set<Map.Entry<Long, BigDecimal>> minimalRois = properties.getMinimalRoi().entrySet();
        List<Pair<Long, BigDecimal>> sortedMinimalRois = minimalRois.stream()
                .map(e -> Pair.of(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(Pair::getLeft))
                .collect(Collectors.toList());
        for (Pair<Long, BigDecimal> minimalRoi : sortedMinimalRois) {
            Long duration = minimalRoi.getLeft();
            BigDecimal threshold = minimalRoi.getRight();
            // Check if time matches and current rate is above threshold
            Long timeDiff = trade.getOpenDate().until(currentTime, ChronoUnit.SECONDS);
            if (timeDiff > duration && currentProfit.compareTo(threshold) > 0) {
                return true;
            }
        }

        LOGGER.debug("Threshold not reached. (cur_profit: {}%)", currentProfit.multiply(BigDecimal.valueOf(100)));
        return false;
    }

    /**
     * Sells the current pair if the threshold is reached and updates the trade record.
     *
     * @throws IOException if any I/O error occurs while contacting the exchange
     * @throws TelegramApiException if any error occurs while using Telegram API
     */
    private void handleTrade(TradeEntity trade) throws IOException, TelegramApiException {
        if (!trade.getOpen()) {
            LOGGER.warn("attempt to handle closed trade: {}", trade);
            return;
        }

        LOGGER.debug("Handling open trade {} ...", trade);

        BigDecimal currentRate = exchangeService.getTicker(new CurrencyPair(trade.getPair())).getBid();
        if (shouldSell(trade, currentRate, LocalDateTime.now())) {
            executeSell(trade, currentRate);
            return;
        }
    }

    /**
     * Calculates bid target between current ask price and last price
     */
    private BigDecimal getTargetBid(Ticker ticker) {
        if (ticker.getAsk().compareTo(ticker.getLast()) < 0) {
            return ticker.getAsk();
        }

        // Factor with regards to ask price
        BigDecimal balance = BigDecimal.ZERO;
        return ticker.getAsk().add(balance.multiply(ticker.getLast().subtract(ticker.getAsk())));
    }

    /**
     * Checks the implemented trading indicator(s) for a randomly picked pair, if one pair triggers the buy_signal a new trade record gets created
     *
     * @param stakeAmount amount of btc to spend
     * @param exchange exchange to use
     *
     * @throws IOException if any I/O error occurs while contacting the exchange
     * @throws TelegramApiException if any error occurs while using Telegram API
     */
    private Optional<TradeEntity> createTrade(BigDecimal stakeAmount, String exchange) throws IOException, TelegramApiException {
        LOGGER.info("Creating new trade with stake_amount: {} ...", stakeAmount);
        List<CurrencyPair> whitelist = properties.getPairWhitelist();
        // Check if stake_amount is fulfilled
        final BigDecimal balance = exchangeService.getBalance(properties.getStakeCurrency());
        if (balance.compareTo(stakeAmount) > 0) {
            LOGGER.info("stake amount is not fulfilled (available={}, stake={}, currency={})", balance,
                    properties.getStakeCurrency(), properties.getStakeCurrency());
            return Optional.empty();
        } else {
            LOGGER.debug("balance is sufficient: {}, stake: {}, currency: {}", balance, properties.getStakeAmount(),
                    properties.getStakeCurrency());
        }

        // Remove currently opened and latest pairs from whitelist
        List<TradeEntity> trades = tradeService.findAllOpenTrade();
        Optional<TradeEntity> latestTrade = tradeService.findLastClosedTrade();
        if (latestTrade.isPresent()) {
            trades.add(latestTrade.get());
        }
        for (TradeEntity trade : trades) {
            if (whitelist.contains(new CurrencyPair(trade.getPair()))) {
                whitelist.remove(new CurrencyPair(trade.getPair()));
                LOGGER.debug("Ignoring {} in pair whitelist", trade.getPair());
            }
        }
        if (whitelist.isEmpty()) {
            LOGGER.info("No pair in whitelist");
            return Optional.empty();
        }

        // Pick pair based on StochRSI buy signals
        CurrencyPair pair = null;
        for (CurrencyPair _pair : whitelist) {
            ZonedDateTime minimumDate = ZonedDateTime.now().minusHours(6);
            List<BittrexChartData> rawTickers = exchangeService.fetchRawticker(_pair, minimumDate);    
            TimeSeries tickers = new BittrexDataConverter().parseRawTickers(rawTickers);
            
            if (analyzeService.getBuySignal(tickers)) {
                pair = _pair;
                break;
            }
        }

        if (pair == null) {
            return Optional.empty();
        }

        BigDecimal openRate = getTargetBid(exchangeService.getTicker(pair));
        BigDecimal amount = stakeAmount.divide(openRate);
        String orderId = exchangeService.buy(pair, openRate, amount);

        // Create trade entity and return
        String message = String.format("*%s:* Buying [%s](%s) at rate `{%s}`",
                exchange,
                pair,
                exchangeService.getPairDetailUrl(pair.toString()),
                openRate);
        LOGGER.info(message);
        telegramService.sendMessage(message);

        TradeEntity trade = new TradeEntity();
        trade.setPair(pair.toString());
        trade.setStakeAmount(stakeAmount);
        trade.setOpenRate(openRate);
        trade.setOpenDate(LocalDateTime.now());
        trade.setAmount(amount);
        trade.setExchange("bittrex");
        trade.setOpenOrderId(orderId);
        trade.setOpen(true);
        return Optional.of(trade);
    }

    /**
     * Main function which handles the application state
     *
     * @throws InterruptedException if a sleep time is interrupted
     * @throws TelegramApiException if any error occurs while using Telegram API
     */
    public void main() throws TelegramApiException, InterruptedException {

        // Set initial application state
        state = properties.getInitialState();

        try {
            State oldState = state;
            LOGGER.info("Initial State: {}", oldState);
            telegramService.sendMessage(String.format("*Status:* `%s`", oldState));
            boolean running = true;
            while (running) {
                State newState = state;
                // Log state transition
                if (newState != oldState) {
                    telegramService.sendMessage(String.format("*Status:* `%s`", newState));
                    LOGGER.info("Changing state to: {}", newState);
                }

                switch (newState) {
                case STOPPED:
                    running = false;
                    break;
                case RUNNING:
                    process();
                    // We need to sleep here because otherwise we would run into bittrex rate limit
                    Thread.sleep(25 * 1000L);
                    break;
                default:
                    Thread.sleep(1 * 1000L);
                }
                oldState = newState;
            }
        } catch (RuntimeException e) {
            telegramService.sendMessage(String.format("*Status:* Got RuntimeError: ```%n%s%n```", e.getMessage()));
            LOGGER.error("RuntimeError. Trader stopped!", e);
        } finally {
            telegramService.sendMessage("*Status:* `Trader has stopped`");
        }
    }

    public State getState() {
        return state;
    }

    public void updateState(State state) {
        this.state = state;
    }

}
