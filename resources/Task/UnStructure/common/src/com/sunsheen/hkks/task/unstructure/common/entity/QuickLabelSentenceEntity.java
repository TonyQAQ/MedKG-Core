package com.sunsheen.hkks.task.unstructure.common.entity;

/**
 * 
 * @Title: QuickLabelSentenceEntity
 * @Description: 文本句子信息
 * @author: FengTao
 * @date 2020年9月9日 下午5:23:59
 */
public class QuickLabelSentenceEntity {

	private String id ;
	//句子下标
	private String sentenceIndex ;
	//开始位置
	private String startIndex ;
	//结束位置
	private String endIndex ;
	//标注信息
	private AnnotateEntity annotation;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSentenceIndex() {
		return sentenceIndex;
	}
	public void setSentenceIndex(String sentenceIndex) {
		this.sentenceIndex = sentenceIndex;
	}
	public String getStartIndex() {
		return startIndex;
	}
	public void setStartIndex(String startIndex) {
		this.startIndex = startIndex;
	}
	public String getEndIndex() {
		return endIndex;
	}
	public void setEndIndex(String endIndex) {
		this.endIndex = endIndex;
	}
	public AnnotateEntity getAnnotation() {
		return annotation;
	}
	public void setAnnotation(AnnotateEntity annotation) {
		this.annotation = annotation;
	}
	@Override
	public String toString() {
		return "QuickLabelSentenceEntity [id=" + id + ", sentenceIndex="
				+ sentenceIndex + ", startIndex=" + startIndex + ", endIndex="
				+ endIndex + ", annotation=" + annotation + "]";
	}
	
}
