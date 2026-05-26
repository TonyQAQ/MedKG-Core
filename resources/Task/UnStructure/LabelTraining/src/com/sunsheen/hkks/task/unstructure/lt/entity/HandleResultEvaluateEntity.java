package com.sunsheen.hkks.task.unstructure.lt.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * 
 * @Title: SchemaDefineEntity
 * @Description: schema实体
 * @author: FengTao
 * @date 2020年7月13日 下午2:12:53
 */
@Entity
@Table(name = "HANDLE_RESULT_EVALUATE")
public class HandleResultEvaluateEntity {
	@Id
	@Column(name = "EVAL_ID")
	@GeneratedValue(strategy = GenerationType.AUTO,generator = "UUID") //AUTO代表程序控制主键
	@GenericGenerator(name = "UUID", strategy = "uuid")
	private String evalId ;
	
	@Column(name = "TASK_ID")
	private String taskId ;
	
	@Column(name = "IAA")
	private String iaa ;
	
	@Column(name = "TRAIN_COUNT")
	private String trainCount ;
	
	@Column(name = "TYPE_CODE")
	private String typeCode ;
	
	@Column(name = "LAST_UPDATED_USER_ID")
	private String lastUpdateUserId ;
	
	@Column(name = "LAST_UPDATED_USER_NAME")
	private String lastUpdateUserName ;
	
	@Column(name = "LAST_UPDATED_DATE")
	private Date lastUpdateDate ;

	public String getEvalId() {
		return evalId;
	}

	public void setEvalId(String evalId) {
		this.evalId = evalId;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getIaa() {
		return iaa;
	}

	public void setIaa(String iaa) {
		this.iaa = iaa;
	}

	public String getTrainCount() {
		return trainCount;
	}

	public void setTrainCount(String trainCount) {
		this.trainCount = trainCount;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
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
		return "HandleResultEvaluateEntity [evalId=" + evalId + ", taskId="
				+ taskId + ", iaa=" + iaa + ", trainCount=" + trainCount
				+ ", typeCode=" + typeCode + ", lastUpdateUserId="
				+ lastUpdateUserId + ", lastUpdateUserName="
				+ lastUpdateUserName + ", lastUpdateDate=" + lastUpdateDate
				+ "]";
	}

}
