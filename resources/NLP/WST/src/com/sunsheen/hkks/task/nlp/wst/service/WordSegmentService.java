package com.sunsheen.hkks.task.nlp.wst.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alibaba.fastjson.JSONObject;
import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.common.util.PageParamsUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.nlp.common.entity.NLPLabelsEntity;
import com.sunsheen.hkks.nlp.common.entity.NLPSentenceEntity;
import com.sunsheen.hkks.nlp.common.entity.WordsEntity;
import com.sunsheen.hkks.nlp.common.utils.AnnotionEntityUtils;
import com.sunsheen.hkks.nlp.common.utils.SentenceReadUtils;
import com.sunsheen.hkks.task.nlp.wst.entity.WordsMarkEntity;
import com.sunsheen.jfids.backport.java.util.concurrent.atomic.AtomicInteger;

public class WordSegmentService {

	/**
	 * 
	* @Title: readyWordsFileBtUpload
	* @Description: 预准备分词文件
	* @author: FengTao
	* @date 2020年11月26日 上午10:34:57
	* @param retMap
	* @param params void
	* @version
	 */
	public void readyWordsFileByUpload( Map<String , Object> retMap , Map<String , Object> params){
		//源标注信息
		String sourcePath = params.get("rootPath") + "source" ;
		try {
			//遍历目标文件列表获取所有文件（不获取文件夹），为每个文件重新构建目标文件
			List<String> files = FileUtils.getAllFile(sourcePath, true) ;
			for(String soruceFile : files){
				StringBuffer content =  new StringBuffer() ;//每个文件的内容
				//文件信息的实体
				NLPLabelsEntity nlpLabelsEntity = new NLPLabelsEntity() ; 
				//句子标注信息
				List<NLPSentenceEntity> sentencesEntity = new ArrayList<NLPSentenceEntity>() ;
				String targetNewFile = soruceFile ;
				//构件目标文件的文件名
				String fileFullName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1,  targetNewFile.length()) ;
				String fileName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1, targetNewFile.lastIndexOf(".")) ;
				String type = targetNewFile.substring( targetNewFile.lastIndexOf(".")+1 , targetNewFile.length()).toLowerCase();
				String fileNewFullName = fileName+"-"+type+".json" ;
				//生成最终文件名称信息
				targetNewFile = targetNewFile.replace( fileFullName , fileNewFullName )
						.replace(File.separator+"source"+File.separator, File.separator+"target"+File.separator) ;
				String[] sentences = SentenceReadUtils.readFileSentence(type,soruceFile,content,true) ;
				//构建每个句子的实体信息
				sentencesEntity = AnnotionEntityUtils.createEntityObject(sentences) ;
				nlpLabelsEntity.setId(UUID.randomUUID().toString());
				nlpLabelsEntity.setContent(content.toString());
				nlpLabelsEntity.setSourceName(fileFullName);
				nlpLabelsEntity.setSentences(sentencesEntity);
				//构件标注文件信息
				FileUtils.createJsonFile(nlpLabelsEntity, targetNewFile);
			}
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg","准备成功！") ;
		} catch (Exception e) {
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}		
	}

	/**
	 * 
	* @Title: readyWordsFileByCite
	* @Description: 预准备分词文件
	* @author: FengTao
	* @date 2020年11月26日 上午10:34:57
	* @param retMap
	* @param params void
	* @version
	 */
	public void readyWordsFileByCite( Map<String , Object> retMap , Map<String , Object> params){
//		//标签信息
//		String schemaPath = params.get("rootPath") + "schema" ;
//		//实体标签
//		List<LabelCategoriesEntity> labelCategories = new ArrayList<LabelCategoriesEntity>() ;
//		//关系标签
//		List<ConnCategoriesEntity> connectionCategories = new ArrayList<ConnCategoriesEntity>() ; 
		try{
//			AnnotionEntityUtils.judgeandAddInitLabelCategories(labelCategories);
//			FileUtils.createJsonFile(labelCategories, schemaPath+File.separator + "labelCategories.json");
//			FileUtils.createJsonFile(connectionCategories, schemaPath+File.separator +"connectionCategories.json");
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg","准备成功！") ;
		} catch (Exception e) {
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: saveWords
	* @Description:保存分词文件信息
	* @author: FengTao
	* @date 2020年11月26日 上午10:35:19
	* @param retMap
	* @param params void
	* @version
	 */
	public void saveWordsFile( Map<String , Object> retMap , Map<String , Object> params,List<WordsMarkEntity> wordsMarkEntity){
		//标签信息
		String targetPath = params.get("rootPath") + "target" ;
		try{
			String path =  targetPath + File.separator + params.get("paths") ;
			String targetNewFile = path.replace(File.separator+"source"+File.separator, File.separator+"target"+File.separator) ;
			String fileFullName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1,  targetNewFile.length()) ;
			String fileName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1, targetNewFile.lastIndexOf(".")) ;
			String type = targetNewFile.substring( targetNewFile.lastIndexOf(".")+1 , targetNewFile.length()) ;
			String fileNewFullName = fileName+"-"+type+".json" ;
			targetNewFile = targetNewFile.replace( fileFullName , fileNewFullName ) ;
			NLPLabelsEntity nlpLabelsEntity = JSONObject.parseObject( FileUtils.readJsonFile( targetNewFile ),NLPLabelsEntity.class ) ;
			//分词标注信息
			for(WordsMarkEntity entity : wordsMarkEntity){
				Integer sentenceIndex = Integer.parseInt( entity.getSentenceIndex() ) ;
				AtomicInteger id = new AtomicInteger(0) ;
				for (WordsEntity word : entity.getWords()) {
					word.setId(id.incrementAndGet()+"");//将id存放成数字字符串，节省内存空间
				}
//				List<WordsEntity> list = JSONObject.parseArray( JSONObject.toJSONString(entity.getWords()),WordsEntity.class ) ;
				nlpLabelsEntity.getSentences().get(sentenceIndex).setWords(entity.getWords());
			}
			//更新标注文件信息
			FileUtils.createJsonFile(nlpLabelsEntity, targetNewFile) ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
	
	/**
	 * 查询分词文件信息
	* @Title: searchWordsFile
	* @Description:
	* @author: FengTao
	* @date 2020年11月26日 上午10:35:35
	* @param retMap
	* @param params void
	* @version
	 */
	public void searchWordsFile( Map<String , Object> retMap , Map<String , Object> params){
		//目标标注信息
		String targetPath = params.get("rootPath") + "target" + File.separator;
		Map<String , Object> data = new HashMap<String ,Object>() ;
		try{
			//文件获取
			String targetNewFile =  targetPath + params.get("paths");
			//文件原名称
			String fileFullName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1,  targetNewFile.length()) ;
			String fileName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1, targetNewFile.lastIndexOf(".")) ;
			String type = targetNewFile.substring( targetNewFile.lastIndexOf(".")+1 , targetNewFile.length()) ;
			String fileNewFullName = fileName+"-"+type+".json" ;
			targetNewFile = targetNewFile.replace( fileFullName , fileNewFullName ) ;
			
			NLPLabelsEntity nlpLabelsEntity = JSONObject.parseObject( FileUtils.readJsonFile( targetNewFile ),NLPLabelsEntity.class ) ;
			List<NLPSentenceEntity> nlpSentences = new ArrayList<NLPSentenceEntity>() ; 
			nlpSentences.addAll(nlpLabelsEntity.getSentences()) ;
			Integer totalCount = nlpSentences.size() ;
			//分页截取
			PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), (String)params.get("pageSize"), totalCount+"");
			Integer startIndex = Integer.parseInt(params.get("pageIndex")+"");
			Integer endIndex = (Integer.parseInt(params.get("pageIndex")+"")+Integer.parseInt(params.get("pageSize")+"") ) > nlpSentences.size() ? nlpSentences.size() :
				 Integer.parseInt(params.get("pageIndex")+"")+Integer.parseInt(params.get("pageSize")+"");
			nlpSentences = nlpSentences.subList(startIndex , endIndex) ;
			
			List<WordsMarkEntity> stentenceAndWords = new ArrayList<>() ;
			for(NLPSentenceEntity entity : nlpSentences){
				String content = entity.getAnnotation().getContent() ;
				WordsMarkEntity temp = new WordsMarkEntity() ;
				temp.setSentenceIndex(entity.getSentenceIndex());
				temp.setWords(entity.getWords());
				temp.setContent(content);
				stentenceAndWords.add(temp) ;
				for(WordsEntity word : entity.getWords()){
					word.setText(content.substring(word.getStartIndex(), word.getEndIndex()));
				}
//				String content = entity.getAnnotation().getContent() ;
//				WordsMarkEntity temp = new WordsMarkEntity() ;
//				temp.setSentenceIndex(entity.getSentenceIndex());
//				System.out.println();
//				List<FullWordsEntity> list = JSONObject.parseArray( JSONObject.toJSONString(entity.getWords()),FullWordsEntity.class ) ;
//				for(FullWordsEntity fullWordsEntity : list){
//					fullWordsEntity.setText(content.substring(fullWordsEntity.getStartIndex(), fullWordsEntity.getEndIndex()));
//				}
//				temp.setWords(list);
//				temp.setContent(entity.getAnnotation().getContent());
//				stentenceAndWords.add(temp) ;
			}
			retMap.putAll(RetInfo.RETSUCCESS) ;
			if(stentenceAndWords.size() <= 0 ){
				retMap.put("retmsg", "查询成功，暂无数据") ;
			}
			data.put("sentences", stentenceAndWords) ;
			retMap.put("totalCount", totalCount) ;
			retMap.put("data", data) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
}
