package example.uds.grpc;

import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.util.NettyRuntime;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static io.grpc.netty.GrpcSslContexts.forClient;
import static io.netty.handler.ssl.SslProvider.OPENSSL;
import static io.netty.handler.ssl.util.InsecureTrustManagerFactory.INSTANCE;

@RequiredArgsConstructor
public class Client {
    private final String agentSocketPath;
    private final String serverHost;
    private final int serverPort;

    public final void start() throws Exception {
        final var group = new EpollEventLoopGroup();
        final var agentStub = ReactorEchoGrpc.newReactorStub(
                NettyChannelBuilder.forAddress(new DomainSocketAddress(agentSocketPath))
                        .eventLoopGroup(group)
                        .channelType(EpollDomainSocketChannel.class)
                        .usePlaintext()
                        .build());

        final var serverStub = ReactorEchoGrpc.newReactorStub(
                NettyChannelBuilder.forAddress(serverHost, serverPort)
                        .eventLoopGroup(group)
                        .channelType(EpollSocketChannel.class)
                        .sslContext(forClient().sslProvider(OPENSSL).trustManager(INSTANCE).build())
                        .build());

        final var latch = new CountDownLatch(NettyRuntime.availableProcessors());
        IntStream.range(0, NettyRuntime.availableProcessors())
                .mapToObj(i -> new Worker(agentStub, serverStub, i))
                .forEach(worker -> worker.start(latch));
        latch.await();
    }

    @RequiredArgsConstructor
    public static class Worker {
        private final ReactorEchoGrpc.ReactorEchoStub agentStub;
        private final ReactorEchoGrpc.ReactorEchoStub serverStub;
        private final int id;

        public void start(CountDownLatch latch) {
            Flux.interval(Duration.ofMillis(id), Duration.ofMillis(10))
                    .publishOn(Schedulers.parallel())
                    .map(i -> Message.newBuilder()
                            .setId(i)
                            .setMessage("hello from worker-" + id)
                            .setSource("client")
                            .setTimestamp(TimeUnit.SECONDS.toNanos(Instant.now().getEpochSecond()) + Instant.now().getNano())
                            .build())
                    .flatMap(message -> agentStub.send(message).onErrorResume(throwable -> serverStub.send(message)).retry())
                    .doFinally(signalType -> latch.countDown())
                    .subscribe();
        }
    }
}
