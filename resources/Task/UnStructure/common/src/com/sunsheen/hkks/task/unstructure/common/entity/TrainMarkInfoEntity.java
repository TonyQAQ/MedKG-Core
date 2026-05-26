package com.sunsheen.hkks.task.unstructure.common.entity;

/**
 * 
 * @Title: TrainMarkInfoEntity
 * @Description: 训练阶段返回的实体信息
 * @author: FengTao
 * @date 2020年8月25日 下午5:06:57
 */
public class TrainMarkInfoEntity {
	
	private String attrMappingId ;
	private String rowIndex ;
	private String evalId ;
	private AnnotateEntity annotation ;
	
	public String getEvalId() {
		return evalId;
	}
	public void setEvalId(String evalId) {
		this.evalId = evalId;
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
	public AnnotateEntity getAnnotation() {
		return annotation;
	}
	public void setAnnotation(AnnotateEntity annotation) {
		this.annotation = annotation;
	}
	
	
}
