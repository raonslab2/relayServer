package com.shark.netty;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
* ClassName:Global
* Function: TODO ADD FUNCTION.
* @author fwz
*/
public class Global {
	public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	public static ChannelGroup gcsChannel = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	public static ChannelGroup droneChannel = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	public static ChannelGroup chatChannel = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	


}