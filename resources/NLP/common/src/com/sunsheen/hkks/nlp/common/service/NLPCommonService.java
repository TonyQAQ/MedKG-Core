package com.sunsheen.hkks.nlp.common.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.hibernate.transform.Transformers;

import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.jfids.system.config.Configs;
import com.sunsheen.jfids.system.database.DBSession;
import com.sunsheen.jfids.util.DataBaseUtil;

public class NLPCommonService {
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
			Object num = session.createDySQLQuery("NLPTask.queryTaskCountByTaskId", params).uniqueResult();
			Object num01 = "0" ;
			if( "0".equals(num.toString()) ){
				num01 = session.createDySQLQuery("NLPTask.insertTask", params).executeUpdate();
			}else if( "1".equals(num.toString()) ){ //updateTask
				num01 = session.createDySQLQuery("NLPTask.updateTask", params).executeUpdate();
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
		//根路径
		String rootPath = params.get("rootPath")+"" ;
		//源标注信息
		String sourcePath = params.get("rootPath") + "source"+File.separator ;
		try{
			//查询该任务
			data = (Map<String, Object>) session.createDySQLQuery("NLPTask.queryTaskInfoById", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult();
			retMap.putAll(RetInfo.RETSUCCESS) ;
			if( data != null && data.size() > 0 ){
				List<String> fileNames = new ArrayList<>() ;
				//	拼接文件路径信息
				File[] rootFiles = new File(rootPath).listFiles() ;
				if(rootFiles != null){
					for(File file : rootFiles){
						if(file.isFile() && file.toString().endsWith(".zip")){
							fileNames.add( file.getName() ) ;
						}
					}
				}
				//	没有压缩文件，则读取source目录下的文件信息
				if(fileNames.size() <= 0){
					//拼接文件路径信息
					File[] files = new File(sourcePath).listFiles() ;
					if(files != null)
					for(File file : files){
						if(!file.isDirectory()){
							fileNames.add( file.getName() ) ;
						}
					}
				}
				
				data.put("files", fileNames) ;
				retMap.put("retmsg","查询成功!") ;
			}else{
				retMap.put("retmsg","查询成功，无数据!") ;
			}
			retMap.put("data", data) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
	
	/**
	 * 
	* @Title: searchTaskTables
	* @Description:查询任务的下拉列表信息
	* @author: FengTao
	* @date 2020年9月11日 上午9:54:07
	* @param retMap
	* @param params void
	* @version
	 */
	@SuppressWarnings("unchecked")
	public void searchTaskTables( Map<String , Object> retMap , Map<String , Object> params ){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		List<Map<String , Object>> dataList = new ArrayList<Map<String,Object>>() ;
		retMap.put("titleHead", JSONArray.fromObject(Configs.get("Config.TaskTablesHead")));
		try{
			//查询该任务
			dataList = session.createDySQLQuery("NLPTask.queryTaskTables", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
			retMap.putAll(RetInfo.RETSUCCESS) ;
			if( dataList != null && dataList.size() > 0 ){
				int sequence = 0 ;
				for(Map<String,Object> temp : dataList){
					sequence ++ ;
					temp.put("sequence", sequence) ;
					temp.put("rowKey", temp.get("taskId")) ;
				}
				retMap.put("retmsg","查询成功!") ;
			}else{
				retMap.put("retmsg","查询成功，无数据!") ;
			}
			retMap.put("data", dataList) ;
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
			Object num01 = session.createDySQLQuery("NLPTask.updateTaskState", params).executeUpdate();
			if( "0".equals(num01.toString()) ){
				retMap.putAll(RetInfo.RETFAIL) ;
			}else{
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}
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
	
	@SuppressWarnings("unchecked")
	public String getFileRootPath( Map<String , Object> params , String rootPath ){
		DBSession session = DataBaseUtil.getStatelessHibernateSession();
		Map<String , Object> data = new HashMap<String,Object>() ;
		try{
			//查询该任务
			data = (Map<String, Object>) session.createDySQLQuery("NLPTask.queryTaskInfoById", params).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).uniqueResult();
			if( data != null && data.size() > 0 ){
				params.put("taskName", data.get("taskName")) ;
				params.put("typeCode", data.get("typeCode")) ;
				String dayInfo = data.get("lastUpdateDate").toString().substring(0, data.get("lastUpdateDate").toString().indexOf(" ")) ;
				rootPath += ("TaskFile" + File.separator + "NLP" + File.separator
							+  data.get("typeCode")+ File.separator 
							+  dayInfo + File.separator + data.get("taskId")+File.separator) ;
			}else{
				return "" ;
			}
		}catch(Exception e){
			e.printStackTrace();
		}	
		return rootPath ;
	}
}
