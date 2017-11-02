package ch.urbanfox.freqtrade.trade;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.urbanfox.freqtrade.exchange.FreqTradeExchangeService;

@Service
@Transactional
public class TradeServiceImpl implements TradeService {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private FreqTradeExchangeService exchangeService;

    @Override
    public BigDecimal executeSellOrder(TradeEntity trade, BigDecimal rate, BigDecimal amount) throws IOException {
        BigDecimal profit = rate.subtract(trade.getOpenRate()).divide(trade.getOpenRate())
                .multiply(BigDecimal.valueOf(100));

        // Execute sell and update trade record
        String orderId = exchangeService.sell(new CurrencyPair(trade.getPair()), rate, amount);
        trade.setCloseRate(rate);
        trade.setCloseProfit(profit);
        trade.setCloseDate(LocalDateTime.now());
        trade.setOpenOrderId(orderId);

        return profit;
    }

    @Override
    public List<TradeEntity> findAllOpenTrade() {
        return tradeRepository.findByOpenIsTrue();
    }

    @Override
    public List<TradeEntity> findAllClosedTrade() {
        return tradeRepository.findByOpenIsFalse();
    }

    @Override
    public void save(TradeEntity trade) {
        tradeRepository.save(trade);
    }

    @Override
    public List<TradeEntity> findAllOrderById() {
        return tradeRepository.findAll(new Sort(Direction.ASC, "id"));
    }

    @Override
    public Optional<TradeEntity> findOpenTradeById(Long tradeId) {
        return tradeRepository.findByIdAndOpenIsTrue(tradeId);
    }

    @Override
    public Optional<TradeEntity> findLastClosedTrade() {
        return tradeRepository.findFirstByOpenIsFalseOrderByIdDesc();
    }

}
