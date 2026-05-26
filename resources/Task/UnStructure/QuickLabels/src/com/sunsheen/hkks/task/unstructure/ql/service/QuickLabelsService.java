package com.sunsheen.hkks.task.unstructure.ql.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.transform.Transformers;

import com.alibaba.fastjson.JSONObject;
import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.common.util.PageParamsUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.task.unstructure.common.entity.AnnotateEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.ConnCategoriesEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.LabelCategoriesEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.QuickLabelSentenceEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.QuickLabelsEntity;
import com.sunsheen.hkks.task.unstructure.common.utils.AnnotionEntityUtils;
import com.sunsheen.hkks.task.unstructure.common.utils.BreakSentence;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.util.DataBaseUtil;

public class QuickLabelsService {
	
	/**
	 * 
	* @Title: searchMarkFile
	* @Description: 读取文件信息
	* @author: FengTao
	* @date 2020年9月10日 上午10:01:58
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void searchMarkFile( Map<String , Object> retMap , Map<String , Object> params){
		//目标标注信息
		String targetPath = params.get("rootPath") + "target" ;
		//标签信息
		String schemaPath = params.get("rootPath") + "schema" ;
		try{
			//实体标签
			List<LabelCategoriesEntity> labelCategories = new ArrayList<LabelCategoriesEntity>() ;
			//关系标签
			List<ConnCategoriesEntity> connectionCategories = new ArrayList<ConnCategoriesEntity>() ; 
			//文件获取
			labelCategories = JSONObject.parseObject(FileUtils.ReadFile(schemaPath+File.separator + "labelCategories.json"),labelCategories.getClass()) ;
			connectionCategories = JSONObject.parseObject(FileUtils.ReadFile(schemaPath+File.separator + "connectionCategories.json"),connectionCategories.getClass()) ;
			//标注文件获取
			String path =  targetPath + File.separator + params.get("paths") ;
			String targetNewFile = path.replace(File.separator+"source"+File.separator, File.separator+"target"+File.separator) ;
			String fileFullName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1,  targetNewFile.length()) ;
			String fileName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1, targetNewFile.lastIndexOf(".")) ;
			String type = targetNewFile.substring( targetNewFile.lastIndexOf(".")+1 , targetNewFile.length()) ;
			String fileNewFullName = fileName+"-"+type+".json" ;
			targetNewFile = targetNewFile.replace( fileFullName , fileNewFullName ) ;
			QuickLabelsEntity quickLabelsEntity = JSONObject.parseObject( FileUtils.ReadFile( targetNewFile ),QuickLabelsEntity.class ) ;

			List<QuickLabelSentenceEntity> quickLabelSentences = new ArrayList<QuickLabelSentenceEntity>() ;
			quickLabelSentences.addAll(quickLabelsEntity.getSentences()) ;
			Integer totalCount = quickLabelSentences.size() ;
			PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), (String)params.get("pageSize"), totalCount+"");
			//分页截取
			Integer startIndex = Integer.parseInt(params.get("pageIndex")+"");
			Integer endIndex = (Integer.parseInt(params.get("pageIndex")+"")+Integer.parseInt(params.get("pageSize")+"") ) > quickLabelSentences.size() ? quickLabelSentences.size() :
				 Integer.parseInt(params.get("pageIndex")+"")+Integer.parseInt(params.get("pageSize")+"");
			quickLabelSentences = quickLabelSentences.subList(startIndex , endIndex) ;
			for(QuickLabelSentenceEntity sentence : quickLabelSentences){
				sentence.getAnnotation().setLabelCategories(labelCategories);
				sentence.getAnnotation().setConnectionCategories(connectionCategories);
				//初始化json
				AnnotionEntityUtils.judgeandAddInitLabelCategories(sentence.getAnnotation());
			}
			if( quickLabelSentences.size() > 0  ){
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}else{
				retMap.putAll(RetInfo.RETFAIL) ;
			}
			retMap.put("data", quickLabelSentences) ;
			retMap.put("totalCount", totalCount) ;
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
	public void updateMarkDatas( Map<String , Object> retMap , List<QuickLabelSentenceEntity> quickLabelList , Map<String , Object> params){
		//标签信息
		String targetPath = params.get("rootPath") + "target" ;
		try{
			
			String path =  targetPath + File.separator + params.get("paths") ;
			System.out.print(path);
			String targetNewFile = path.replace(File.separator+"source"+File.separator, File.separator+"target"+File.separator) ;
			String fileFullName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1,  targetNewFile.length()) ;
			String fileName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1, targetNewFile.lastIndexOf(".")) ;
			String type = targetNewFile.substring( targetNewFile.lastIndexOf(".")+1 , targetNewFile.length()) ;
			String fileNewFullName = fileName+"-"+type+".json" ;
			targetNewFile = targetNewFile.replace( fileFullName , fileNewFullName ) ;
			
			QuickLabelsEntity quickLabelsEntity = JSONObject.parseObject( FileUtils.ReadFile( targetNewFile ),QuickLabelsEntity.class ) ;
			for( QuickLabelSentenceEntity quickLabel : quickLabelList ){
				Integer sentenceIndex = Integer.parseInt( quickLabel.getSentenceIndex() ) ;
				quickLabelsEntity.getSentences().set(sentenceIndex, quickLabel) ;
			}
			//更新标注文件信息
			FileUtils.createJsonFile(quickLabelsEntity, targetNewFile) ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
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
	public void searchSchema( Map<String , Object> retMap , Map<String , Object> params){
		//标签信息
		String schemaPath = params.get("rootPath") + "schema" ;
		try{
			//实体标签
			List<LabelCategoriesEntity> labelCategories = new ArrayList<LabelCategoriesEntity>() ;
			//关系标签
			List<ConnCategoriesEntity> connectionCategories = new ArrayList<ConnCategoriesEntity>() ; 
			//文件获取
			labelCategories = JSONObject.parseObject(FileUtils.ReadFile(schemaPath+File.separator + "labelCategories.json"),labelCategories.getClass()) ;
			connectionCategories = JSONObject.parseObject(FileUtils.ReadFile(schemaPath+File.separator + "connectionCategories.json"),connectionCategories.getClass()) ;
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
	* @Title: readyMarkFile
	* @Description: 标注准备，生成预标注文件信息 ，纯文件操作
	* @author: FengTao
	* @date 2020年9月9日 下午5:38:06
	* @param retMap
	* @param params void
	* @version
	 */
	public void readyMarkFile( Map<String , Object> retMap , Map<String , Object> params ) {
		
		//源标注信息
		String sourcePath = params.get("rootPath") + "source" ;
		//目标标注信息
		String targetPath = params.get("rootPath") + "target" ;
		//标签信息
		String schemaPath = params.get("rootPath") + "schema" ;
		//实体标签
		List<LabelCategoriesEntity> labelCategories = new ArrayList<LabelCategoriesEntity>() ;
		//关系标签
		List<ConnCategoriesEntity> connectionCategories = new ArrayList<ConnCategoriesEntity>() ; 
		
		try {
			//复制目录信息到target
			com.sunsheen.jfids.commons.io.FileUtils.copyDirectory( new File(sourcePath) , new File(targetPath) ,true );
			//创建初始化标签信息
			FileUtils.createJsonFile(labelCategories, schemaPath+File.separator + "labelCategories.json");
			FileUtils.createJsonFile(connectionCategories, schemaPath+File.separator +"connectionCategories.json");
			//遍历目标文件列表获取所有文件（不获取文件夹），为每个文件重新构建目标文件
			List<String> files = FileUtils.getAllFile(targetPath, true) ;
			for(String targetFile : files){
				StringBuffer content =  new StringBuffer() ;
				//文件信息的实体
				QuickLabelsEntity quickLabelsEntity = new QuickLabelsEntity() ; 
				List<QuickLabelSentenceEntity> quickLabelSentences = new ArrayList<QuickLabelSentenceEntity>() ;
				String targetNewFile = targetFile.replace(File.separator+"source"+File.separator, File.separator+"target"+File.separator) ;
				String fileFullName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1,  targetNewFile.length()) ;
				String fileName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1, targetNewFile.lastIndexOf(".")) ;
				String type = targetNewFile.substring( targetNewFile.lastIndexOf(".")+1 , targetNewFile.length()).toLowerCase();
				String fileNewFullName = fileName+"-"+type+".json" ;
				targetNewFile = targetNewFile.replace( fileFullName , fileNewFullName ) ;
				
				if("txt".equals(type)){ //txt文本分句
					content.append(FileUtils.ReadFile(targetFile)) ;
					//删除文件
					FileUtils.deleteFile(targetFile) ; 
					//整个文本读取
					String[] sentences = BreakSentence.splitSentence(content.toString()) ;
					AtomicInteger  sentenceIndex = new AtomicInteger(0) ;
					for(String sentence : sentences){
						QuickLabelSentenceEntity quickLabelSentenceEntity = new QuickLabelSentenceEntity() ;
						AnnotateEntity annotation = new AnnotateEntity() ;
						annotation.setContent(sentence);
						annotation.setLabelCategories(new ArrayList<>());
						annotation.setConnectionCategories(new ArrayList<>());
						annotation.setLabels(new ArrayList<>());
						annotation.setConnections(new ArrayList<>());
						quickLabelSentenceEntity.setId(UUID.randomUUID().toString());
						quickLabelSentenceEntity.setSentenceIndex(sentenceIndex.getAndIncrement()+"");
						quickLabelSentenceEntity.setStartIndex("0");
						quickLabelSentenceEntity.setEndIndex("1");
						quickLabelSentenceEntity.setAnnotation(annotation);
						quickLabelSentences.add(quickLabelSentenceEntity) ;
					}
				}else if("csv".equals(type)) { //csv按行读取
					//逐行读取
					List<String> sentences = FileUtils.readFileByLine(targetFile) ;
					//删除文件
					FileUtils.deleteFile(targetFile) ; 
					AtomicInteger  sentenceIndex = new AtomicInteger(0) ;
					for(String sentence : sentences){
						content.append(sentence) ;
						List<LabelCategoriesEntity> labelCategories01 = new ArrayList<LabelCategoriesEntity>() ;
						//初始化标签信息
						AnnotionEntityUtils.judgeandAddInitLabelCategories(labelCategories01) ; 
						QuickLabelSentenceEntity quickLabelSentenceEntity = new QuickLabelSentenceEntity() ;
						AnnotateEntity annotation = new AnnotateEntity() ;
						annotation.setContent(sentence);
						annotation.setLabelCategories(labelCategories01);
						annotation.setConnectionCategories(connectionCategories);
						annotation.setLabels(new ArrayList<>());
						annotation.setConnections(new ArrayList<>());
						quickLabelSentenceEntity.setId(UUID.randomUUID().toString());
						quickLabelSentenceEntity.setSentenceIndex(sentenceIndex.getAndIncrement()+"");
						quickLabelSentenceEntity.setStartIndex("20");
						quickLabelSentenceEntity.setEndIndex("13");
						quickLabelSentenceEntity.setAnnotation(annotation);
						quickLabelSentences.add(quickLabelSentenceEntity) ;
					}
				}else if("doc".equals(type) || "docx".equals(type)){
					content.append(FileUtils.readWord(targetFile)) ;
					//删除文件
					FileUtils.deleteFile(targetFile) ; 
					//整个文本读取
//					String[] sentences = BreakSentence.splitSentence(content.toString()) ;
					AtomicInteger  sentenceIndex = new AtomicInteger(0) ;
					String sentence = content.toString();
//					for(String sentence : sentences){
						QuickLabelSentenceEntity quickLabelSentenceEntity = new QuickLabelSentenceEntity() ;
						AnnotateEntity annotation = new AnnotateEntity() ;
						annotation.setContent(sentence);
						annotation.setLabelCategories(new ArrayList<>());
						annotation.setConnectionCategories(new ArrayList<>());
						annotation.setLabels(new ArrayList<>());
						annotation.setConnections(new ArrayList<>());
						quickLabelSentenceEntity.setId(UUID.randomUUID().toString());
						quickLabelSentenceEntity.setSentenceIndex(sentenceIndex.getAndIncrement()+"");
						quickLabelSentenceEntity.setStartIndex("0");
						quickLabelSentenceEntity.setEndIndex("1");
						quickLabelSentenceEntity.setAnnotation(annotation);
						quickLabelSentences.add(quickLabelSentenceEntity) ;
//					}
				}else{
					System.out.print("不满足该文件！");
				}
				quickLabelsEntity.setId(UUID.randomUUID().toString());
				quickLabelsEntity.setContent("");
				quickLabelsEntity.setSourceName(fileFullName);
				quickLabelsEntity.setSentences(quickLabelSentences);
				//构件标注文件信息
				FileUtils.createJsonFile(quickLabelsEntity, targetNewFile);
			}
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg","准备成功！") ;
		} catch (IOException e) {
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	* @Title: saveOrUpdateTaskInfo
	* @Description: 新增或更新任务
	* @author: FengTao
	* @date 2020年9月9日 上午11:10:04
	* @param retMap
	* @param params void
	* @version
	 */
	public void saveOrUpdateTaskInfo( Map<String , Object> retMap , Map<String , Object> params ){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try{
			//查询该任务是否存在
			Object num = session.createDySQLQuery("QL.queryTaskCountByTaskId", params).uniqueResult();
			Object num01 = "0" ;
			if( "0".equals(num.toString()) ){
				num01 = session.createDySQLQuery("QL.insertTask", params).executeUpdate();
			}else if( "1".equals(num.toString()) ){ //updateTask
				num01 = session.createDySQLQuery("QL.updateTask", params).executeUpdate();
			}
			if( "0".equals(num01.toString()) ){
				retMap.putAll(RetInfo.RETFAIL) ;
			}else{
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
	
	
	/**
	 * 
	* @Title: searchTaskInfo
	* @Description:查询任务详细信息
	* @author: FengTao
	* @date 2020年9月11日 上午9:54:07
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void searchTaskInfo( Map<String , Object> retMap , Map<String , Object> params ){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String , Object> data = new HashMap<String,Object>() ;
		//源标注信息
		String sourcePath = params.get("rootPath") + "source" ;
		try{
			//查询该任务
			data = (Map<String, Object>) session.createDySQLQuery("QL.queryTaskInfoById", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult();
			File[] files = new File(sourcePath).listFiles() ;
			List<String> fileNames = new ArrayList<>() ;
			for(File file : files){
				if(file.isDirectory()){
					fileNames.add(file.toString().substring(file.toString().lastIndexOf(File.separator)+1, file.toString().length())+".zip") ;
				}else{
					fileNames.add(file.toString().substring(file.toString().lastIndexOf(File.separator)+1, file.toString().length())) ;
				}
			}
			data.put("files", fileNames) ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("data", data) ;
			if( data != null && data.size() > 0 ){
				retMap.put("retmsg","查询成功!") ;
			}else{
				retMap.put("retmsg","查询成功，无数据!") ;
			}
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
	
	/**
	 * 
	* @Title: updateTaskState
	* @Description: 更新任务状态
	* @author: FengTao
	* @date 2020年9月9日 上午11:09:50
	* @param retMap
	* @param params void
	* @version
	 */
	public void updateTaskState( Map<String , Object> retMap , Map<String , Object> params){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		try{
			Object num01 = session.createDySQLQuery("QL.updateTaskState", params).executeUpdate();
			if( "0".equals(num01.toString()) ){
				retMap.putAll(RetInfo.RETFAIL) ;
			}else{
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}
			System.out.print(params.get("stateCode"));
			if( "300000".equals(params.get("stateCode")+"") ){ //任务开始，则修改为结束时间
				session.createDySQLQuery("Task.updateTaskStartTime", params).executeUpdate() ;
			}
			if( "400000".equals(params.get("stateCode")+"") || "500000".equals(params.get("stateCode")+"") ){ //任务结束，则修改为结束时间
				session.createDySQLQuery("Task.updateTaskEndTime", params).executeUpdate() ;
			}
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
	
}
