package com.sunsheen.hkks.task.nlp.wst.entity;

import java.util.List;

import com.sunsheen.hkks.nlp.common.entity.WordsEntity;

public class WordsMarkEntity {
	//句子下标
	private String sentenceIndex ;
	private String content ;
	//多标签信息  
	private List<WordsEntity> words ;
	
	public String getSentenceIndex() {
		return sentenceIndex;
	}
	public void setSentenceIndex(String sentenceIndex) {
		this.sentenceIndex = sentenceIndex;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public List<WordsEntity> getWords() {
		return words;
	}
	public void setWords(List<WordsEntity> words) {
		this.words = words;
	}
	@Override
	public String toString() {
		return "WordsMarkEntity [sentenceIndex=" + sentenceIndex + ", content="
				+ content + ", words=" + words + "]";
	}
	
	
}
