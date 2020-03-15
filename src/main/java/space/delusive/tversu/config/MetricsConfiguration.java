package space.delusive.tversu.config;

import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import space.delusive.tversu.component.DefaultMetricsRegistrar;
import space.delusive.tversu.component.MetricsExposer;
import space.delusive.tversu.component.MockMetricsRegistrar;
import space.delusive.tversu.dao.UserDao;

@Configuration
@EnableScheduling
@PropertySource("classpath:timingbot.properties")
public class MetricsConfiguration {
    @Bean
    @Conditional(MetricsEnabledCondition.class)
    TaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Bean
    @Conditional(MetricsEnabledCondition.class)
    PrometheusMeterRegistry prometheusMeterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    @Bean
    @Conditional(MetricsEnabledCondition.class)
    DefaultMetricsRegistrar defaultMetricsRegistrar(PrometheusMeterRegistry prometheusMeterRegistry, UserDao userDao) {
        return new DefaultMetricsRegistrar(prometheusMeterRegistry, userDao);
    }

    @Bean
    @Conditional(MetricsDisabledCondition.class)
    MockMetricsRegistrar mockMetricsRegistrar() {
        return new MockMetricsRegistrar();
    }


    @Bean
    @Conditional(MetricsEnabledCondition.class)
    MetricsExposer metricsExposer(PrometheusMeterRegistry prometheusMeterRegistry, @Value("${metrics.port}") String port) {
        return new MetricsExposer(prometheusMeterRegistry, Integer.parseInt(port));
    }
}
