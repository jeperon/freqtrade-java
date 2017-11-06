package ch.urbanfox.freqtrade;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class FreqTradeConfiguration {

    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private FreqTradeProperties properties;

    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();

        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("asyncEventExecutor-");
        taskExecutor.setCorePoolSize(4);
        taskExecutor.initialize();

        eventMulticaster.setTaskExecutor(taskExecutor);
        return eventMulticaster;
    }

    @Bean
    public Exchange exchange() {
        return ExchangeFactory.INSTANCE.createExchangeWithApiKeys(BittrexExchange.class.getName(),
                properties.getBittrex().getKey(), properties.getBittrex().getSecret());
    }

    @EventListener
    public void contextCloseEventListener(ContextClosedEvent event) {
        if (taskExecutor != null) {
            taskExecutor.shutdown();
        }
    }

}
