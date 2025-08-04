package project.coca;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import org.springframework.stereotype.Component;

@Component
public class MetricFilter implements MeterFilter {
    @Override
    public MeterFilterReply accept(Meter.Id id) {
        if (id.getName().startsWith("http.server.requests")) {
            String uri = id.getTag("uri");
            if (uri != null && (uri.contains("/api/healthcheck") || uri.contains("/actuator/prometheus"))) {
                return MeterFilterReply.DENY;
            }
        }
        return MeterFilterReply.NEUTRAL;
    }
}
