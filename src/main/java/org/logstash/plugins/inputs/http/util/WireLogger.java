package org.logstash.plugins.inputs.http.util;

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
}
