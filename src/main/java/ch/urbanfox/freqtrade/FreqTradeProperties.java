package ch.urbanfox.freqtrade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import ch.urbanfox.freqtrade.telegram.TelegramProperties;
import ch.urbanfox.freqtrade.type.State;

/**
 * FreqTrade configuration properties
 */
@Configuration
@ConfigurationProperties(prefix = "freqtrade")
public class FreqTradeProperties {

    private State initialState;

    private int maxOpenTrades;

    private Currency stakeCurrency;

    private BigDecimal stakeAmount;

    private boolean dryRun = false;

    private Map<Long, BigDecimal> minimalRoi = new TreeMap<>();

    private BigDecimal stopLoss;

    private List<CurrencyPair> pairWhitelist = new ArrayList<>();

    private BittrexProperties bittrex;

    private TelegramProperties telegram;

    public State getInitialState() {
        return initialState;
    }

    public void setInitialState(State initialState) {
        this.initialState = initialState;
    }

    public int getMaxOpenTrades() {
        return maxOpenTrades;
    }

    public void setMaxOpenTrades(int maxOpenTrades) {
        this.maxOpenTrades = maxOpenTrades;
    }

    public Currency getStakeCurrency() {
        return stakeCurrency;
    }

    public void setStakeCurrency(Currency stakeCurrency) {
        this.stakeCurrency = stakeCurrency;
    }

    public BigDecimal getStakeAmount() {
        return stakeAmount;
    }

    public void setStakeAmount(BigDecimal stakeAmount) {
        this.stakeAmount = stakeAmount;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public Map<Long, BigDecimal> getMinimalRoi() {
        return minimalRoi;
    }

    public void setMinimalRoi(Map<Long, BigDecimal> minimalRoi) {
        this.minimalRoi = minimalRoi;
    }

    public BigDecimal getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(BigDecimal stopLoss) {
        this.stopLoss = stopLoss;
    }

    public List<CurrencyPair> getPairWhitelist() {
        return pairWhitelist;
    }

    public void setPairWhitelist(List<CurrencyPair> pairWhitelist) {
        this.pairWhitelist = pairWhitelist;
    }

    public BittrexProperties getBittrex() {
        return bittrex;
    }

    public void setBittrex(BittrexProperties bittrex) {
        this.bittrex = bittrex;
    }

    public TelegramProperties getTelegram() {
        return telegram;
    }

    public void setTelegram(TelegramProperties telegram) {
        this.telegram = telegram;
    }

}
