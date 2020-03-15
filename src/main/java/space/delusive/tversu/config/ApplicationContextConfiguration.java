package space.delusive.tversu.config;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import space.delusive.tversu.manager.DataManager;
import space.delusive.tversu.manager.impl.PropertiesManager;

@Configuration
@ComponentScan("space.delusive.tversu")
@PropertySource("classpath:timingbot.properties")
public class ApplicationContextConfiguration {

    @Bean("messages")
    public DataManager getMessagesManager() {
        return new PropertiesManager("/messages_ru.properties");
    }

    @Bean("config")
    public DataManager getConfigManager() {
        return new PropertiesManager("/timingbot.properties");
    }

    @Bean
    public JdbcTemplate getJdbcTemplate(@Value("${db.url}") String url,
                                        @Value("${db.username}") String username,
                                        @Value("${db.password}") String password) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        return new JdbcTemplate(dataSource);
    }
}
