package com.sunsheen.hkks.database.mgr.entity;

import java.util.Date;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

/**
 * 
 * @ClassName: DataBaseEntity  
 * @Description: 数据库配置信息  
 * @author FengTao
 * @date 2020年7月7日  
 *
 */
@Entity
@Table(name = "DATABASE_CONFIG")
public class DataBaseEntity {
	@Id
	@Column(name = "CONNECTION_ID")
	@GeneratedValue(strategy = GenerationType.AUTO,generator = "UUID") //AUTO代表程序控制主键
	@GenericGenerator(name = "UUID", strategy = "uuid")
	private String connectionId ;
	
	@Column(name = "CONNECTION_NAME")
	private String connectionName ;
	
	@Column(name = "DATABASE_NAME")
	private String databaseName ;
	
	@Column(name = "DATABASE_TYPE")
	private String databaseType ;
	
	@Column(name = "DRIVER")
	private String driver ;
	
	@Column(name = "IP")
	private String ip ;
	
	@Column(name = "PORT")
	private String port ;
	
	@Column(name = "USERNAME")
	private String username ;
	
	@Column(name = "PASSWORD")
	private String password ;
	
	@Column(name = "SERVICE_TYPE")
	private String serviceType ;
	
	@Column(name = "SERVICE_OTHER")
	private String serviceOther ;
	
	@Column(name = "MEMO")
	private String memo ;
	
	@Column(name = "LAST_UPDATED_USER_ID")
	private String lastUpdateUserId ;
	
	@Column(name = "LAST_UPDATED_USER_NAME")
	private String lastUpdateUserName ;
	
	@Column(name = "LAST_UPDATED_DATE")
	private Date lastUpdateDate ;
	
	@Column(name = "STATE")
	private String state ;

	public String getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}

	public String getConnectionName() {
		return connectionName;
	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getServiceOther() {
		return serviceOther;
	}

	public void setServiceOther(String serviceOther) {
		this.serviceOther = serviceOther;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}


	public String getLastUpdateUserId() {
		return lastUpdateUserId;
	}

	public void setLastUpdateUserId(String lastUpdateUserId) {
		this.lastUpdateUserId = lastUpdateUserId;
	}

	public String getLastUpdateUserName() {
		return lastUpdateUserName;
	}

	public void setLastUpdateUserName(String lastUpdateUserName) {
		this.lastUpdateUserName = lastUpdateUserName;
	}

	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "DataBaseEntity [connectionId=" + connectionId
				+ ", connectionName=" + connectionName + ", databaseName="
				+ databaseName + ", databaseType=" + databaseType + ", driver="
				+ driver + ", ip=" + ip + ", port=" + port + ", username="
				+ username + ", password=" + password + ", serviceType="
				+ serviceType + ", serviceOther=" + serviceOther + ", memo="
				+ memo + ", lastUpdateUserId=" + lastUpdateUserId
				+ ", lastUpdateUserName=" + lastUpdateUserName
				+ ", lastUpdateDate=" + lastUpdateDate + "]";
	}

	
}
