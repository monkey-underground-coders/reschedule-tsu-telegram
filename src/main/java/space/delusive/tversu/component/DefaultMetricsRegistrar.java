package space.delusive.tversu.component;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DefaultMetricsRegistrar implements MetricsRegistrar {
    private MeterRegistry meterRegistry;
    private Set<Integer> users;
    private Timer timer;

    public DefaultMetricsRegistrar(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.users = new HashSet<>();
    }

    @PostConstruct
    public void init() {
        if (meterRegistry != null) {
            meterRegistry.config().commonTags("bot", "telegram");
            this.timer = meterRegistry.timer("rt.time.per.message");
            Gauge.builder("rt.unique.users", () -> users.size())
                    .register(meterRegistry);
        }
    }

    public void registerPath(String path) {
        meterRegistry.counter("rt.path.usage", "path", path).increment();
    }

    public void registerUserCall(Integer userId) {
        users.add(userId);
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void updateUserStats() {
        users.clear();
    }

    public void registerTimeConsumed(long millisecond) {
        timer.record(millisecond, TimeUnit.MILLISECONDS);
    }
}
