package com.sunsheen.hkks.task.unstructure.common.entity;

/**
 * 
 * @Title: CustomMarkEntity
 * @Description: 标注信息列表
 * @author: FengTao
 * @date 2020年8月14日 下午2:23:15
 */
public class MarkInfoEntity {
	
	private String attrMappingId ;
	private String rowIndex ;
	private AnnotateEntity annotation ;
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
