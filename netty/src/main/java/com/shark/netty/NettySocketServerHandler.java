package com.shark.netty;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
public class NettySocketServerHandler extends ChannelInboundHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(NettySocketServerHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
		String readMessage = ((ByteBuf) msg).toString(Charset.forName("UTF8"));
        ctx.write(msg); 
        //LOGGER.debug("sever receive:",readMessage);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}