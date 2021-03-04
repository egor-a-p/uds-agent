package example.uds.grpc;

import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.RequiredArgsConstructor;

import static io.grpc.netty.GrpcSslContexts.forServer;
import static io.netty.handler.ssl.SslProvider.OPENSSL;

@RequiredArgsConstructor
public class Server {
    private final int port;
    private final int metricsPort;

    public void start() throws Exception {
        final var metricsServer = new MetricsServer(metricsPort);
        metricsServer.start();
        final var certificate = new SelfSignedCertificate();
        final var server = NettyServerBuilder.forPort(port)
                .channelType(EpollServerSocketChannel.class)
                .workerEventLoopGroup(new EpollEventLoopGroup())
                .bossEventLoopGroup(new EpollEventLoopGroup(1))
                .sslContext(forServer(certificate.certificate(), certificate.privateKey()).sslProvider(OPENSSL).build())
                .addService(new EchoService("server", metricsServer.registry()))
                .build()
                .start();
        System.out.println("server started on port: " + port);
        server.awaitTermination();
        metricsServer.awaitTermination();
    }
}
