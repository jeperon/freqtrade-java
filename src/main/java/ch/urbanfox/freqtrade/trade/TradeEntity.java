package ch.urbanfox.freqtrade.trade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "trades")
public class Trade {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "exchange", nullable = false)
    private String exchange;

    @Column(name = "pair", nullable = false)
    private String pair;

    @Column(name = "is_open", nullable = false)
    private Boolean open = true;

    @Column(name = "open_rate", nullable = false)
    private BigDecimal openRate;

    @Column(name = "close_rate")
    private BigDecimal closeRate;

    @Column(name = "close_profit")
    private BigDecimal closeProfit;

    @Column(name = "stake_amount", nullable = false)
    private BigDecimal stakeAmount;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "open_date", nullable = false)
    private LocalDateTime openDate;

    @Column(name = "close_date")
    private LocalDateTime closeDate;

    @Column(name = "open_order_id")
    private String openOrderId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public Boolean getOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public BigDecimal getOpenRate() {
        return openRate;
    }

    public void setOpenRate(BigDecimal openRate) {
        this.openRate = openRate;
    }

    public BigDecimal getCloseRate() {
        return closeRate;
    }

    public void setCloseRate(BigDecimal closeRate) {
        this.closeRate = closeRate;
    }

    public BigDecimal getCloseProfit() {
        return closeProfit;
    }

    public void setCloseProfit(BigDecimal closeProfit) {
        this.closeProfit = closeProfit;
    }

    public BigDecimal getStakeAmount() {
        return stakeAmount;
    }

    public void setStakeAmount(BigDecimal stakeAmount) {
        this.stakeAmount = stakeAmount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getOpenDate() {
        return openDate;
    }

    public void setOpenDate(LocalDateTime openDate) {
        this.openDate = openDate;
    }

    public LocalDateTime getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(LocalDateTime closeDate) {
        this.closeDate = closeDate;
    }

    public String getOpenOrderId() {
        return openOrderId;
    }

    public void setOpenOrderId(String openOrderId) {
        this.openOrderId = openOrderId;
    }

    @Override
    public String toString() {
        String openSince;
        if (open) {
            openSince = Long.toString(openDate.until(LocalDateTime.now(), ChronoUnit.MINUTES));
        } else {
            openSince = "closed";
        }

        StringBuilder builder = new StringBuilder("Trade(");
        builder.append("id=").append(id);
        builder.append(", pair=").append(pair);
        builder.append(", amount=").append(amount);
        builder.append(", open_rate=").append(openRate);
        builder.append(", open_since=").append(openSince).append("minutes");
        builder.append(")");
        return builder.toString();
    }

}
