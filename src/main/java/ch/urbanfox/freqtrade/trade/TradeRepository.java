package ch.urbanfox.freqtrade.trade;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<TradeEntity, Long> {

    List<TradeEntity> findByOpenIsTrue();

    List<TradeEntity> findByOpenIsFalse();

    Optional<TradeEntity> findByIdAndOpenIsTrue(Long tradeId);

    Optional<TradeEntity> findFirstByOpenIsFalseOrderByIdDesc();

}
