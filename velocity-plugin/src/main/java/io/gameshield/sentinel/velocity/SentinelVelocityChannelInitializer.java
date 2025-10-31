package io.gameshield.sentinel.velocity;

import com.velocitypowered.proxy.connection.MinecraftConnection;
import io.gameshield.sentinel.base.haproxy.HAProxyProtocolSniffer;
import io.gameshield.sentinel.base.subnet.AddressSubnet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author milansky
 */
@Slf4j
@RequiredArgsConstructor(staticName = "create")
public final class SentinelVelocityChannelInitializer extends ChannelInitializer<Channel> {
    private static final MethodHandle MH_INIT_CHANNEL;
    private static final VarHandle VH_REMOTE_ADDRESS;

    static {
        try {
            val channelInitializerLookup = MethodHandles.privateLookupIn(ChannelInitializer.class, MethodHandles.lookup());
            MH_INIT_CHANNEL = channelInitializerLookup.findVirtual(
                    ChannelInitializer.class,
                    "initChannel",
                    MethodType.methodType(void.class, Channel.class)
            );

            val minecraftConnectionLookup = MethodHandles.privateLookupIn(MinecraftConnection.class, MethodHandles.lookup());
            VH_REMOTE_ADDRESS = minecraftConnectionLookup.findVarHandle(
                    MinecraftConnection.class,
                    "remoteAddress",
                    SocketAddress.class
            );
        } catch (final @NotNull Throwable throwable) {
            log.error("Failed to lookup method handles", throwable);

            throw new ExceptionInInitializerError(throwable);
        }
    }

    private final ChannelInitializer<Channel> downstream;
    private final AddressSubnet allowedSubnet;

    @Override
    @SneakyThrows
    protected void initChannel(final @NotNull Channel channel) {
        MH_INIT_CHANNEL.invoke(downstream, channel);

        if (!allowedSubnet.contains(((InetSocketAddress) channel.remoteAddress()).getAddress())) return;

        val pipeline = channel.pipeline();

        if (pipeline.get(HAProxyMessageDecoder.class) != null) pipeline.remove(HAProxyMessageDecoder.class);

        val sniffer = HAProxyProtocolSniffer.create((ctx, address) -> {
            val minecraftConnection = ctx.pipeline().get(MinecraftConnection.class);
            VH_REMOTE_ADDRESS.set(minecraftConnection, address);
        });

        pipeline.addFirst("gs-proxy-sniffer", sniffer);
    }
}
