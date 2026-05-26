package com.sunsheen.hkks.task.unstructure.common.entity;

/**
 * 
 * @Title: SchemaJsonEntity
 * @Description: 标注json截取列表
 * @author: FengTao
 * @date 2020年8月14日 下午2:23:15
 */
public class SchemaJsonEntity {
	
	private String sampleId ;
	private String tag ;
	private AnnotateEntity annotation ;
	
	
	public AnnotateEntity getAnnotation() {
		return annotation;
	}
	public void setAnnotation(AnnotateEntity annotation) {
		this.annotation = annotation;
	}
	public String getSampleId() {
		return sampleId;
	}
	public void setSampleId(String sampleId) {
		this.sampleId = sampleId;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	@Override
	public String toString() {
		return "SchemaJsonEntity [sampleId=" + sampleId + ", tag=" + tag
				+ ", annotation=" + annotation + "]";
	}
	
}
