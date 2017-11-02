package ch.urbanfox.freqtrade.exchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;

public interface FreqTradeExchangeService {

    /**
     * Places a limit buy order.
     *
     * @param pair currency pair
     * @param rate Rate limit for order
     * @param amount The amount to purchase
     * @return orderId of the placed buy order
     *
     * @throws IOException if any error occur while contacting the exchange
     */
    String buy(CurrencyPair pair, BigDecimal rate, BigDecimal amount) throws IOException;

    /**
     * Get Ticker for given pair.
     *
     * @param pair Pair as str, format: BTC_ETC
     * @return the ticker
     *
     * @throws IOException if any error occur while contacting the exchange
     */
    Ticker getTicker(CurrencyPair pair) throws IOException;

    /**
     * Places a limit sell order.
     *
     * @param pair currency pair
     * @param rate Rate limit for order
     * @param amount The amount to sell
     * @return the order ID
     *
     * @throws IOException if any communication error occur while contacting the exchange
     */
    String sell(CurrencyPair pair, BigDecimal rate, BigDecimal amount) throws IOException;

    /**
     * Get all open orders for given pair.
     *
     * @param pair the currency pair
     * @return list of orders
     *
     * @throws IOException if any communication error occur while contacting the exchange
     */
    List<LimitOrder> getOpenOrders(CurrencyPair pair) throws IOException;

    /**
     * Returns the market detail url for the given pair
     *
     * @param pair pair as a String, format: BTC_ANT
     * @return url as a string
     */
    String getPairDetailUrl(String pair);

    /**
     * Get account balance.
     *
     * @param currency currency as str, format: BTC
     * @return balance
     *
     * @throws IOException if any communication error occur while contacting the exchange
     */
    BigDecimal getBalance(Currency currency) throws IOException;

}
