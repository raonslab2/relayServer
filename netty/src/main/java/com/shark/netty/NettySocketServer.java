package com.shark.netty;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NettySocketServer implements Runnable {
    private Connection conn; 
    private DBConnectionPool pool;
    private Statement stmt;
    private int port; 
    private static final Logger LOGGER = LoggerFactory.getLogger(NettySocketServer.class);
    public NettySocketServer(Connection conn,DBConnectionPool pool,Statement stmt,int port) {
        this.conn = conn; 
        this.pool = pool; 
        this.stmt = stmt; 
        this.port = port;
    } 
    
    
    public void run() {
    	
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(
            		 new NettySocketServerInitializer(conn,pool,stmt,port)
            		 );

            Channel ch = b.bind(port).sync().channel();
 

            ch.closeFuture().sync();
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        } 
    }
    
    public static void main(String[] args) throws Exception {
		int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 5010;
        }
        
		Connection conn = null;
		DBConnectionPool pool = new DBConnectionPool( 
				"jdbc:mariadb://127.0.0.1:3306/sepm_db?serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true&useSSL=false",
				"mrdev", "mrdev1", 2);
		Statement stmt = null;
		try {
			conn = pool.getConnection();
			stmt = conn.createStatement();
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
        
		Runnable task1 = new NettySocketServer(conn,pool,stmt,port);
		Thread thread1 = new Thread(task1);
		thread1.start();
		System.out.println("Websocket Server Start");
	 
	}
}