package space.delusive.tversu.component;

public interface MetricsRegistrar {
    void registerPath(String path);

    void registerUserCall(Integer userId);

    void registerTimeConsumed(long millisecond);
}
