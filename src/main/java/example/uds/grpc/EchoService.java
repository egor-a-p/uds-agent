package example.uds.grpc;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class EchoService extends ReactorEchoGrpc.EchoImplBase {
    private final String source;
    private final MeterRegistry registry;

    public EchoService(String source, MeterRegistry registry) {
        this.source = source;
        this.registry = registry;
    }

    public Mono<Message> send(Mono<Message> request) {
        return request.map(message -> Message.newBuilder()
                .setMessage("echo: " + message.getMessage())
                .setSource(source)
                .setTimestamp(message.getTimestamp())
                .setId(message.getId())
                .build())
                .doOnSuccess(this::record);
    }

    private void record(Message message) {
        long duration = TimeUnit.SECONDS.toNanos(Instant.now().getEpochSecond()) + Instant.now().getNano() - message.getTimestamp();
        Timer.builder("request_duration")
                .tag("source", source)
                .publishPercentileHistogram()
                .register(registry)
                .record(duration, TimeUnit.NANOSECONDS);
    }
}
