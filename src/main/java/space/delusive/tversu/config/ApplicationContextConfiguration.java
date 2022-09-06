package space.delusive.tversu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ResourceLoader;
import space.delusive.tversu.manager.DataManager;
import space.delusive.tversu.manager.impl.PropertiesManager;

@Configuration
@ComponentScan("space.delusive.tversu")
@PropertySource("classpath:timingbot.properties")
@PropertySource("${SPRING_CONFIG_LOCATION:classpath:timingbot.properties}")
public class ApplicationContextConfiguration {

	private final ResourceLoader resourceLoader;

	public ApplicationContextConfiguration(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Bean("messages")
	public DataManager getMessagesManager() {
		return new PropertiesManager(resourceLoader.getResource("classpath:messages_ru.properties"));
	}

	@Bean("config")
	public DataManager getConfigManager() {
		String sourceFromEnv = System.getenv("PROPERTIES_SOURCE");
		String source = sourceFromEnv != null ? sourceFromEnv : "classpath:timingbot.properties";
		return new PropertiesManager(resourceLoader.getResource(source));
	}
}
