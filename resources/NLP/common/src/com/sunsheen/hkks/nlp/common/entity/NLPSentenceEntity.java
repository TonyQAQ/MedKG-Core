package com.sunsheen.hkks.nlp.common.entity;

import java.util.List;

/**
 * 
 * @Title: NLPSentenceEntity
 * @Description: 文本句子信息
 * @author: FengTao
 * @date 2020年9月9日 下午5:23:59
 */
public class NLPSentenceEntity {

	private String id ;
	//句子下标
	private String sentenceIndex ;
	//开始位置
	private String startIndex ;
	//结束位置
	private String endIndex ;
	//分类信息
	private List<String> categories ;
	//多标签信息  
	private List<String> tags ;
	//多标签信息  
	private List<WordsEntity> words ;
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
	public List<String> getCategories() {
		return categories;
	}
	public void setCategories(List<String> categories) {
		this.categories = categories;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	public List<WordsEntity> getWords() {
		return words;
	}
	public void setWords(List<WordsEntity> words) {
		this.words = words;
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
		return "NLPSentenceEntity [id=" + id + ", sentenceIndex="
				+ sentenceIndex + ", startIndex=" + startIndex + ", endIndex="
				+ endIndex + ", categories=" + categories + ", tags=" + tags
				+ ", words=" + words + ", annotation=" + annotation + "]";
	}
	
}
