package io.gameshield.sentinel.base.haproxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

/**
 * @author milansky
 */
@RequiredArgsConstructor(staticName = "create")
public final class HAProxyProtocolSniffer extends ChannelInboundHandlerAdapter {
    private static final byte[] PROXY_V1_PREFIX = "PROXY".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PROXY_V2_PREFIX = new byte[] {
            0x0D, 0x0A, 0x0D, 0x0A,
            0x00, 0x0D, 0x0A, 0x51,
            0x55, 0x49, 0x54, 0x0A
    };
    private static final byte[] PROXY_HEALTHCHECK = "INVALID_PROXY_HEADER\r\n".getBytes(StandardCharsets.US_ASCII);

    private final BiConsumer<ChannelHandlerContext, InetSocketAddress> handler;
    private boolean first = true;

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext context, final @NotNull Object msg) {
        try {
            if (first && msg instanceof ByteBuf) {
                val byteBuf = (ByteBuf) msg;
                first = false;

                if (startsWith(byteBuf, PROXY_HEALTHCHECK)) {
                    context.channel().close();
                    return;
                }

                val pipeline = context.pipeline();

                if (startsWith(byteBuf, PROXY_V1_PREFIX) || startsWith(byteBuf, PROXY_V2_PREFIX)) {
                    pipeline.addAfter(context.name(), "gs-haproxy-decoder", new HAProxyMessageDecoder());
                    pipeline.addAfter("gs-haproxy-decoder", "gs-haproxy-handler", new HaProxyAddressHandler(handler));
                }

                pipeline.remove(this);
            }
        } finally {
            context.fireChannelRead(msg);
        }
    }

    private static boolean startsWith(final @NotNull ByteBuf buf, final byte[] prefix) {
        val readable = buf.readableBytes();
        if (readable < prefix.length) return false;

        val idx = buf.readerIndex();
        for (int i = 0; i < prefix.length; i++) if (buf.getByte(idx + i) != prefix[i]) return false;

        return true;
    }

    @RequiredArgsConstructor(staticName = "create")
    private static final class HaProxyAddressHandler extends ChannelInboundHandlerAdapter {
        private final BiConsumer<ChannelHandlerContext, InetSocketAddress> handler;

        @Override
        public void channelRead(final @NotNull ChannelHandlerContext context, final @NotNull Object msg) {
            if (msg instanceof HAProxyMessage) {
                val haproxyMessage = (HAProxyMessage) msg;
                handler.accept(context, new InetSocketAddress(haproxyMessage.sourceAddress(), haproxyMessage.sourcePort()));
            }

            context.fireChannelRead(msg);
            context.pipeline().remove(this);
        }
    }
}
