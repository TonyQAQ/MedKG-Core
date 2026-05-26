package com.sunsheen.hkks.nlp.common.utils;

import java.util.ArrayList;
import java.util.List;

import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.task.unstructure.common.utils.BreakSentence;

public class SentenceReadUtils {
	
	/**
	 * 
	* @Title: readFileSentence
	* @Description:
	* @author: FengTao
	* @date 2020年12月14日 上午10:20:35
	* @param type
	* @param targetFile
	* @param content
	* @param breakSentence  是否断句,ture是断句，false不断句
	* @return String[]
	* @version
	 */
	public static String[] readFileSentence(String type , String targetFile ,StringBuffer content , Boolean breakSentence){
		
		String[] sentences = null ;
		List<String> sentencesList = new ArrayList<>() ;
		if("txt".equals(type)){ //txt文本分句
			content.append(FileUtils.ReadFile(targetFile)) ;
			//整个文本读取
			if(breakSentence){
				sentences = BreakSentence.splitSentence(content.toString()) ;
			}else{
				sentencesList.add(content.toString()) ;
				sentences = sentencesList.toArray(new String[sentencesList.size()]);
			}
		}else if("csv".equals(type)) { //csv按行读取
			//逐行读取
			sentencesList = FileUtils.readFileByLine(targetFile) ;
			for(String sentence : sentencesList){
				content.append(sentence) ;
			}
			sentences = sentencesList.toArray(new String[sentencesList.size()]) ;
		}else if("doc".equals(type) || "docx".equals(type)){
			content.append(FileUtils.readWord(targetFile)) ;
			//整个文本读取
			if(breakSentence){
				sentences = BreakSentence.splitSentence(content.toString()) ;
			}else{
				sentencesList.add(content.toString()) ;
				sentences = sentencesList.toArray(new String[sentencesList.size()]);
			}
		}else{
			System.out.print("不满足该文件！");
		}
		return sentences ;
	}
}
