package ch.urbanfox.freqtrade.analyze.indicator;

import eu.verdelhan.ta4j.indicators.CachedIndicator;
import eu.verdelhan.ta4j.indicators.EMAIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;

public class UpSwingIndicator extends CachedIndicator<Boolean> {

    private static final long serialVersionUID = 1L;
    
    private final ClosePriceIndicator closePriceIndicator;
    private final EMAIndicator emaIndicator;

    public UpSwingIndicator(ClosePriceIndicator closePriceIndicator, EMAIndicator emaIndicator) {
        super(closePriceIndicator);
        this.closePriceIndicator = closePriceIndicator;
        this.emaIndicator = emaIndicator;
    }

    @Override
    protected Boolean calculate(int index) {
        return emaIndicator.getValue(index).isLessThanOrEqual(closePriceIndicator.getValue(index));
    }

}
