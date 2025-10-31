package io.gameshield.sentinel.bungee;

import io.gameshield.sentinel.base.haproxy.HAProxyProtocolSniffer;
import io.gameshield.sentinel.base.subnet.AddressSubnet;
import io.netty.channel.Channel;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.protocol.channel.ChannelAcceptor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;

/**
 * @author milansky
 */
@Slf4j
@RequiredArgsConstructor(staticName = "create")
public final class SentinelBungeeChannelInitializer implements ChannelAcceptor {
    private static final Field F_HANDLER_BOSS_CHANNEL;

    static {
        try {
            val channelField = HandlerBoss.class.getDeclaredField("channel");
            channelField.setAccessible(true);
            F_HANDLER_BOSS_CHANNEL = channelField;
        } catch (final @NotNull Throwable throwable) {
            log.error("Failed to init Bungee hooks", throwable);
            throw new ExceptionInInitializerError(throwable);
        }
    }

    private final ChannelAcceptor downstream;
    private final AddressSubnet allowedSubnet;

    @Override
    public boolean accept(final @NotNull Channel channel) {
        downstream.accept(channel);

        if (!allowedSubnet.contains(((InetSocketAddress) channel.remoteAddress()).getAddress())) return true;

        val pipeline = channel.pipeline();
        if (pipeline.get(HAProxyMessageDecoder.class) != null) pipeline.remove(HAProxyMessageDecoder.class);

        val sniffer = HAProxyProtocolSniffer.create((ctx, socketAddress) -> {
            try {
                val boss = ctx.pipeline().get(HandlerBoss.class);
                if (boss == null) return;

                val wrapper = (ChannelWrapper) F_HANDLER_BOSS_CHANNEL.get(boss);
                if (wrapper == null) return;

                if (socketAddress != null) wrapper.setRemoteAddress(socketAddress);
            } catch (final @NotNull Throwable throwable) {
                log.warn("Failed to apply HAProxy remote address: {}", throwable.getMessage());
            }
        });

        pipeline.addFirst("gs-haproxy-sniffer", sniffer);

        return true;
    }
}
