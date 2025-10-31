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
import java.util.function.BiConsumer;

/**
 * @author milansky
 */
@RequiredArgsConstructor(staticName = "create")
public final class HAProxyProtocolSniffer extends ChannelInboundHandlerAdapter {
    private final BiConsumer<ChannelHandlerContext, InetSocketAddress> handler;

    private boolean first = true;

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext context, final @NotNull Object msg) {
        try {
            if (first && msg instanceof ByteBuf) {
                val byteBuf = (ByteBuf) msg;
                first = false;

                val pipeline = context.pipeline();

                if (looksLikeProxy(byteBuf)) {
                    pipeline.addAfter(context.name(), "gs-haproxy-decoder", new HAProxyMessageDecoder());
                    pipeline.addAfter("gs-haproxy-decoder", "gs-haproxy-handler", new HaProxyAddressHandler(handler));
                }

                pipeline.remove(this);
            }
        } finally {
            context.fireChannelRead(msg);
        }
    }

    private static boolean looksLikeProxy(final @NotNull ByteBuf buf) {
        int idx = buf.readerIndex();
        int r = buf.readableBytes();

        if (r >= 5 &&
                buf.getByte(idx) == 'P' &&
                buf.getByte(idx + 1) == 'R' &&
                buf.getByte(idx + 2) == 'O' &&
                buf.getByte(idx + 3) == 'X' &&
                buf.getByte(idx + 4) == 'Y') return true;

        return r >= 12 &&
                buf.getByte(idx) == 0x0D && buf.getByte(idx + 1) == 0x0A &&
                buf.getByte(idx + 2) == 0x0D && buf.getByte(idx + 3) == 0x0A &&
                buf.getByte(idx + 4) == 0x00 && buf.getByte(idx + 5) == 0x0D &&
                buf.getByte(idx + 6) == 0x0A && buf.getByte(idx + 7) == 'Q' &&
                buf.getByte(idx + 8) == 'U' && buf.getByte(idx + 9) == 'I' &&
                buf.getByte(idx + 10) == 'T' && buf.getByte(idx + 11) == 0x0A;
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
