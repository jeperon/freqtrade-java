package ch.urbanfox.freqtrade.analyze.indicator;

import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.indicators.CachedIndicator;

public class ShiftedIndicator<T> extends CachedIndicator<T> {

    private static final long serialVersionUID = 1L;

    private final Indicator<T> baseIndicator;

    private final Integer shift;

    public ShiftedIndicator(Indicator<T> baseIndicator, Integer shift) {
        super(baseIndicator);
        this.baseIndicator = baseIndicator;
        this.shift = shift;
    }

    @Override
    protected T calculate(int index) {
        return baseIndicator.getValue(index - shift);
    }

}
