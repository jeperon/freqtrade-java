package ch.urbanfox.freqtrade.trade;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TradeService {

    /**
     * Executes a sell for the given trade and updated the entity.
     *
     * @param trade the trade upon which the sell order was executed
     * @param rate rate to sell for
     * @param amount amount to sell
     * @return current profit as percentage
     *
     * @throws IOException if any error occurs while contacting the exchange for information
     */
    BigDecimal executeSellOrder(TradeEntity trade, BigDecimal rate, BigDecimal amount) throws IOException;

    List<TradeEntity> findAllOpenTrade();

    List<TradeEntity> findAllClosedTrade();

    void save(TradeEntity trade);

    List<TradeEntity> findAllOrderById();

    Optional<TradeEntity> findOpenTradeById(Long tradeId);

    Optional<TradeEntity> findLastClosedTrade();

}
