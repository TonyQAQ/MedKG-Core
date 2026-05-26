package com.sunsheen.hkks.nlp.common.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.nlp.common.entity.ConnCategoriesEntity;
import com.sunsheen.hkks.nlp.common.entity.LabelCategoriesEntity;
import com.sunsheen.hkks.nlp.common.entity.NLPLabelsEntity;
import com.sunsheen.hkks.nlp.common.entity.NLPSentenceEntity;
import com.sunsheen.hkks.nlp.common.utils.AnnotionEntityUtils;
import com.sunsheen.hkks.nlp.common.utils.SentenceReadUtils;

public class MarkCommonService {
	
	
	
	
	
	/**
	 * 
	* @Title: savePictureSchemaJson
	* @Description: 保存图片的标注实体
	* @author: lijian
	* @date 2021年9月26日 
	* @param retMap
	* @param  params
	* @param puctureLabel
	* @param params void
	* @version
	 */
	public void savePictureSchemaJson(Map<String,Object> retMap , Map<String,Object> params,JSONArray array){
		String schemaPath = params.get("filePath") + "schema" ;
		
		String rootPath = schemaPath + FileUtils.FILE_SEPARATOR + "labelCategories.json";
		try{
			FileUtils.deleteFile(rootPath);
			FileUtils.createJsonFile(array,rootPath);
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL);
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	* @Title: saveSchemaJson
	* @Description: 保存关系标签或实体标签
	* @author: FengTao
	* @date 2020年9月10日 上午11:30:54
	* @param retMap
	* @param labelCategories
	* @param connectionCategories
	* @param params void
	* @version
	 */
	public void saveSchemaJson(Map<String,Object> retMap , List<LabelCategoriesEntity> labelCategories , List<ConnCategoriesEntity> connectionCategories , Map<String,Object> params){
		String schemaPath = params.get("rootPath") + "schema" ;
		try{
			if(labelCategories != null ){
				FileUtils.createJsonFile(labelCategories, schemaPath+File.separator + "labelCategories.json") ;
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}else if(connectionCategories != null ){
				FileUtils.createJsonFile(connectionCategories, schemaPath+File.separator + "connectionCategories.json") ;
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
	* @Title: searchSchema
	* @Description: 类别信息获取
	* @author: FengTao
	* @date 2020年9月10日 上午10:09:09
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void searchSchema( Map<String , Object> retMap , Map<String , Object> params , String typeCode){
		//标签信息
		String schemaPath = params.get("rootPath") + "schema" ;
		try{
			//实体标签
			List<LabelCategoriesEntity> labelCategories = new ArrayList<LabelCategoriesEntity>() ;
			//关系标签
			List<ConnCategoriesEntity> connectionCategories = new ArrayList<ConnCategoriesEntity>() ; 
			//文件获取
			if("labelCategories".equals(typeCode)){
				labelCategories = JSONObject.parseObject(FileUtils.readJsonFile(schemaPath+File.separator + "labelCategories.json"),labelCategories.getClass()) ;
			}else if("connectionCategories".equals(typeCode)){
				connectionCategories = JSONObject.parseObject(FileUtils.readJsonFile(schemaPath+File.separator + "connectionCategories.json"),connectionCategories.getClass()) ;
			}else{
				labelCategories = JSONObject.parseObject(FileUtils.readJsonFile(schemaPath+File.separator + "labelCategories.json"),labelCategories.getClass()) ;
				connectionCategories = JSONObject.parseObject(FileUtils.readJsonFile(schemaPath+File.separator + "connectionCategories.json"),connectionCategories.getClass()) ;
			}
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("labelCategories", labelCategories) ;
			retMap.put("connectionCategories", connectionCategories) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
	/**
	 * 
	* @Title: updateMarkDatas
	* @Description: 更新标注文件信息
	* @author: FengTao
	* @date 2020年9月10日 上午11:50:24
	* @param retMap
	* @param params void
	* @version
	 */
	public void updateMarkDatas( Map<String , Object> retMap , List<NLPSentenceEntity> nlpSentenceEntities , Map<String , Object> params){
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
				nlpLabelsEntity.getSentences().set(sentenceIndex, nlpSentenceEntity) ;
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
	* @Title: readyMarkFileByUpload
	* @Description: 标注准备工作
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
		//实体标签
		List<LabelCategoriesEntity> labelCategories = new ArrayList<LabelCategoriesEntity>() ;
		//关系标签
		List<ConnCategoriesEntity> connectionCategories = new ArrayList<ConnCategoriesEntity>() ; 
		if("labelCategories".equals(typeCode)){
			FileUtils.createJsonFile(labelCategories, schemaPath+File.separator + "labelCategories.json");
		}else if("connectionCategories".equals(typeCode)){
			FileUtils.createJsonFile(connectionCategories, schemaPath+File.separator +"connectionCategories.json");
		}else{
			FileUtils.createJsonFile(labelCategories, schemaPath+File.separator + "labelCategories.json");
			FileUtils.createJsonFile(connectionCategories, schemaPath+File.separator +"connectionCategories.json");
		}
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
				FileUtils.createJsonFile(nlpLabelsEntity, targetNewFile);//创建初始化标签信息
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
	* @Title: readyMarkFileByCite
	* @Description: 预准备标注文件
	* @author: FengTao
	* @date 2020年11月26日 上午10:34:57
	* @param retMap
	* @param params void
	* @version
	 */
	public void readyMarkFileByCite( Map<String , Object> retMap , Map<String , Object> params , String typeCode){
		//标签信息
		String schemaPath = params.get("rootPath") + "schema" ;
		//实体标签
		List<LabelCategoriesEntity> labelCategories = new ArrayList<LabelCategoriesEntity>() ;
		//关系标签
		List<ConnCategoriesEntity> connectionCategories = new ArrayList<ConnCategoriesEntity>() ; 
		try{

			AnnotionEntityUtils.judgeandAddInitLabelCategories(labelCategories);
			System.out.println(""+new File(schemaPath+File.separator + "labelCategories.json").exists());
			if("labelCategories".equals(typeCode) && !new File(schemaPath+File.separator + "labelCategories.json").exists() ){
				
				FileUtils.createJsonFile(labelCategories, schemaPath+File.separator + "labelCategories.json");
			}else if("connectionCategories".equals(typeCode) && !new File(schemaPath+File.separator + "connectionCategories.json").exists() ){
				FileUtils.createJsonFile(connectionCategories, schemaPath+File.separator +"connectionCategories.json");
			}else{
				if(!new File(schemaPath+File.separator + "labelCategories.json").exists()){
					FileUtils.createJsonFile(labelCategories, schemaPath+File.separator + "labelCategories.json");
				}
				if(!new File(schemaPath+File.separator + "connectionCategories.json").exists()){
					FileUtils.createJsonFile(connectionCategories, schemaPath+File.separator +"connectionCategories.json");
				}
			}
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg","准备成功！") ;
		} catch (Exception e) {
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	
	public void savePictureLablesConnection( Map<String , Object> retMap , Map<String , Object> params , JSONArray josnArray){
		String targetPath = params.get("filePath") + "target" + FileUtils.FILE_SEPARATOR + "pictureLablesInfo.json";
		System.out.println("数据有：：：：：：：：："+targetPath);
		try{
			FileUtils.createJsonFile(josnArray,targetPath);
			retMap.putAll(RetInfo.RETSUCCESS);
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL);
			e.printStackTrace();
		}
		
	}
	
}
