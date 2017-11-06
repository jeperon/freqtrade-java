package ch.urbanfox.freqtrade.analyze.indicator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.AverageDirectionalMovementIndicator;
import eu.verdelhan.ta4j.indicators.CachedIndicator;
import eu.verdelhan.ta4j.indicators.EMAIndicator;
import eu.verdelhan.ta4j.indicators.ParabolicSarIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;

public class BuySignalIndicator extends CachedIndicator<Boolean> {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(BuySignalIndicator.class);

    private final UpSwingIndicator upSwingIndicator;
    private final SwapIndicator swapIndicator;
    private final AverageDirectionalMovementIndicator adxIndicator;

    private final Decimal momentum;

    public BuySignalIndicator(TimeSeries series, int timeframe, Decimal momentum) {
        super(series);

        this.momentum = momentum;

        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);

        EMAIndicator emaIndicator = new EMAIndicator(closePriceIndicator, timeframe);
        ParabolicSarIndicator sarIndicator = new ParabolicSarIndicator(series, timeframe);
        this.adxIndicator = new AverageDirectionalMovementIndicator(series, timeframe);

        // wait for stable turn from bearish to bullish market
        this.swapIndicator = new SwapIndicator(closePriceIndicator, sarIndicator);

        // consider prices above ema to be in upswing
        this.upSwingIndicator = new UpSwingIndicator(closePriceIndicator, emaIndicator);
    }

    @Override
    protected Boolean calculate(int index) {

        Boolean upSwing = upSwingIndicator.getValue(index);
        Boolean swap = swapIndicator.getValue(index);
        Decimal adxValue = adxIndicator.getValue(index);
        Boolean adx = adxValue.isGreaterThan(momentum);

        LOGGER.debug("@index={} upSwing={} swap={} adx={} (value={}, momentum={})", index, upSwing, swap, adx, adxValue,
                momentum);

        return upSwing && swap && adx;
    }

}
