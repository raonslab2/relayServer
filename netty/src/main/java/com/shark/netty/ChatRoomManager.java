package com.shark.netty;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChatRoomManager {
	protected Map<String, ChannelGroup> chatRooms = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(NettySocketFrameHandler.class);

 
    
    public void createChatRoom(String roomName) {
        if (!chatRooms.containsKey(roomName)) {
            ChannelGroup newRoom = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            chatRooms.put(roomName, newRoom);
        } else {
            System.out.println("채팅방 " + roomName + "은 이미 존재합니다.");
            LOGGER.debug(" createChatRoom exist: {}", roomName);
        }
    }

    public void deleteChatRoom(String roomName) {
        ChannelGroup room = chatRooms.get(roomName);
        if (room != null) {
            room.close();
            chatRooms.remove(roomName);
        }
    }
    
    protected ChannelGroup retrieveChatRoom(String roomName) {
        return chatRooms.get(roomName);
    }

    public ChannelGroup getChatRoom(String roomName) {
        return chatRooms.get(roomName);
    }
}
