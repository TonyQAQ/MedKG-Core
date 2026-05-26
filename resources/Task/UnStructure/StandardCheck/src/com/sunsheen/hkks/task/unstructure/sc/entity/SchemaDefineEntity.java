package com.sunsheen.hkks.task.unstructure.sc.entity;

import java.util.Date;

import javax.persistence.* ;

import org.hibernate.annotations.GenericGenerator;

/**
 * 
 * @Title: SchemaDefineEntity
 * @Description: schema实体
 * @author: FengTao
 * @date 2020年7月13日 下午2:12:53
 */
@Entity
@Table(name = "SCHEMA_DEFINE")
public class SchemaDefineEntity {
	@Id
	@Column(name = "SCHEMA_ID")
	@GeneratedValue(strategy = GenerationType.AUTO,generator = "UUID") //AUTO代表程序控制主键
	@GenericGenerator(name = "UUID", strategy = "uuid")
	private String schemaId ;
	
	@Column(name = "SCHEMA_NAME")
	private String schemaName ;
	
	@Column(name = "DEFINITION")
	private String definition ;
	
	@Column(name = "LABEL_NAME")
	private String labelName ;
	
	@Column(name = "TYPE_CODE")
	private String typeCode ;
	
	@Column(name = "TASK_ID")
	private String taskId ;
	
	@Column(name = "COLOR")
	private String color ;
	
	@Column(name = "BORDER_COLOR")
	private String borderColor ;
	
	@Column(name = "LAST_UPDATED_USER_ID")
	private String lastUpdateUserId ;
	
	@Column(name = "LAST_UPDATED_USER_NAME")
	private String lastUpdateUserName ;
	
	@Column(name = "LAST_UPDATED_DATE" , updatable=false)
	private Date lastUpdateDate ;

	public String getSchemaId() {
		return schemaId;
	}

	public void setSchemaId(String schemaId) {
		this.schemaId = schemaId;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getLabelName() {
		return labelName;
	}

	public void setLabelName(String labelName) {
		this.labelName = labelName;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(String borderColor) {
		this.borderColor = borderColor;
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
		return "SchemaDefineEntity [schemaId=" + schemaId + ", schemaName="
				+ schemaName + ", definition=" + definition + ", labelName="
				+ labelName + ", typeCode=" + typeCode + ", taskId=" + taskId
				+ ", color=" + color + ", borderColor=" + borderColor
				+ ", lastUpdateUserId=" + lastUpdateUserId
				+ ", lastUpdateUserName=" + lastUpdateUserName
				+ ", lastUpdateDate=" + lastUpdateDate + "]";
	}
	
}
