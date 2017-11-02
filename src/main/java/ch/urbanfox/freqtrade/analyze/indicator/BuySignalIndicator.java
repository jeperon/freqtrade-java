package ch.urbanfox.freqtrade.analyze.indicator;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.indicators.AverageDirectionalMovementIndicator;
import eu.verdelhan.ta4j.indicators.CachedIndicator;

public class BuySignalIndicator extends CachedIndicator<Boolean> {

    private static final long serialVersionUID = 1L;

    private final UpSwingIndicator upSwingIndicator;
    private final SwapIndicator swapIndicator;
    private final AverageDirectionalMovementIndicator adxIndicator;

    private Decimal momentum = Decimal.valueOf(25);

    public BuySignalIndicator(UpSwingIndicator upSwingIndicator, SwapIndicator swapIndicator,
            AverageDirectionalMovementIndicator adxIndicator) {
        super(upSwingIndicator);
        this.upSwingIndicator = upSwingIndicator;
        this.swapIndicator = swapIndicator;
        this.adxIndicator = adxIndicator;
    }

    @Override
    protected Boolean calculate(int index) {
        return upSwingIndicator.getValue(index) &&
                swapIndicator.getValue(index) &&
                adxIndicator.getValue(index).isGreaterThan(momentum);
    }

    public Decimal getMomentum() {
        return momentum;
    }

    public void setMomentum(Decimal momentum) {
        this.momentum = momentum;
    }

}
