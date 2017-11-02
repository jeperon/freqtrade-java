package ch.urbanfox.freqtrade.analyze.indicator;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.indicators.CachedIndicator;
import eu.verdelhan.ta4j.indicators.ParabolicSarIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;

public class SwapIndicator extends CachedIndicator<Boolean> {

    private static final long serialVersionUID = 1L;
    
    private final ClosePriceIndicator closePriceIndicator;
    private final ParabolicSarIndicator parabolicSarIndicator;

    private final Indicator<Decimal> prevSar;
    private final Indicator<Decimal> prevClose;
    private final Indicator<Decimal> prevSar2;
    private final Indicator<Decimal> prevClose2;

    public SwapIndicator(ClosePriceIndicator closePriceIndicator, ParabolicSarIndicator parabolicSarIndicator) {
        super(closePriceIndicator);
        this.closePriceIndicator = closePriceIndicator;
        this.parabolicSarIndicator = parabolicSarIndicator;

        prevSar = new ShiftedIndicator<>(parabolicSarIndicator, 1);
        prevClose = new ShiftedIndicator<>(closePriceIndicator, 1);
        prevSar2 = new ShiftedIndicator<>(parabolicSarIndicator, 2);
        prevClose2 = new ShiftedIndicator<>(closePriceIndicator, 2);
    }

    @Override
    protected Boolean calculate(int index) {

        boolean closePriceBiggerThanSar = closePriceIndicator.getValue(index)
                .isGreaterThan(parabolicSarIndicator.getValue(index));
        boolean prevClosePriceBiggerThanPrevSar = prevClose.getValue(index).isGreaterThan(prevSar.getValue(index));
        boolean prev2ClosePriceSmallerThanPrev2Sar = prevClose2.getValue(index).isLessThan(prevSar2.getValue(index));

        return closePriceBiggerThanSar && prevClosePriceBiggerThanPrevSar && prev2ClosePriceSmallerThanPrev2Sar;
    }

}
