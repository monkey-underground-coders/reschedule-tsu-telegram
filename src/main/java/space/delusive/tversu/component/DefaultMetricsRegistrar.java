package space.delusive.tversu.component;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import space.delusive.tversu.repository.UserRepository;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class DefaultMetricsRegistrar implements MetricsRegistrar {
    private final Set<Integer> users = new HashSet<>();
    private final MeterRegistry meterRegistry;
    private final UserRepository userRepository;

    private Timer timer;

    @PostConstruct
    public void init() {
        if (meterRegistry != null) {
            meterRegistry.config().commonTags("bot", "telegram");
            this.timer = meterRegistry.timer("rt.time.per.message");
            Gauge.builder("rt.users.today", users::size)
                    .register(meterRegistry);
            updateUsersStats();
        }
    }

    public void registerPath(String path) {
        meterRegistry.counter("rt.path.usage", "path", path).increment();
    }

    public void registerUserCall(Integer userId) {
        users.add(userId);
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void updateTodayUsersStats() {
        users.clear();
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void updateUsersStats() {
        userRepository.getCoursesInfo()
                .forEach(info -> meterRegistry.gauge("rt.users.info",
                        List.of(
                                Tag.of("faculty", info.getFaculty()),
                                Tag.of("course", Integer.toString(info.getCourse())),
                                Tag.of("program", info.getProgram())
                        ),
                        info.getCount()
                ));
    }

    public void registerTimeConsumed(long millisecond) {
        timer.record(millisecond, TimeUnit.MILLISECONDS);
    }
}
