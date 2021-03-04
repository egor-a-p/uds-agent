package example.uds.grpc;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

@RequiredArgsConstructor
public class MetricsServer {
    private final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    private final int port;
    private DisposableServer server;

    public void start() {
        this.server = HttpServer.create().port(port)
                .route(routes -> routes.get("/prometheus", (req, resp) -> resp.sendString(Mono.fromCallable(registry::scrape))))
                .bindNow();
    }

    public void awaitTermination() {
        server.onDispose().block();
    }

    public MeterRegistry registry() {
        return registry;
    }
}
