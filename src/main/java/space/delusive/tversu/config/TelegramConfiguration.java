package space.delusive.tversu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Configuration
public class TelegramConfiguration {
    @Bean("options")
    public DefaultBotOptions defaultBotOptions(@Value("${bot.proxy.host:}") String host,
                                               @Value("${bot.proxy.port:}") String port,
                                               @Value("${bot.proxy.type:}") String proxyType) {
        DefaultBotOptions defaultBotOptions = new DefaultBotOptions();
        if (!host.equals("")) {
            defaultBotOptions.setProxyHost(host);
            defaultBotOptions.setProxyPort(Integer.parseInt(port));
            defaultBotOptions.setProxyType(DefaultBotOptions.ProxyType.valueOf(proxyType));
        }
        return defaultBotOptions;
    }
}
