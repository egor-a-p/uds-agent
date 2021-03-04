package example.uds.grpc;

import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Agent {
    private final String socketPath;
    private final int metricsPort;

    public void start() throws Exception {
        final var metricsServer = new MetricsServer(metricsPort);
        metricsServer.start();
        final var server = NettyServerBuilder.forAddress(new DomainSocketAddress(socketPath))
                .channelType(EpollServerDomainSocketChannel.class)
                .workerEventLoopGroup(new EpollEventLoopGroup())
                .bossEventLoopGroup(new EpollEventLoopGroup(1))
                .addService(new EchoService("agent", metricsServer.registry()))
                .build()
                .start();
        System.out.println("agent started in path: " + socketPath);
        server.awaitTermination();
        metricsServer.awaitTermination();
    }
}
