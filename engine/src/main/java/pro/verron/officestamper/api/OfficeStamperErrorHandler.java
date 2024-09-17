package pro.verron.officestamper.api;

import org.springframework.lang.Nullable;

public interface OfficeStamperErrorHandler {
    @Nullable Object resolve(Exception exception);
}
