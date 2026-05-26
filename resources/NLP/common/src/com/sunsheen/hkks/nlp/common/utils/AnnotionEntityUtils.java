package com.sunsheen.hkks.nlp.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.sunsheen.hkks.nlp.common.entity.AnnotateEntity;
import com.sunsheen.hkks.nlp.common.entity.LabelCategoriesEntity;
import com.sunsheen.hkks.nlp.common.entity.NLPSentenceEntity;

public class AnnotionEntityUtils {
	
	public static void judgeandAddInitLabelCategories(AnnotateEntity annotation){
		if(annotation != null && (annotation.getLabelCategories()==null || annotation.getLabelCategories().size() <= 0)){
			LabelCategoriesEntity temp = new LabelCategoriesEntity();
			temp.setId("originId");
			temp.setText("初始化");
			temp.setColor("#ffff");
			temp.setBorderColor("#ffff") ;
			annotation.setLabelCategories(new ArrayList<LabelCategoriesEntity>() );
			annotation.getLabelCategories().add(temp) ;
		}
	}
	
	public static void judgeandAddInitLabelCategories(List<LabelCategoriesEntity> labelCategories){
		if( labelCategories != null && labelCategories.size() <= 0 ){
			LabelCategoriesEntity temp = new LabelCategoriesEntity();
			temp.setId("originId");
			temp.setText("初始化");
			temp.setColor("#ffff");
			temp.setBorderColor("#ffff") ;
			labelCategories.add(temp) ;
		}
	}
	/**
	 * 
	* @Title: createEntityObject
	* @Description: 构件分句标准对象
	* @author: FengTao
	* @date 2020年12月2日 下午3:40:02
	* @param sentences
	* @return List<NLPSentenceEntity>
	* @version
	 */
	public static List<NLPSentenceEntity> createEntityObject(String[] sentences){
		List<NLPSentenceEntity> sentencesEntity = new ArrayList<NLPSentenceEntity>() ;
		Integer start = 0 ;

		AtomicInteger sentenceIndex = new AtomicInteger(0) ;
		for (String sentence : sentences) {
			
			NLPSentenceEntity sentenceEntity = new NLPSentenceEntity() ;
			//初始信息
			sentenceEntity.setId(sentenceIndex.intValue()+"");
			sentenceEntity.setTags(new ArrayList<>());
			sentenceEntity.setWords(new ArrayList<>());
			sentenceEntity.setCategories(new ArrayList<>());
			sentenceEntity.setSentenceIndex(sentenceIndex.getAndIncrement()+"");
			sentenceEntity.setStartIndex(start+"");//在原句中的位置
			sentenceEntity.setEndIndex(start+sentence.length()+"");//在原句中的位置
			AnnotateEntity annotation = new AnnotateEntity() ;
			//构件标注实体
			annotation.setContent(sentence);
			annotation.setLabelCategories(new ArrayList<>());
			annotation.setConnectionCategories(new ArrayList<>());
			annotation.setLabels(new ArrayList<>());
			annotation.setConnections(new ArrayList<>());
			sentenceEntity.setAnnotation(annotation);
			sentencesEntity.add(sentenceEntity) ;
			start += sentence.length();
		}
		return sentencesEntity ;
	}
}
