package com.sunsheen.hkks.task.unstructure.fm.entity;

import java.util.Date;

import javax.persistence.* ;

import org.hibernate.annotations.GenericGenerator;

/**
 * 
 * @Title: HandleUsAssignEntity
 * @Description: HandleUsAssignEntity实体
 * @author: FengTao
 * @date 2020年7月13日 下午2:12:53
 */
@Entity
@Table(name = "HANDLE_UNSTRUCTURED_ASSIGN")
public class HandleUsAssignEntity {
	@Id
	@Column(name = "ASSIGN_ID")
	@GeneratedValue(strategy = GenerationType.AUTO,generator = "UUID") //AUTO代表程序控制主键
	@GenericGenerator(name = "UUID", strategy = "uuid")
	private String assignId ;
	
	@Column(name = "ATTR_MAPPING_ID")
	private String attrMappingId ;
	
	@Column(name = "ASSIGN_COUNT")
	private String assignCount ;
	
	@Column(name = "ROW_START")
	private String rowStart ;
	
	@Column(name = "ROW_END")
	private String rowEnd ;
	
	@Column(name = "TASK_ID")
	private String taskId ;
	
	@Column(name = "USER_ID")
	private String userId ;
	
	@Column(name = "LAST_UPDATED_DATE")
	private Date lastUpdateDate ;

	public String getAssignCount() {
		return assignCount;
	}

	public void setAssignCount(String assignCount) {
		this.assignCount = assignCount;
	}

	public String getAssignId() {
		return assignId;
	}

	public void setAssignId(String assignId) {
		this.assignId = assignId;
	}

	public String getAttrMappingId() {
		return attrMappingId;
	}

	public void setAttrMappingId(String attrMappingId) {
		this.attrMappingId = attrMappingId;
	}

	public String getRowStart() {
		return rowStart;
	}

	public void setRowStart(String rowStart) {
		this.rowStart = rowStart;
	}

	public String getRowEnd() {
		return rowEnd;
	}

	public void setRowEnd(String rowEnd) {
		this.rowEnd = rowEnd;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	@Override
	public String toString() {
		return "HandleUsAssignEntity [assignId=" + assignId
				+ ", attrMappingId=" + attrMappingId + ", rowStart=" + rowStart
				+ ", rowEnd=" + rowEnd + ", taskId=" + taskId + ", userId="
				+ userId + ", lastUpdateDate=" + lastUpdateDate + "]";
	}

}
