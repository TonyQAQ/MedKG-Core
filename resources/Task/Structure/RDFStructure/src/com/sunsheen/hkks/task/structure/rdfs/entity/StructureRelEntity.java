package com.sunsheen.hkks.task.structure.rdfs.entity;

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
 * @Title: StructureRelEntity
 * @Description: RDF关系实体
 * @author: FengTao
 * @date 2020年7月30日 下午6:09:44
 */
@Entity
@Table(name = "HANDLE_STRUCTURED_RESULT_TMP")
public class StructureRelEntity {
	@Id
	@Column(name = "RESULT_ID")
	@GeneratedValue(strategy = GenerationType.AUTO,generator = "UUID") //AUTO代表程序控制主键
	@GenericGenerator(name = "UUID", strategy = "uuid")
	private String resultId ;
	
	@Column(name = "TASK_ID")
	private String taskId ;
	
	@Column(name = "ATTR_START")
	private String attrStart ;
	
	@Column(name = "ATTR_END")
	private String attrEnd ;
	
	@Column(name = "ATTR_UNIQUE")
	private String attrUnique ;
	
	@Column(name = "REL")
	private String rel ;
	
	@Column(name = "LAST_UPDATED_USER_ID")
	private String userId ;
	
	@Column(name = "LAST_UPDATED_USER_NAME")
	private String userName ;
	
	@Column(name = "LAST_UPDATED_DATE")
	private Date lastUpdateDate ;

	public String getResultId() {
		return resultId;
	}

	public void setResultId(String resultId) {
		this.resultId = resultId;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getAttrStart() {
		return attrStart;
	}

	public void setAttrStart(String attrStart) {
		this.attrStart = attrStart;
	}

	public String getAttrEnd() {
		return attrEnd;
	}

	public void setAttrEnd(String attrEnd) {
		this.attrEnd = attrEnd;
	}

	public String getAttrUnique() {
		return attrUnique;
	}

	public void setAttrUnique(String attrUnique) {
		this.attrUnique = attrUnique;
	}

	public String getRel() {
		return rel;
	}

	public void setRel(String rel) {
		this.rel = rel;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	@Override
	public String toString() {
		return "StructureRelEntity [resultId=" + resultId + ", taskId="
				+ taskId + ", attrStart=" + attrStart + ", attrEnd=" + attrEnd
				+ ", attrUnique=" + attrUnique + ", rel=" + rel + ", userId="
				+ userId + ", userName=" + userName + ", lastUpdateDate="
				+ lastUpdateDate + "]";
	}
}
