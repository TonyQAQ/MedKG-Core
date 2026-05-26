package com.sunsheen.hkks.task.unstructure.common.entity;

import java.util.List;

public class QuickLabelsEntity {
	
	private String id ;
	//源名称
	private String sourceName ; 
	//内容
	private String content ;
	// 分句标注信息
	private List<QuickLabelSentenceEntity> sentences ;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSourceName() {
		return sourceName;
	}
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public List<QuickLabelSentenceEntity> getSentences() {
		return sentences;
	}
	public void setSentences(List<QuickLabelSentenceEntity> sentences) {
		this.sentences = sentences;
	}
	
	@Override
	public String toString() {
		return "QuickLabelsEntity [id=" + id + ", sourceName=" + sourceName
				+ ", content=" + content + ", sentences=" + sentences + "]";
	}
	
}
