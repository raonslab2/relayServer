/*
 * Copyright 2014 The Netty Project
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

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
 
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
 

/**
 * This is an example of a WebSocket client.
 * <p>
 * In order to run this example you need a compatible WebSocket server.
 * Therefore you can either start the WebSocket server from the examples
 * by running {@link io.netty.example.http.websocketx.server.WebSocketServer}
 * or connect to an existing WebSocket server such as
 * <a href="https://www.websocket.org/echo.html">ws://echo.websocket.org</a>.
 * <p>
 * The client will attempt to connect to the URI passed to it as the first argument.
 * You don't have to specify any arguments if you want to connect to the example WebSocket server,
 * as this is the default.
 */
public final class WebCall implements Runnable{ 
    static final String URL = System.getProperty("url", "ws://127.0.0.1:5010/websocket");
    
	private Connection conn;
	private DBConnectionPool pool;
	private Statement stmt;
	private int port;
	private String websocketUrl;
	
	public WebCall(Connection conn,DBConnectionPool pool,Statement stmt,int port,String websocketUrl) {
        this.conn = conn; 
        this.pool = pool; 
        this.stmt = stmt; 
        this.port = port; 
        this.websocketUrl = websocketUrl;
	}
	
	public void run() { 

        URI uri = null;
		try {
			uri = new URI(URL);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String scheme = uri.getScheme() == null? "ws" : uri.getScheme();
        final String host = uri.getHost() == null? "127.0.0.1" : uri.getHost();
        final int port;
        if (uri.getPort() == -1) {
            if ("ws".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("wss".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }

        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            System.err.println("Only WS(S) is supported.");
            return;
        }

  

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
            // If you change it to V00, ping is not supported and remember to change
            // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
            final WebCallHandler handler =
                    new WebCallHandler(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders())
                            ,group,port);

            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ChannelPipeline p = ch.pipeline();
                     p.addLast(
                             new HttpClientCodec(),
                             new HttpObjectAggregator(8192),
                             WebSocketClientCompressionHandler.INSTANCE,
                             handler);
                 }
             });

            Channel ch = b.connect(uri.getHost(), port).sync().channel();
            handler.handshakeFuture().sync();

           // BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("DATA_GUBUN", "DRONE_STATE");
			jsonObject.put("DATA_REQUEST", "DRONE_STATE");
			
			JSONObject jsonObj1;
			Map<String, Object> param = null; 
			int di = 0; 
			   
			
			ResultSet rs;
			
			try { 
				 
				 while (true) { 
		            	rs = pool.selectData(stmt);
		            	if(rs != null) {
			             	 int si = 0;
			              	 JSONArray jsonArr1 = new JSONArray(); 
			   			    try {
								while (rs.next()) {
				     				jsonObj1 = new JSONObject();
				   	   				jsonObj1.put("dl_idx", si);
				   	  				jsonObj1.put("dl_id", rs.getString(1));
				   	   				jsonObj1.put("dl_name", rs.getString(2));
				   	   				jsonObj1.put("st_satelite_num", rs.getString(3));
				   	   				jsonObj1.put("st_bat_voltage", rs.getString(4));
				   	   				jsonObj1.put("st_bat_level", rs.getString(5));
				   	   				jsonObj1.put("st_speed", rs.getString(6));
				   	   				jsonObj1.put("st_x", rs.getString(7));
				   	   				jsonObj1.put("st_y", rs.getString(8));
				   	   			    jsonObj1.put("st_z", rs.getString(10) );
				   	   				jsonObj1.put("st_atitude", rs.getString(9));
				   	   			    jsonObj1.put("st_roll", rs.getString(11));
				   	   		        jsonObj1.put("st_pitch", rs.getString(12));
				   	   	            jsonObj1.put("st_yaw", rs.getString(13));
				   	   	            jsonObj1.put("st_head", rs.getString(14));
				   	   	            jsonObj1.put("st_state", rs.getString(15));
				   	   	            jsonObj1.put("st_mode", rs.getString(16));
				   	   	            jsonObj1.put("st_arm", rs.getString(17));
				   	   	            jsonObj1.put("st_time", rs.getString(18));
				   	   				jsonArr1.add(jsonObj1); 
				   	   				
				   	   				si++;
						 
								}
							} catch (SQLException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
			   			  
			 	   			 if(jsonArr1!=null && jsonArr1.size()>0) {
				 	 	   			jsonObject.put("DATA_STATE", jsonArr1);
				 		   			
				 	            	String msg = jsonObject.toString();
				 	                WebSocketFrame frame = new TextWebSocketFrame(msg);
				 	                ch.writeAndFlush(frame);
				 	   			 }
		            	}else {
		            		Thread.sleep(1000);
		        			conn = pool.getConnection();
		        			stmt = conn.createStatement();
		            	}
 
		 	   			
		 	   		    di = di +1;
		 	   	  
						Thread.sleep(1000);

		            }
			
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (conn != null) {
					try {
						pool.returnConnection(conn);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} 
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
        	try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            group.shutdownGracefully();
            System.out.println("group.shutdownGracefully()"); 
            //new WebCall(port).run();
            
    		Runnable task2 = new WebCall(conn,pool,stmt,port,websocketUrl);
    		Thread thread2 = new Thread(task2);
    		thread2.start();
        }
    
	} 

    public static void main(String[] args) throws Exception {
		int port;
		String websocketUrl;
        if (args.length > 0) {
        	websocketUrl = System.getProperty("url", "ws://"+args[0]+":5010/websocket");
            port = Integer.parseInt(args[1]);
        } else {
        	websocketUrl = System.getProperty("url", "ws://127.0.0.1:5010/websocket");
            port = 5010;
        }
        
		Connection conn = null;
		DBConnectionPool pool = new DBConnectionPool(
				"jdbc:mariadb://127.0.0.1:3306/sepm_db?serverTimezone=Asia/Seoul",
				"mrdev", "mrdev1", 2);
		Statement stmt = null;
		try {
 
			conn = pool.getConnection();
			stmt = conn.createStatement();
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		if(conn!=null) {
			Runnable task = new WebCall(conn,pool,stmt,port,websocketUrl);
			Thread thread = new Thread(task);
			thread.start();
		}else {
			System.out.println("db conn is null");
			
		}

         
    	
    }
}
