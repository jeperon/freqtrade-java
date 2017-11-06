package ch.urbanfox.freqtrade.exchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.bittrex.dto.marketdata.BittrexChartData;
import org.knowm.xchange.bittrex.service.BittrexChartDataPeriodType;
import org.knowm.xchange.bittrex.service.BittrexMarketDataService;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.urbanfox.freqtrade.FreqTradeProperties;
import ch.urbanfox.freqtrade.exchange.exception.FreqTradeExchangeInitializationException;

@Component
public class FreqTradeExchangeServiceImpl implements FreqTradeExchangeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FreqTradeExchangeServiceImpl.class);

    private final FreqTradeProperties properties;

    /**
     * Current selected exchange
     */
    private final Exchange exchange;
    private final BittrexMarketDataService marketDataService;
    private final AccountService accountService;
    private final TradeService tradeService;

    @Autowired
    public FreqTradeExchangeServiceImpl(FreqTradeProperties properties, Exchange exchange) {
        this.properties = properties;

        this.exchange = exchange;

        marketDataService = (BittrexMarketDataService) exchange.getMarketDataService();
        accountService = exchange.getAccountService();
        tradeService = exchange.getTradeService();

        if (properties.isDryRun()) {
            LOGGER.info("Instance is running with dry_run enabled");
        }

        // Check if all pairs are available
        List<CurrencyPair> markets = getMarkets();
        for (CurrencyPair pair : properties.getPairWhitelist()) {
            if (!markets.contains(pair)) {
                throw new FreqTradeExchangeInitializationException("Pair: " + pair + " is not available");
            }
        }
    }

    @Override
    public String buy(CurrencyPair pair, BigDecimal rate, BigDecimal amount) throws IOException {
        LOGGER.debug("Placing buy order: pair: {}, rate: {}, amount: {}", pair, rate, amount);

        if (properties.isDryRun()) {
            return "dry_run";
        } else {
            LimitOrder limitOrder = new LimitOrder(OrderType.BID, amount, pair, null, null, rate);
            return tradeService.placeLimitOrder(limitOrder);
        }
    }

    @Override
    public String sell(CurrencyPair pair, BigDecimal rate, BigDecimal amount) throws IOException {
        LOGGER.debug("Placing sell order: pair: {}, rate: {}, amount: {}", pair, rate, amount);

        if (properties.isDryRun()) {
            return "dry_run";
        } else {
            LimitOrder limitOrder = new LimitOrder(OrderType.ASK, amount, pair, null, null, rate);
            return tradeService.placeLimitOrder(limitOrder);
        }
    }

    @Override
    public BigDecimal getBalance(Currency currency) throws IOException {
        if (properties.isDryRun()) {
            return BigDecimal.valueOf(999.9);
        } else {
            AccountInfo accountInfo = accountService.getAccountInfo();

            BigDecimal total = BigDecimal.ZERO;
            for (Wallet wallet : accountInfo.getWallets().values()) {
                Balance balance = wallet.getBalance(currency);
                total.add(balance.getAvailable());
            }

            return total;
        }
    }

    @Override
    public Ticker getTicker(CurrencyPair pair) throws IOException {
        LOGGER.debug("Getting ticker for pair: {}", pair);
        return marketDataService.getTicker(pair);
    }

    @Override
    public List<LimitOrder> getOpenOrders(CurrencyPair pair) throws IOException {
        LOGGER.debug("Fetching open orders for pair: {}", pair);

        if (properties.isDryRun()) {
            return new ArrayList<>();
        } else {
            OpenOrdersParams params = new DefaultOpenOrdersParamCurrencyPair(pair);

            OpenOrders openOrders = tradeService.getOpenOrders(params);

            return openOrders.getOpenOrders();
        }
    }

    @Override
    public String getPairDetailUrl(String pair) {
        return String.format("https://bittrex.com/Market/Index?MarketName=%s", pair.replace("_", "-"));
    }

    @Override
    public List<BittrexChartData> fetchRawticker(CurrencyPair pair) throws IOException {
        LOGGER.debug("Fetching ticks for: {}", pair);

        List<BittrexChartData> ticks = marketDataService.getBittrexChartData(pair, BittrexChartDataPeriodType.FIVE_MIN);

        LOGGER.debug("Found {} ticks", ticks.size());

        return ticks;
    }

    @Override
    public List<BittrexChartData> fetchRawticker(CurrencyPair pair, ZonedDateTime minimumDate) throws IOException {
        LOGGER.debug("Fetching ticks for: {}, minimum date: {}", pair, minimumDate);

        List<BittrexChartData> ticks = marketDataService.getBittrexChartData(pair, BittrexChartDataPeriodType.FIVE_MIN);

        LOGGER.debug("Found {} ticks", ticks.size());

        List<BittrexChartData> filteredTicks = ticks.stream()
                .filter(t -> t.getTimeStamp().after(Date.from(minimumDate.toInstant())))
                .collect(Collectors.toList());

        LOGGER.debug("After filtering: {} ticks", filteredTicks.size());

        return filteredTicks;
    }

    /**
     * Returns all available markets
     *
     * @return list of all available pairs
     */
    private List<CurrencyPair> getMarkets() {
        LOGGER.debug("Fetching currency pairs...");
        List<CurrencyPair> availablePairs = exchange.getExchangeSymbols();
        LOGGER.debug("Available pairs: {}", availablePairs);
        return availablePairs;
    }

}
