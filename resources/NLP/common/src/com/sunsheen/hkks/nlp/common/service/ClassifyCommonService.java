package com.sunsheen.hkks.nlp.common.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alibaba.fastjson.JSONObject;
import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.common.util.PageParamsUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.nlp.common.entity.ClassifyCategoriesEntity;
import com.sunsheen.hkks.nlp.common.entity.ClassifyTagsEntity;
import com.sunsheen.hkks.nlp.common.entity.NLPLabelsEntity;
import com.sunsheen.hkks.nlp.common.entity.NLPSentenceEntity;
import com.sunsheen.hkks.nlp.common.utils.AnnotionEntityUtils;
import com.sunsheen.hkks.nlp.common.utils.SentenceReadUtils;

public class ClassifyCommonService {
	
	private static final Integer PAGE_STRING_LENGTH = 3000 ; //一页字符大小
	
	/**
	 * 依据大文本分页的查询
	* @Title: searchClassifyFileByContent
	* @Description:
	* @author: FengTao
	* @date 2020年12月14日 上午10:51:03
	* @param retMap
	* @param params void
	* @version
	 */
	public void searchClassifyFileByContent( Map<String , Object> retMap , Map<String , Object> params){
		//目标标注信息
		String targetPath = params.get("rootPath") + "target" ;
		try{
			//标注文件获取
			String path =  targetPath + File.separator + params.get("paths") ;
			String targetNewFile = path.replace(File.separator+"source"+File.separator, File.separator+"target"+File.separator) ;
			String fileFullName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1,  targetNewFile.length()) ;
			String fileName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1, targetNewFile.lastIndexOf(".")) ;
			String type = targetNewFile.substring( targetNewFile.lastIndexOf(".")+1 , targetNewFile.length()) ;
			String fileNewFullName = fileName+"-"+type+".json" ;
			targetNewFile = targetNewFile.replace( fileFullName , fileNewFullName ) ;

	        long startTime=System.currentTimeMillis();
			NLPLabelsEntity nlpLabelsEntity = JSONObject.parseObject( FileUtils.readJsonFile( targetNewFile ),NLPLabelsEntity.class ) ;
	        long endTime=System.currentTimeMillis();
	        System.out.println("程序运行时间： "+Double.valueOf(endTime-startTime)/1000+"s");
			List<NLPSentenceEntity> nlpSentenceEntities = new ArrayList<NLPSentenceEntity>() ;
			nlpSentenceEntities.addAll(nlpLabelsEntity.getSentences()) ;
			if(nlpSentenceEntities.size() > 0){
				String content = nlpSentenceEntities.get(0).getAnnotation().getContent() ;
				Integer totalCount = content.length()%PAGE_STRING_LENGTH == 0 ?
													(content.length()/PAGE_STRING_LENGTH) :
													(content.length()/PAGE_STRING_LENGTH)+1 ;
				PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), params.get("pageSize")+"" , totalCount+"");
				//分页截取
				Integer startIndex = Integer.parseInt(params.get("pageIndex")+"")*PAGE_STRING_LENGTH;
				Integer endIndex = startIndex+Integer.parseInt(params.get("pageSize")+"")*PAGE_STRING_LENGTH > content.length() ? 
												content.length() :
												startIndex + +Integer.parseInt(params.get("pageSize")+"")*PAGE_STRING_LENGTH;
				for(NLPSentenceEntity sentence : nlpSentenceEntities){//去除标注信息，只留下分类信息
					sentence.getAnnotation().setConnectionCategories(new ArrayList<>());
					sentence.getAnnotation().setConnections(new ArrayList<>());
					sentence.getAnnotation().setLabels(new ArrayList<>());
					sentence.getAnnotation().setLabelCategories(new ArrayList<>());
					sentence.getAnnotation().setContent( content.substring( startIndex, endIndex ) );
				}
				retMap.putAll(RetInfo.RETSUCCESS) ;
				retMap.put("data", nlpSentenceEntities) ;
				retMap.put("totalCount", totalCount) ;
			}else{
				retMap.put("data", new ArrayList<>()) ;
				retMap.put("totalCount", "0") ;
				retMap.putAll(RetInfo.RETSUCCESS);
				retMap.put("retmsg", "暂无数据") ;
			}
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
	/**
	 * 
	* @Title: saveClassifyJson
	* @Description: 保存关系标签或实体标签
	* @author: FengTao
	* @date 2020年9月10日 上午11:30:54
	* @param retMap
	* @param labelCategories
	* @param connectionCategories
	* @param params void
	* @version
	 */
	public void saveClassifyJson(Map<String,Object> retMap , List<ClassifyCategoriesEntity> classifyCategories , List<ClassifyTagsEntity> classifyTags , Map<String,Object> params){
		String schemaPath = params.get("rootPath") + "schema" ;
		try{
			if(classifyCategories != null ){
				FileUtils.createJsonFile(classifyCategories, schemaPath+File.separator + "classifyCategories.json") ;
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}else if(classifyTags != null ){
				FileUtils.createJsonFile(classifyTags, schemaPath+File.separator + "classifyTags.json") ;
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}else{
				retMap.putAll(RetInfo.RETFAIL) ;
			}
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	/**
	 * 
	* @Title: updateClassifyDatas
	* @Description: 更新标注文件信息
	* @author: FengTao
	* @date 2020年9月10日 上午11:50:24
	* @param retMap
	* @param params void
	* @version
	 */
	public void updateClassifyDatas( Map<String , Object> retMap , List<NLPSentenceEntity> nlpSentenceEntities , Map<String , Object> params,String typeCode){
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
			for( NLPSentenceEntity nlpSentenceEntity : nlpSentenceEntities ){
				Integer sentenceIndex = Integer.parseInt( nlpSentenceEntity.getSentenceIndex() ) ;
				nlpSentenceEntity.getAnnotation().setConnectionCategories(new ArrayList<>()); //不需要存，已经存在公共文件里面
				nlpSentenceEntity.getAnnotation().setLabelCategories(new ArrayList<>());//不需要存，已经存在公共文件里面
				if("classifyCategories".equals(typeCode)){
					nlpLabelsEntity.getSentences().get(sentenceIndex).setCategories(nlpSentenceEntity.getCategories()) ;
				}else if("classifyTags".equals(typeCode)){
					nlpLabelsEntity.getSentences().get(sentenceIndex).setTags(nlpSentenceEntity.getTags()) ;
				}else{
					retMap.putAll(RetInfo.RETSUCCESS) ;
					retMap.put("retmsg", "没有任何更新") ;
					return  ;
				}
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
	 * 
	* @Title: searchClassify
	* @Description: 类别信息获取
	* @author: FengTao
	* @date 2020年9月10日 上午10:09:09
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void searchClassify( Map<String , Object> retMap , Map<String , Object> params,String typeCode){
		//标签信息
		String schemaPath = params.get("rootPath") + "schema" ;
		try{
			//实体标签
			List<ClassifyCategoriesEntity> classifyCategories = new ArrayList<ClassifyCategoriesEntity>() ;
			//关系标签
			List<ClassifyTagsEntity> classifyTags = new ArrayList<ClassifyTagsEntity>() ; 
			//文件获取
			if("classifyCategories".equals(typeCode)){
				classifyCategories = JSONObject.parseObject(FileUtils.readJsonFile(schemaPath+File.separator + "classifyCategories.json"),classifyCategories.getClass()) ;
			}else if("classifyTags".equals(typeCode)){
				classifyTags = JSONObject.parseObject(FileUtils.readJsonFile(schemaPath+File.separator + "classifyTags.json"),classifyTags.getClass()) ;
			}else{
				classifyCategories = JSONObject.parseObject(FileUtils.readJsonFile(schemaPath+File.separator + "classifyCategories.json"),classifyCategories.getClass()) ;
				classifyTags = JSONObject.parseObject(FileUtils.readJsonFile(schemaPath+File.separator + "classifyTags.json"),classifyTags.getClass()) ;
			}
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("classifyCategories", classifyCategories) ;
			retMap.put("classifyTags", classifyTags) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
	
	/**
	 * 
	* @Title: readyMarkFileByUpload
	* @Description: 多类别分类准备工作
	* @author: FengTao
	* @date 2020年9月9日 下午5:38:06
	* @param retMap
	* @param params void
	* @version
	 */
	public void readyMarkFileByUpload( Map<String , Object> retMap , Map<String , Object> params , String typeCode ) {
		//源标注信息
		String sourcePath = params.get("rootPath") + "source" ;
		//标签信息
		String schemaPath = params.get("rootPath") + "schema" ;
		//多类别分类信息
		List<ClassifyCategoriesEntity> classifyCategories = new ArrayList<ClassifyCategoriesEntity>() ;
		//多标签分类信息
		List<ClassifyTagsEntity> classifyTags = new ArrayList<ClassifyTagsEntity>() ; 
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
				String[] sentences = SentenceReadUtils.readFileSentence(type,soruceFile,content,false) ;
				//构建每个句子的实体信息
				sentencesEntity = AnnotionEntityUtils.createEntityObject(sentences) ;
				nlpLabelsEntity.setId(UUID.randomUUID().toString());
				nlpLabelsEntity.setContent(content.toString());
				nlpLabelsEntity.setSourceName(fileFullName);
				nlpLabelsEntity.setSentences(sentencesEntity);
				//构件标注文件信息
				FileUtils.createJsonFile(nlpLabelsEntity, targetNewFile);//创建初始化标签信息
				if("classifyCategories".equals(typeCode)){
					FileUtils.createJsonFile(classifyCategories, schemaPath+File.separator + "classifyCategories.json");
				}else if("classifyTags".equals(typeCode)){
					FileUtils.createJsonFile(classifyTags, schemaPath+File.separator +"classifyTags.json");
				}
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
	* @Title: readyWordsFileBtUpload
	* @Description: 预准备分词文件
	* @author: FengTao
	* @date 2020年11月26日 上午10:34:57
	* @param retMap
	* @param params void
	* @version
	 */
	public void readyMarkFileByCite( Map<String , Object> retMap , Map<String , Object> params , String typeCode){
		//标签信息
		String schemaPath = params.get("rootPath") + "schema" ;
		//多类别分类信息
		List<ClassifyCategoriesEntity> classifyCategories = new ArrayList<ClassifyCategoriesEntity>() ;
		//多标签分类信息
		List<ClassifyTagsEntity> classifyTags = new ArrayList<ClassifyTagsEntity>() ; 
		try{
			if("classifyCategories".equals(typeCode)){
				FileUtils.createJsonFile(classifyCategories, schemaPath+File.separator + "classifyCategories.json");
			}else if("classifyTags".equals(typeCode)){
				FileUtils.createJsonFile(classifyTags, schemaPath+File.separator +"classifyTags.json");
			}
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg","准备成功！") ;
		} catch (Exception e) {
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	
}
