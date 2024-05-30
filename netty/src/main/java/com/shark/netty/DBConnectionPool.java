package com.shark.netty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import org.json.simple.JSONObject;

public class DBConnectionPool {
	private String databaseUrl;
	private String userName;
	private String password;
	private int maxPoolSize = 10;
	private int connNum = 0;

	private static final String SQL_VERIFYCONN = "select 1";

	Stack<Connection> freePool = new Stack<Connection>();
	Set<Connection> occupiedPool = new HashSet<Connection>();

	/**
	 * Constructor
	 * 
	 * @param databaseUrl
	 *            The connection url
	 * @param userName
	 *            user name
	 * @param password
	 *            password
	 * @param maxSize
	 *            max size of the connection pool
	 */
	public DBConnectionPool(String databaseUrl, String userName,
			String password, int maxSize) {
		this.databaseUrl = databaseUrl;
		this.userName = userName;
		this.password = password;
		this.maxPoolSize = maxSize;
	}

	/**
	 * Get an available connection
	 * 
	 * @return An available connection
	 * @throws SQLException
	 *             Fail to get an available connection
	 */
	public synchronized Connection getConnection() throws SQLException {
		Connection conn = null;

		if (isFull()) {
			throw new SQLException("The connection pool is full.");
		}

		conn = getConnectionFromPool();

		// If there is no free connection, create a new one.
		if (conn == null) {
			conn = createNewConnectionForPool();
		}

		// For Azure Database for MySQL, if there is no action on one connection for some
		// time, the connection is lost. By this, make sure the connection is
		// active. Otherwise reconnect it.
		conn = makeAvailable(conn);
		return conn;
	}

	/**
	 * Return a connection to the pool
	 * 
	 * @param conn
	 *            The connection
	 * @throws SQLException
	 *             When the connection is returned already or it isn't gotten
	 *             from the pool.
	 */
	public synchronized void returnConnection(Connection conn)
			throws SQLException {
		if (conn == null) {
			throw new NullPointerException();
		}
		if (!occupiedPool.remove(conn)) {
			throw new SQLException(
					"The connection is returned already or it isn't for this pool");
		}
		freePool.push(conn);
	}

	/**
	 * Verify if the connection is full.
	 * 
	 * @return if the connection is full
	 */
	private synchronized boolean isFull() {
		return ((freePool.size() == 0) && (connNum >= maxPoolSize));
	}

	/**
	 * Create a connection for the pool
	 * 
	 * @return the new created connection
	 * @throws SQLException
	 *             When fail to create a new connection.
	 */
	private Connection createNewConnectionForPool() throws SQLException {
		Connection conn = createNewConnection();
		connNum++;
		occupiedPool.add(conn);
		return conn;
	}

	/**
	 * Crate a new connection
	 * 
	 * @return the new created connection
	 * @throws SQLException
	 *             When fail to create a new connection.
	 */
	private Connection createNewConnection() throws SQLException {
		Connection conn = null;
		conn = DriverManager.getConnection(databaseUrl, userName, password);
		return conn;
	}

	/**
	 * Get a connection from the pool. If there is no free connection, return
	 * null
	 * 
	 * @return the connection.
	 */
	private Connection getConnectionFromPool() {
		Connection conn = null;
		if (freePool.size() > 0) {
			conn = freePool.pop();
			occupiedPool.add(conn);
		}
		return conn;
	}

	/**
	 * Make sure the connection is available now. Otherwise, reconnect it.
	 * 
	 * @param conn
	 *            The connection for verification.
	 * @return the available connection.
	 * @throws SQLException
	 *             Fail to get an available connection
	 */
	private Connection makeAvailable(Connection conn) throws SQLException {
		if (isConnectionAvailable(conn)) {
			return conn;
		}

		// If the connection is't available, reconnect it.
		occupiedPool.remove(conn);
		connNum--;
		conn.close();

		conn = createNewConnection();
		occupiedPool.add(conn);
		connNum++;
		return conn;
	}

	/**
	 * By running a sql to verify if the connection is available
	 * 
	 * @param conn
	 *            The connection for verification
	 * @return if the connection is available for now.
	 */
	private boolean isConnectionAvailable(Connection conn) {
		try (Statement st = conn.createStatement()) {
			st.executeQuery(SQL_VERIFYCONN);
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
	
	public ResultSet selectData(Statement stmt) {
		// TODO Auto-generated method stub 
		ResultSet rs;
		 String sqlDel = "delete FROM br_drone_state WHERE TIMESTAMP <= DATE_SUB(DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), INTERVAL 1 MINUTE)";
		 try {
			 stmt.executeUpdate(sqlDel);			  
		 } catch (SQLException e) {
			 e.printStackTrace();
		 }
		 
		String sql;
		/*
		 sql = "SELECT a.dl_id \r\n" + 
		 		"		,a.dl_name \r\n" + 
		 		"		,b.st_satelite_num \r\n" + 
		 		"		,b.st_bat_voltage \r\n" + 
		 		"		,b.st_bat_level \r\n" + 
		 		"		,b.st_speed \r\n" + 
		 		"		,b.st_x \r\n" + 
		 		"		,b.st_y \r\n" + 
		 		"		,b.st_z \r\n" + 
		 		"		,b.st_atitude \r\n" + 
		 		"		,b.st_roll \r\n" + 
		 		"		,b.st_pitch \r\n" + 
		 		"		,b.st_yaw \r\n" + 
		 		"		,b.st_head \r\n" + 
		 		"		,b.st_state \r\n" + 
		 		"		,b.st_mode \r\n" + 
		 		"		,b.st_arm \r\n" + 
		 		"		,b.st_time \r\n" + 
		 		"		FROM br_drone_list a \r\n" + 
		 		"		 LEFT JOIN br_drone_state b \r\n" + 
		 		"			ON b.st_pk = ( select MAX(st_pk) from br_drone_state WHERE dl_id =  a.dl_id  ) \r\n" + 
		 		"		WHERE a.dl_state = 1 ";
		 */
		 sql ="SELECT \r\n"
		 		+ "    a.ai_devicename AS dl_id\r\n"
		 		+ "   ,a.ai_devicename AS dl_name\r\n"
		 		+ " 		,b.st_satelite_num \r\n"
		 		+ "		,b.st_bat_voltage \r\n"
		 		+ "		,b.st_bat_level \r\n"
		 		+ "		,b.st_speed \r\n"
		 		+ "		,b.st_x \r\n"
		 		+ "		,b.st_y \r\n"
		 		+ "		,b.st_z \r\n"
		 		+ "		,b.st_atitude \r\n"
		 		+ "		,b.st_roll \r\n"
		 		+ "		,b.st_pitch \r\n"
		 		+ "		,b.st_yaw \r\n"
		 		+ "		,b.st_head \r\n"
		 		+ "		,b.st_state \r\n"
		 		+ "		,b.st_mode \r\n"
		 		+ "		,b.st_arm \r\n"
		 		+ "		,b.st_time \r\n"
		 		+ " FROM br_tb_aircraft a \r\n"
		 		+ " 		 LEFT JOIN br_drone_state b \r\n"
		 		+ "			ON b.st_pk = ( select MAX(st_pk) from br_drone_state WHERE dl_id =  a.ai_devicename  ) \r\n"
		 		+ "		WHERE a.ai_use = 'Y' ";
		 try {
 
			rs = stmt.executeQuery(sql);
			return rs;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			
			System.out.println(e.toString());
			return null;
		}
		 
		
	}
	

	 public void insertTemp(Statement stmt,int tmDis) {
		 
		 //test
		 Random rand = new Random();
		 int st_satelite_num = rand.nextInt(3)+14; 
		 float st_bat_voltage = rand.nextFloat()+12; 
		 int st_bat_level = rand.nextInt(3)+5; 
		 int st_speed = rand.nextInt(3)+10; 
		 int st_atitude = rand.nextInt(3)+12; 

		 double tmLat = 37.205082707834745+tmDis*0.00001; 
		 double tmlog = 127.74743140234899+tmDis*0.00001; 
		 double tmalt = 50; 
		  
		 
		 double tmLat2 = 37.205182707834745+tmDis*0.00001; 
		 double tmlog2 = 127.74753140234899+tmDis*0.00001; 
		 double tmalt2 = 50; 
		 
		 double tmLat3 = 37.205282707834745+tmDis*0.00001; 
		 double tmlog3 = 127.74763140234899+tmDis*0.00001; 
		 double tmalt3 = 50; 
		 
 
		 String sql = "INSERT INTO br_drone_state (dl_id,st_satelite_num,st_bat_voltage,st_bat_level,st_speed,st_x,st_y,st_z,st_atitude)\r\n" + 
		 		"VALUES ('lm_10001',"+st_satelite_num+","+st_bat_voltage+","+st_bat_level+","+st_speed+","+tmLat+","+tmlog+","+tmalt+","+st_atitude+")";
		 
		 int st_satelite_num2 = rand.nextInt(3)+14; 
		 float st_bat_voltage2 = rand.nextFloat()+12; 
		 int st_bat_level2 = rand.nextInt(3)+5; 
		 int st_speed2 = rand.nextInt(3)+10; 
		 int st_atitude2 = rand.nextInt(3)+12;
		 String sql2 = "INSERT INTO br_drone_state (dl_id,st_satelite_num,st_bat_voltage,st_bat_level,st_speed,st_x,st_y,st_z,st_atitude)\r\n" + 
		 		"VALUES ('lm_10002',"+st_satelite_num2+","+st_bat_voltage2+","+st_bat_level2+","+st_speed2+","+tmLat2+","+tmlog2+","+tmalt2+","+st_atitude2+")";
		 
		 int st_satelite_num3 = rand.nextInt(3)+14; 
		 float st_bat_voltage3 = rand.nextFloat()+12; 
		 int st_bat_level3 = rand.nextInt(3)+5; 
		 int st_speed3 = rand.nextInt(3)+10; 
		 int st_atitude3 = rand.nextInt(3)+12;
		 String sql3 = "INSERT INTO br_drone_state (dl_id,st_satelite_num,st_bat_voltage,st_bat_level,st_speed,st_x,st_y,st_z,st_atitude)\r\n" + 
		 		"VALUES ('lm_10003',"+st_satelite_num3+","+st_bat_voltage3+","+st_bat_level3+","+st_speed3+","+tmLat3+","+tmlog3+","+tmalt3+","+st_atitude3+")";
		 
		 String sqlDel = "delete FROM br_drone_state WHERE TIMESTAMP <= DATE_SUB(DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), INTERVAL 1 MINUTE)";
		 try {
			 stmt.executeUpdate(sql);
			 stmt.executeUpdate(sql2);
			 stmt.executeUpdate(sql3);
			 stmt.executeUpdate(sqlDel);
			  
		 } catch (SQLException e) {
			 e.printStackTrace();
		 }
		// close();
	 }
	 
 public void insertDroneState(Statement stmt,JSONObject jsonState) {
		 
 
		 int st_sat = Integer.parseInt(jsonState.get("st_sat").toString());  
		 float st_bat_v = Float.valueOf(jsonState.get("st_bat_v").toString());  
		 int st_bat_l = Integer.parseInt(jsonState.get("st_bat_l").toString());  
		 int st_speed = Integer.parseInt(jsonState.get("st_speed").toString());  
		 int st_atitude = Integer.parseInt(jsonState.get("st_atitude").toString()); 

		 double tmLat = Double.valueOf(jsonState.get("st_x").toString());  
		 double tmlog = Double.valueOf(jsonState.get("st_y").toString()); 
		 double tmalt = Double.valueOf(jsonState.get("st_z").toString()); 
		 
		 String dl_id = jsonState.get("dl_id").toString();
 
 
		 String sql = "INSERT INTO br_drone_state (dl_id,st_satelite_num,st_bat_voltage,st_bat_level,st_speed,st_x,st_y,st_z,st_atitude)\r\n" + 
		 		"VALUES ('"+dl_id+"',"+st_sat+","+st_bat_v+","+st_bat_l+","+st_speed+","+tmLat+","+tmlog+","+tmalt+","+st_atitude+")";
		 
		 try {
			 stmt.executeUpdate(sql); 
			  
		 } catch (SQLException e) {
			 e.printStackTrace();
		 }
		// close();
	 }
	
	// Just an Example
	public static void main(String[] args) throws SQLException {
		Connection conn = null;
		DBConnectionPool pool = new DBConnectionPool( 
				"jdbc:mariadb://127.0.0.1:3306/sepm_db?serverTimezone=Asia/Seoul",
				"mrdev", "mrdev1", 2);
		try {
			conn = pool.getConnection();
			try (Statement stmt = conn.createStatement())
			{
	 
				 
				ResultSet res = pool.selectData(stmt); 
				while (res.next()) {
					String tblName = res.getString(1);
					System.out.println(tblName);
				}
			}
		}
		 finally {
			if (conn != null) {
				pool.returnConnection(conn);
			}
		}
	}

}