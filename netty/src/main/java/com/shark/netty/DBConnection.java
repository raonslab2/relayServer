package com.shark.netty;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class DBConnection {
	 static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver"; 
	 
	 private static String DB_URL;
	 private static String USER;
	 private static String PASS;
	 
	 private Connection conn;
	 private Statement stmt;
 
	 DBConnection() {
		 try { 
			DB_URL = "jdbc:mariadb://192.168.0.23:3306/drone_db?serverTimezone=Asia/Seoul";
			USER = "mrdev";
			PASS = "mrdev1"; 
		    Class.forName(JDBC_DRIVER);
		    conn = DriverManager.getConnection(DB_URL,USER,PASS);
		    stmt = conn.createStatement();
		 } catch(SQLException se) {
			 se.printStackTrace();
		 } catch(Exception e) {
			 e.printStackTrace();
		 }
	 }
	 
	 public void insertData(String ip, String url, String redirect) {
		 String sql = "INSERT INTO br_connections VALUES ('" + ip + "', '" + url + "', '" + redirect + "', NOW())";
		 try {
			 stmt.executeUpdate(sql);
		 } catch (SQLException e) {
			 e.printStackTrace();
		 }
		 close();
	 }
	 
	 public void insertTemp(int tmDis) {
		 
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
		 
		 double tmLat4 = 37.205382707834745+tmDis*0.00001; 
		 double tmlog4 = 127.74773140234899+tmDis*0.00001; 
		 double tmalt4 = 50; 
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
		 
		 int st_satelite_num4 = rand.nextInt(3)+14; 
		 float st_bat_voltage4 = rand.nextFloat()+12; 
		 int st_bat_level4 = rand.nextInt(3)+5; 
		 int st_speed4 = rand.nextInt(3)+10; 
		 int st_atitude4 = rand.nextInt(3)+12;
		 String sql4 = "INSERT INTO br_drone_state (dl_id,st_satelite_num,st_bat_voltage,st_bat_level,st_speed,st_x,st_y,st_z,st_atitude)\r\n" + 
		 		"VALUES ('lm_10004',"+st_satelite_num4+","+st_bat_voltage4+","+st_bat_level4+","+st_speed4+","+tmLat4+","+tmlog4+","+tmalt4+","+st_atitude4+")";
		 
		 String sqlDel = "delete FROM br_drone_state WHERE TIMESTAMP <= DATE_SUB(DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), INTERVAL 1 MINUTE)";
		 try {
			 stmt.executeUpdate(sql);
			 stmt.executeUpdate(sql2);
			 stmt.executeUpdate(sql3);
			 stmt.executeUpdate(sqlDel);
			 
			 stmt.executeUpdate(sql4);
		 } catch (SQLException e) {
			 e.printStackTrace();
		 }
		// close();
	 }
	 
	 public String getStatusOutput() {
		 StringBuilder res = new StringBuilder();
		 String sql;
		 ResultSet rs;
		 try {
			 res.append("test: ");
			 sql = "SELECT COUNT(*) FROM br_connections";
			 rs = stmt.executeQuery(sql);
			 if (rs.next()) {
				 res.append(rs.getInt(1));
			 }
			 
			 res.append("\ntttest: ");
			 sql = "SELECT COUNT(*) FROM (SELECT * FROM br_connections GROUP BY ip, url) AS temp";
			 rs = stmt.executeQuery(sql);
			 if (rs.next()) {
				 res.append(rs.getInt(1));
			 }
			 
			 res.append("\n\ntest IP:\n");
			 sql = "SELECT ip, COUNT(*) as count, MAX(time) AS last FROM br_connections GROUP BY ip";
			 rs = stmt.executeQuery(sql);
			 while (rs.next()) { 
				 res.append("IP: " + rs.getString(1) + " ff" + rs.getString(2) + " fff: " + rs.getString(3) + "\n");
			 }
			 
			 res.append("\nffff:\n");
			 sql = "SELECT redirect, COUNT(*) FROM br_connections WHERE redirect IS NOT NULL GROUP BY redirect";
			 rs = stmt.executeQuery(sql);
			 while (rs.next()) {
				 res.append("sss: " + rs.getString(1) + " - " + rs.getString(2) + "fff\n");
			 }
		} catch (SQLException e) {
			 e.printStackTrace();
			 return "Can't get output!";
		}
		 
		 return res.toString();
	 }
	 
	 public void close() {
		 try {
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	 }

	public ResultSet selectData() {
		// TODO Auto-generated method stub 
		ResultSet rs;
		String sql;
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
		 		"		FROM br_drone_list a \r\n" + 
		 		"		 LEFT JOIN br_drone_state b \r\n" + 
		 		"			ON b.st_pk = ( select MAX(st_pk) from br_drone_state WHERE dl_id =  a.dl_id  ) \r\n" + 
		 		"		WHERE a.dl_state = 1 ";
		 try {
			rs = stmt.executeQuery(sql);
			return rs;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		 
		
	}
	 
}