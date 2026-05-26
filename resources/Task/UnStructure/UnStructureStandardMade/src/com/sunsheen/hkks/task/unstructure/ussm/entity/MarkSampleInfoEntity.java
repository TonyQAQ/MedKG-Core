package com.sunsheen.hkks.task.unstructure.ussm.entity;

import java.util.Date;

import javax.persistence.* ;

import org.hibernate.annotations.GenericGenerator;

/**
 * 
 * @Title: MarkSampleInfoEntity
 * @Description: 标签标注信息保存
 * @author: FengTao
 * @date 2020年7月13日 下午2:12:53
 */
@Entity
@Table(name = "MARK_SAMPLE_INFO")
public class MarkSampleInfoEntity {
	@Id
	@Column(name = "SAMPLE_ID")
	@GeneratedValue(strategy = GenerationType.AUTO,generator = "UUID") //AUTO代表程序控制主键
	@GenericGenerator(name = "UUID", strategy = "uuid")
	private String sampleId ;
	
	@Column(name = "ATTR_MAPPING_ID")
	private String attrMappingId ;
	
	@Column(name = "ROW_INDEX")
	private String rowIndex ;
	
	@Column(name = "MARK_FILE_NAME")
	private String markFileName ;
	
	@Column(name = "LABEL_ID")
	private String labelId ;
	
	@Column(name = "FROM_ID")
	private String fromId ;
	
	@Column(name = "TO_ID")
	private String toId ;
	
	@Column(name = "TAG")
	private String tag ;
	
	@Column(name = "LAST_UPDATED_USER_ID")
	private String lastUpdateUserId ;
	
	@Column(name = "LAST_UPDATED_USER_NAME")
	private String lastUpdateUserName ;
	
	@Column(name = "LAST_UPDATED_DATE")
	private Date lastUpdateDate ;

	@Column(name = "EVAL_ID")
	private String evalId ;
	
	public String getEvalId() {
		return evalId;
	}

	public void setEvalId(String evalId) {
		this.evalId = evalId;
	}
	
	public String getSampleId() {
		return sampleId;
	}

	public void setSampleId(String sampleId) {
		this.sampleId = sampleId;
	}

	public String getAttrMappingId() {
		return attrMappingId;
	}

	public void setAttrMappingId(String attrMappingId) {
		this.attrMappingId = attrMappingId;
	}

	public String getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(String rowIndex) {
		this.rowIndex = rowIndex;
	}

	public String getMarkFileName() {
		return markFileName;
	}

	public void setMarkFileName(String markFileName) {
		this.markFileName = markFileName;
	}

	public String getLabelId() {
		return labelId;
	}

	public void setLabelId(String labelId) {
		this.labelId = labelId;
	}

	public String getFromId() {
		return fromId;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	public String getToId() {
		return toId;
	}

	public void setToId(String toId) {
		this.toId = toId;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
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
		return "MarkSampleInfoEntity [sampleId=" + sampleId
				+ ", attrMappingId=" + attrMappingId + ", rowIndex=" + rowIndex
				+ ", markFileName=" + markFileName + ", labelId=" + labelId
				+ ", fromId=" + fromId + ", toId=" + toId + ", tag=" + tag
				+ ", lastUpdateUserId=" + lastUpdateUserId
				+ ", lastUpdateUserName=" + lastUpdateUserName
				+ ", lastUpdateDate=" + lastUpdateDate + "]";
	}
}
