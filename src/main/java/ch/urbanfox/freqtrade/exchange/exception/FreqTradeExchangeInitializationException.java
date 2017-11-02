package ch.urbanfox.freqtrade.exchange.exception;

public class FreqTradeExchangeInitializationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FreqTradeExchangeInitializationException(String message) {
        super(message);
    }

}
