package org.logstash.plugins.inputs.http.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WireLogger extends ChannelDuplexHandler {

    private final static Logger logger = LogManager.getLogger(WireLogger.class);

    static final int MAX_DUMP_LENGTH = 4096;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Opening connection {}", ctx.channel().remoteAddress());
        }
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Closing connection {}", ctx.channel().remoteAddress());
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (logger.isDebugEnabled() && msg instanceof ByteBuf) {
            logger.debug("<< {} : {}", ctx.channel().remoteAddress(), dump((ByteBuf) msg, MAX_DUMP_LENGTH));
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (logger.isDebugEnabled() && msg instanceof ByteBuf) {
            logger.debug(">> {} : {}", ctx.channel().remoteAddress(), dump((ByteBuf) msg, MAX_DUMP_LENGTH));
        }
        ctx.write(msg, promise);
    }

    // maxLength == 0 means unlimited; any other value caps the dumped bytes and appends a truncation notice.
    static String dump(ByteBuf buf, int maxLength) {
        int total = buf.readableBytes();
        int limit = (maxLength == 0) ? total : Math.min(maxLength, total);

        StringBuilder sb = new StringBuilder();
        for (int i = buf.readerIndex(), end = buf.readerIndex() + limit; i < end; i++) {
            byte b = buf.getByte(i);
            if (b == '\r') {
                sb.append("[\\r]");
            } else if (b == '\n') {
                sb.append("[\\n]");
            } else if (b >= 0x20 && b < 0x7F) {
                sb.append((char) b);
            } else {
                sb.append(String.format("[0x%02X]", b & 0xFF));
            }
        }

        if (maxLength > 0 && total > maxLength) {
            sb.append("...[truncated, total ").append(total).append(" bytes]");
        }

        return sb.toString();
    }
}
