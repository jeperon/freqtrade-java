package ch.urbanfox.freqtrade.telegram.command;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.urbanfox.freqtrade.FreqTradeMainRunner;
import ch.urbanfox.freqtrade.exchange.FreqTradeExchangeService;
import ch.urbanfox.freqtrade.telegram.TelegramService;
import ch.urbanfox.freqtrade.trade.TradeEntity;
import ch.urbanfox.freqtrade.trade.TradeService;
import ch.urbanfox.freqtrade.type.State;

/**
 * Handler for /forcesell <id>
 *
 * Sells the given trade at current price
 */
@Component
public class ForceSellCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForceSellCommandHandler.class);

    @Autowired
    private FreqTradeMainRunner runner;

    @Autowired
    private FreqTradeExchangeService exchangeService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private TelegramService telegramService;

    @Override
    public String getCommandName() {
        return "/forcesell";
    }

    @Override
    protected void handleInternal(String[] params) throws Exception {
        if (runner.getState() != State.RUNNING) {
            telegramService.sendMessage("`trader is not running`");
            return;
        }

        try {
            Long tradeId = Long.parseLong(params[0]);
            // Query for trade
            Optional<TradeEntity> tradeOptional = tradeService.findOpenTradeById(tradeId);
            if (tradeOptional.isPresent()) {
                TradeEntity trade = tradeOptional.get();

                // Get current rate
                BigDecimal currentRate = exchangeService.getTicker(new CurrencyPair(trade.getPair())).getBid();
                // Get available balance
                String currency = new CurrencyPair(trade.getPair()).toString().split("/")[1];
                BigDecimal balance = exchangeService.getBalance(Currency.getInstance(currency));
                // Execute sell
                BigDecimal profit = tradeService.executeSellOrder(trade, currentRate, balance);
                String message = String.format(
                        "*%s:* Selling [%s](%s) at rate `%s (profit: %s\\%)`",
                        trade.getExchange(),
                        trade.getPair(),
                        exchangeService.getPairDetailUrl(trade.getPair()),
                        trade.getCloseRate(),
                        profit.round(new MathContext(2)).toString());
                LOGGER.info(message);
                telegramService.sendMessage(message);
            } else {
                telegramService.sendMessage(String.format("There is no open trade with ID: `%l`", tradeId));
            }

        } catch (NumberFormatException e) {
            telegramService.sendMessage("Invalid argument. Usage: `/forcesell <trade_id>`");
            LOGGER.warn("/forcesell: Invalid argument received");
        }
    }

}
