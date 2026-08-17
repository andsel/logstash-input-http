package org.logstash.plugins.inputs.http.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WireLogger extends ChannelInboundHandlerAdapter {

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
            logger.debug("Read from {} : {}", ctx.channel().remoteAddress(), dump((ByteBuf) msg));
        }
        ctx.fireChannelRead(msg);
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
