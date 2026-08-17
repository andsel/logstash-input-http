package org.logstash.plugins.inputs.http.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WireLogger extends ChannelDuplexHandler {

    private final static Logger logger = LogManager.getLogger(WireLogger.class);

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
            logger.debug("<< {} : {}", ctx.channel().remoteAddress(), dump((ByteBuf) msg));
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (logger.isDebugEnabled() && msg instanceof ByteBuf) {
            logger.debug(">> {} : {}", ctx.channel().remoteAddress(), dump((ByteBuf) msg));
        }
        ctx.write(msg, promise);
    }

    private static String dump(ByteBuf buf) {
        StringBuilder sb = new StringBuilder();
        for (int i = buf.readerIndex(), end = buf.readerIndex() + buf.readableBytes(); i < end; i++) {
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
        return sb.toString();
    }
}
