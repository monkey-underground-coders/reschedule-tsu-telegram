package space.delusive.tversu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import space.delusive.tversu.manager.DataManager;
import space.delusive.tversu.manager.impl.PropertiesManager;

@Configuration
@ComponentScan("space.delusive.tversu")
public class ApplicationContextConfiguration {

    @Bean("messages")
    public DataManager getMessagesManager() {
        return new PropertiesManager("/messages_ru.properties");
    }

    @Bean("config")
    public DataManager getConfigManager() {
        return new PropertiesManager("/timingbot.properties");
    }
}
