package com.shark.netty;

import io.netty.channel.group.ChannelGroup;
public class test {
    public static void main(String[] args) {
        // ChatRoomManager 인스턴스 생성
        ChatRoomManager chatRoomManager = new ChatRoomManager();

        // 채팅방 생성
        chatRoomManager.createChatRoom("room1");

        // 채팅방 가져오기
        ChannelGroup room1 = chatRoomManager.getChatRoom("room1");

        if (room1 != null) {
            // room1 채팅방이 존재하는 경우에 수행할 작업
            System.out.println("room1 채팅방을 찾았습니다.");
        } else {
            System.out.println("room1 채팅방을 찾을 수 없습니다.");
        }
    }
}
