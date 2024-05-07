/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.shark.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil; 
 
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map; 

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Echoes uppercase content of text frames.
 */
public class NettySocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> { 
	private WebSocketServerHandshaker handshaker;
	private JSONObject jsonState;
	
    private Connection conn; 
    private DBConnectionPool pool; 
    private Statement stmt;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NettySocketFrameHandler.class);
    
    //public static HashMap<String, Channel> hm = new HashMap<String, Channel>(); 
    //  map
    public  static Map<String, ChannelHandlerContext> hm = new HashMap<String, ChannelHandlerContext>();
    public  static Map<String, ChannelHandlerContext> hmGroup = new HashMap<String, ChannelHandlerContext>();
    public  static ChatRoomManager chatRoomManager = new ChatRoomManager();
    public  static ChannelGroup room;
 

    public NettySocketFrameHandler(Connection conn,DBConnectionPool pool,Statement stmt) {
        this.conn = conn; 
        this.pool = pool;  
        this.stmt = stmt;   
    }
	   
	/**
	 * Channel channel action is active When the client actively connects to the
	 * server link, this channel is active. That is, the client and the server have
	 * established a communication channel and can transmit data
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// Add to 
		Global.group.add(ctx.channel());   
		LOGGER.debug("The connection between the client and the server is opened: {}", ctx.channel().remoteAddress().toString());
	}

	/**
	 * channel Inactive When the client actively disconnects from the server, this
	 * channel is inactive. In other words, the client and server have closed the
	 * communication channel and cannot transmit data
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// Remove
		Global.group.remove(ctx.channel()); 
		Global.gcsChannel.remove(ctx.channel()); 
		Global.droneChannel.remove(ctx.channel()); 
		Global.chatChannel.remove(ctx.channel()); 
 
		LOGGER.debug("The connection between the client and the server is closed: {}", ctx.channel().remoteAddress().toString());
	}

	/**
	 * Receive messages sent by the client channel channel Read In short, it reads
	 * data from the channel, that is, the server receives the data sent by the
	 * client. But this data is of type ByteBuf when it is not decoded
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// Traditional HTTP access
		if (msg instanceof FullHttpRequest) {
			handleHttpRequest(ctx, ((FullHttpRequest) msg));
			// WebSocket access
		} else if (msg instanceof WebSocketFrame) { 
			if ("anzhuo".equals(ctx.channel().attr(AttributeKey.valueOf("type")).get())) {
				handlerWebSocketFrame(ctx, (WebSocketFrame) msg);
			} else {
				handlerWebSocketFrame2(ctx, (WebSocketFrame) msg);
			}
		}
	}

	/**
	 * Channel channel Read Read Complete is completed After the channel read is
	 * completed, it will be notified in this method, and the corresponding refresh
	 * operation can be done ctx.flush()
	 */
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
		// To determine whether to close the link
		if (frame instanceof CloseWebSocketFrame) { 
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
			return;
		}
		// Determine whether to ping the message
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		// This example only supports text messages, not binary messages
		if (!(frame instanceof TextWebSocketFrame)) { 
			LOGGER.debug("This example only supports text messages, not binary messages");
			throw new UnsupportedOperationException(
					String.format("%s frame types not supported", frame.getClass().getName()));
		}
		// Reply message
		String request = ((TextWebSocketFrame) frame).text();
		LOGGER.debug("request: {}", request);
		TextWebSocketFrame tws = new TextWebSocketFrame(request);
		// Group posting
		Global.group.writeAndFlush(tws);
		// Back [Who sent it to whom]
		// ctx.channel().writeAndFlush(tws);
	}

	private void handlerWebSocketFrame2(ChannelHandlerContext ctx, WebSocketFrame frame) {
	    if (frame instanceof CloseWebSocketFrame) {
	        handleCloseFrame(ctx, (CloseWebSocketFrame) frame);
	    } else if (frame instanceof PingWebSocketFrame) {
	        handlePingFrame(ctx, frame);
	    } else if (frame instanceof TextWebSocketFrame) {
	        handleTextFrame(ctx, (TextWebSocketFrame) frame);
	    } else {
	        handleUnsupportedFrame(frame);
	    }
	}

	private void handleCloseFrame(ChannelHandlerContext ctx, CloseWebSocketFrame frame) {
	    handshaker.close(ctx.channel(), frame.retain());
	}

	private void handlePingFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
	    ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
	}

	private void handleTextFrame(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
	    String request = frame.text();
	    try {
	        JSONParser jsonParser = new JSONParser();
	        JSONObject jsonObj = (JSONObject) jsonParser.parse(request);
	        String dataRequest = (String) jsonObj.get("DATA_REQUEST");

	        if (dataRequest != null) {
	            switch (dataRequest) {
	                case "OPEN":
	                    handleOpenRequest(ctx, jsonObj);
	                    break;
	                case "DRONE_STATE":
	                    handleDroneStateRequest(ctx, jsonObj);
	                    break;
	                case "DRONE_GPS":
	                    handleDroneGPSRequest(ctx, jsonObj);
	                    break;
	                case "DRONE_PATH":
	                    handleDronePathRequest(ctx, jsonObj);
	                    break;
	                case "CHAT":
	                    handleChatRequest(ctx, jsonObj);
	                    break;
	                default:
	                    handleUndefinedRequest(dataRequest);
	            }
	        } else {
	            LOGGER.debug("No DATA_REQUEST field found in the request.");
	        }
	    } catch (ParseException e) {
	        LOGGER.debug("ParseException error: {}", request);
	    }
	}

	private void handleOpenRequest(ChannelHandlerContext ctx, JSONObject jsonObj) {
	    String dataGubun = (String) jsonObj.get("DATA_GUBUN");
	    if (dataGubun != null) {
	        switch (dataGubun) {
	            case "GCS":
	                Global.gcsChannel.add(ctx.channel());
	                break;
	            case "DRONE":
	                Global.droneChannel.add(ctx.channel());
	                NettySocketFrameHandler.hm.put((String) jsonObj.get("DATA_DRONE_ID"), ctx);
	                break;
	            case "USER":
	                handleUserRequest(ctx, jsonObj);
	                break;
	            default:
	                LOGGER.debug("Unhandled DATA_GUBUN: {}", dataGubun);
	        }
	    } else {
	        LOGGER.debug("No DATA_GUBUN field found in the request.");
	    }
	}

	private void handleUserRequest(ChannelHandlerContext ctx, JSONObject jsonObj) {
	    //Global.chatChannel.add(ctx.channel());
	    NettySocketFrameHandler.hmGroup.put((String) jsonObj.get("DATA_ID"), ctx);
	    chatRoomManager.createChatRoom((String) jsonObj.get("TW_PK"));
	    room = chatRoomManager.getChatRoom((String) jsonObj.get("TW_PK"));
	    // 클라이언트가 방에 이미 있는지 확인
	    room.add(ctx.channel());
	    LOGGER.debug("ctx.channel: {}", ctx.channel());
	}

	private void handleDroneStateRequest(ChannelHandlerContext ctx, JSONObject jsonObj) {
	    TextWebSocketFrame tws = new TextWebSocketFrame(jsonObj.toJSONString());
	    Global.gcsChannel.writeAndFlush(tws);
	}

	private void handleDroneGPSRequest(ChannelHandlerContext ctx, JSONObject jsonObj) {
	    JSONObject jsonState = (JSONObject) jsonObj.get("DATA_STATE");
	    pool.insertDroneState(stmt, jsonState);
	    ChannelHandlerContext tmChannel = hm.get(jsonState.get("dl_id"));
	    TextWebSocketFrame tws = new TextWebSocketFrame(jsonState.toString());
	    if (tmChannel != null) {
	        tmChannel.channel().writeAndFlush(tws);
	    }
	}

	private void handleDronePathRequest(ChannelHandlerContext ctx, JSONObject jsonObj) {
	    String gcsPath = (String) jsonObj.get("DATA_GUBUN");
	    JSONObject jsonState = (JSONObject) jsonObj.get("DATA_STATE");
	    if ("GCS".equals(gcsPath)) {
	        String tmDlName = (String) jsonState.get("dlName");
	        String[] tmDlNameArray = tmDlName.split(",");
	        for (String dlName : tmDlNameArray) {
	            ChannelHandlerContext tmChannel = hm.get(dlName);
	            if (tmChannel != null) {
	                TextWebSocketFrame tws = new TextWebSocketFrame(jsonState.toJSONString());
	                tmChannel.channel().writeAndFlush(tws);
	            }
	        }
	        LOGGER.debug("GCS DATA_PATH: {}", jsonState);
	    }
	}

	private void handleChatRequest(ChannelHandlerContext ctx, JSONObject jsonObj) {
	    JSONObject jsonState = (JSONObject) jsonObj.get("DATA_STATE");
	    String tmDlName = (String) jsonState.get("dlName");
	    String[] tmDlNameArray = tmDlName.split(",");
	    String dlAction =(String) jsonState.get("dlAction");
	    if(dlAction!=null&&dlAction!="null"&&dlAction!=""&&dlAction.length()>0) {
		    room = chatRoomManager.getChatRoom((String) jsonState.get("dlPk"));
		    for (Channel channel : room) {
		        ChannelHandlerContext ctx2 = channel.pipeline().firstContext();
		        if(!ctx.channel().id().equals(ctx2.channel().id())) {
			        TextWebSocketFrame tws = new TextWebSocketFrame(jsonState.toJSONString());
			        ctx2.channel().writeAndFlush(tws);
		        } 
		        LOGGER.debug("ctx.channel(): {}", ctx.channel().id());
		        LOGGER.debug("ctx2.channel(): {}", ctx2.channel().id());
		    }
		    LOGGER.debug("CHAT: {}", jsonState);
	    } 
	}

	private void handleUndefinedRequest(String dataRequest) {
	    LOGGER.debug("Undefined DATA_REQUEST: {}", dataRequest);
	}

	private void handleUnsupportedFrame(WebSocketFrame frame) {
	    LOGGER.debug("This example only supports text messages, not binary messages");
	    throw new UnsupportedOperationException(
	            String.format("%s frame types not supported", frame.getClass().getName()));
	}


	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
		// If HTTP decoding fails, return HHTP exception
		if (!req.decoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
			sendHttpResponse(ctx, req,
					new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
			return;
		}
		// Get URL post parameters
		String uri = req.uri();

		// Construct a handshake response and return, test on this machine
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
				"ws://localhost:8081/websocket", null, false);
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			handshaker.handshake(ctx.channel(), req);
		}
	}

	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
		// Return the response to the client
		if (res.status().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
			
			assert buf.refCnt() == 1;

			buf.retain();
			assert buf.refCnt() == 2;

			boolean destroyed = buf.release();
			assert !destroyed;
			assert buf.refCnt() == 1;
 

		}
		// If it is not Keep-Alive, close the connection
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	/**
	 * exception Caught Caught catch the exception, when an exception occurs, you
	 * can do some corresponding processing, such as printing the log, closing the
	 * link
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
		// ping and pong frames already handled

		if (frame instanceof TextWebSocketFrame) {
			// Send the uppercase string back.
			String request = ((TextWebSocketFrame) frame).text();
			ctx.channel().writeAndFlush(new TextWebSocketFrame(request.toUpperCase(Locale.US)));
		} else {
			String message = "unsupported frame type: " + frame.getClass().getName();
			throw new UnsupportedOperationException(message);
		}
	}

}
