package example.uds.grpc;

import java.util.Optional;

public class Application {
    public static void main(String[] args) throws Exception {
        switch (System.getProperty("profile", "client").toUpperCase()) {
            case "SERVER" -> {
                final int port = Integer.parseInt(Optional.ofNullable(System.getenv("SERVER_PORT")).orElse("8090"));
                final int metricsPort = Integer.parseInt(Optional.ofNullable(System.getenv("METRICS_PORT")).orElse("8091"));
                new Server(port, metricsPort).start();
            }
            case "AGENT" -> {
                final String path = Optional.ofNullable(System.getenv("SOCKET_PATH")).orElse("/tmp/agent.sock");
                final int metricsPort = Integer.parseInt(Optional.ofNullable(System.getenv("METRICS_PORT")).orElse("8092"));
                new Agent(path, metricsPort).start();
            }
            default -> {
                final String path = Optional.ofNullable(System.getenv("SOCKET_PATH")).orElse("/tmp/agent.sock");
                final String host = Optional.ofNullable(System.getenv("SERVER_HOST")).orElse("localhost");
                final int port = Integer.parseInt(Optional.ofNullable(System.getenv("SERVER_PORT")).orElse("8090"));
                new Client(path, host, port).start();
            }
        }
    }
}
