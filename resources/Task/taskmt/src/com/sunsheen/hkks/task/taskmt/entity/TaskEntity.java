package com.sunsheen.hkks.task.taskmt.entity;

import java.util.Date;

import javax.persistence.* ;

import org.hibernate.annotations.GenericGenerator;

/**
 * 
 * @Title: TaskEntity
 * @Description: 任务表实体类
 * @author: FengTao
 * @date 2020年7月13日 下午2:12:53
 */
@Entity
@Table(name = "TASK_INFO")
public class TaskEntity {
	@Id
	@Column(name = "TASK_ID")
	@GeneratedValue(strategy = GenerationType.AUTO,generator = "UUID") //AUTO代表程序控制主键
	@GenericGenerator(name = "UUID", strategy = "uuid")
	private String taskId ;
	
	@Column(name = "TASK_NAME" , nullable = false)
	private String taskName ;
	
	@Column(name = "TYPE_CODE" , nullable = false)
	private String typeCode ;
	
	@Column(name = "START_TIME" , nullable = false)
	private Date startTime ;
	
	@Column(name = "END_TIME" , nullable = false)
	private Date endTime ;
	
	@Column(name = "CONNECTION_ID" , nullable = false)
	private String connectionId ;
	
	@Column(name = "MEMO")
	private String memo ;
	
	@Column(name = "STATE_CODE" , nullable = false)
	private String stateCode ;
	
	@Column(name = "LAST_UPDATED_USER_ID")
	private String lastUpdateUserId ;
	
	@Column(name = "LAST_UPDATED_USER_NAME")
	private String lastUpdateUserName ;
	
	@Column(name = "LAST_UPDATED_DATE")
	private Date lastUpdateDate ;

	@Column(name = "MAP_STATE_CODE")
	private String mapStateCode ;
	
	
	
	public String getMapStateCode() {
		return mapStateCode;
	}

	public void setMapStateCode(String mapStateCode) {
		this.mapStateCode = mapStateCode;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getStateCode() {
		return stateCode;
	}

	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
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

	@Override
	public String toString() {
		return "TaskEntity [taskId=" + taskId + ", taskName=" + taskName
				+ ", typeCode=" + typeCode + ", startTime=" + startTime
				+ ", endTime=" + endTime + ", connectionId=" + connectionId
				+ ", memo=" + memo + ", stateCode=" + stateCode
				+ ", lastUpdateUserId=" + lastUpdateUserId
				+ ", lastUpdateUserName=" + lastUpdateUserName
				+ ", lastUpdateDate=" + lastUpdateDate + "]";
	}
	
}
