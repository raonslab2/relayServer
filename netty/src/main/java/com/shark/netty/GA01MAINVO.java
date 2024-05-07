package com.shark.netty;

import java.io.Serializable;

/**
 * @Class Name : LoginVO.java
 * @Description : Login VO class
 * @Modification Information
 * @
 * @  수정일         수정자                   수정내용
 * @ -------    --------    ---------------------------
 * @ 2009.03.03    박지욱          최초 생성
 *
 *  @author 공통서비스 개발팀 박지욱
 *  @since 2009.03.03
 *  @version 1.0
 *  @see
 *  
 */
public class GA01MAINVO implements Serializable{ 
	
	private static final long serialVersionUID = -9219604963667213010L;
	
	// 사람이름 테스트
	private	String	testName ="";
	private	String	dlId ="";
	private	String	dlName ="";
	private	String	stSateliteNum ="";
	private	String	stBatVoltage ="";
	private	String	stBatLevel ="";
	private	String	stSpeed ="";
	private	String	stXa ="";
	private	String	stYa ="";
	private	String	stZa ="";
	private	String	stAtitude ="";
	
	
	public String getTestName() {
		return testName;
	}
	public void setTestName(String testName) {
		this.testName = testName;
	}
	public String getDlId() {
		return dlId;
	}
	public void setDlId(String dlId) {
		this.dlId = dlId;
	}
	public String getDlName() {
		return dlName;
	}
	public void setDlName(String dlName) {
		this.dlName = dlName;
	}
	public String getStSateliteNum() {
		return stSateliteNum;
	}
	public void setStSateliteNum(String stSateliteNum) {
		this.stSateliteNum = stSateliteNum;
	}
	public String getStBatVoltage() {
		return stBatVoltage;
	}
	public void setStBatVoltage(String stBatVoltage) {
		this.stBatVoltage = stBatVoltage;
	}
	public String getStBatLevel() {
		return stBatLevel;
	}
	public void setStBatLevel(String stBatLevel) {
		this.stBatLevel = stBatLevel;
	}
	public String getStSpeed() {
		return stSpeed;
	}
	public void setStSpeed(String stSpeed) {
		this.stSpeed = stSpeed;
	}
	public String getStXa() {
		return stXa;
	}
	public void setStXa(String stXa) {
		this.stXa = stXa;
	}
	public String getStYa() {
		return stYa;
	}
	public void setStYa(String stYa) {
		this.stYa = stYa;
	}
	public String getStZa() {
		return stZa;
	}
	public void setStZa(String stZa) {
		this.stZa = stZa;
	}
	public String getStAtitude() {
		return stAtitude;
	}
	public void setStAtitude(String stAtitude) {
		this.stAtitude = stAtitude;
	} 
	  
	
 
}
