package com.sunsheen.hkks.task.nlp.cct.controller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.CheckParametersUtil;
import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.nlp.common.entity.ClassifyCategoriesEntity;
import com.sunsheen.hkks.nlp.common.entity.NLPSentenceEntity;
import com.sunsheen.hkks.nlp.common.service.ClassifyCommonService;
import com.sunsheen.hkks.nlp.common.service.NLPCommonService;
import com.sunsheen.hkks.task.nlp.cct.service.CategoriesClassifyService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

@Path("/nlp/cct")
public class CategoriesClassifyController extends BaseAPI{
	
	private CategoriesClassifyService service = new CategoriesClassifyService() ;
	private NLPCommonService commonService = new NLPCommonService() ;
	private ClassifyCommonService classifyCommonService = new ClassifyCommonService() ;
	
	/**
	 * 
	* @Title: markReady
	* @Description: 标注准备
	* @author: FengTao
	* @date 2020年9月9日 下午5:13:12
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/ready/mark")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> markReady(){
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String stateCode = request.getParameter("stateCode") ;
		String fromTaskId = request.getParameter("fromTaskId") ;
		if( StringUtils.isEmptyOrWhitespaceOnly(taskId) || 
				 StringUtils.isEmptyOrWhitespaceOnly(stateCode)){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "准备失败！") ;
		}else if( "800000".equals(stateCode) ){ //以及准备过了
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "已经预先准备！") ;
		}else if( "100000".equals(stateCode) ){ //等待执行阶段才能进行任务准备
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId()) ; 
			String rootPath = commonService.getFileRootPath(params ,
									FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/"))) ;
			params.put("rootPath", rootPath) ;
			//任务文件路
			if(StringUtils.isEmptyOrWhitespaceOnly(fromTaskId)){ //文件上传
				classifyCommonService.readyMarkFileByUpload(retMap, params , "classifyCategories");
			}else{ //任务引用
				classifyCommonService.readyMarkFileByCite(retMap, params , "classifyCategories");
			}
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: saveMarkData
	* @Description: 查询标注文件信息标注文件信息
	* @author: FengTao
	* @date 2020年9月10日 上午9:22:11
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/search/markDatas")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> searchMarkData(){
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId") ; //任务ID
		String pageSize = request.getParameter("pageSize") ; //分页参数
		String pageCount = request.getParameter("pageCount") ; //分页参数
		String paths = request.getParameter("paths") ; //文件路径，同逗号隔开
		String peddleType = request.getParameter("peddleType") ; //分页方式     sliding(滑动)
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)
				|| StringUtils.isEmptyOrWhitespaceOnly(paths)){
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "查询失败！") ;
		}else{
			String dirs = "" ;
			for( String path : paths.split(",") ){
				dirs += (path+File.separator) ;
			}
			dirs = dirs.substring(0, dirs.length()-1) ;
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId()) ; 
			String rootPath = commonService.getFileRootPath(params ,
									FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/"))) ;
			params.put("rootPath", rootPath) ;
			params.put("paths", dirs ) ;
			params.put("pageSize", pageSize) ;
			params.put("pageCount", pageCount) ;
			if("sliding".equalsIgnoreCase(peddleType)){
				classifyCommonService.searchClassifyFileByContent(retMap, params);
			}else{
				service.searchClassifyFile(retMap, params);
			}
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: saveMarkData
	* @Description: 保存标注文件信息
	* @author: FengTao
	* @date 2020年9月10日 上午9:22:11
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/save/markDatas")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> saveMarkData(List<NLPSentenceEntity> nlpSentenceEntities){
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String paths = request.getParameter("paths") ; //文件路径，同逗号隔开
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId) || 
				StringUtils.isEmptyOrWhitespaceOnly(paths)){
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "更新失败！") ;
		}else{
			String dirs = "" ;
			for( String path : paths.split(",") ){
				dirs += (path+File.separator) ;
			}
			dirs = dirs.substring(0, dirs.length()-1) ;
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId()) ; 
			String rootPath = commonService.getFileRootPath(params ,
									FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/"))) ;
			params.put("rootPath", rootPath) ;
			params.put("paths", dirs ) ;
			classifyCommonService.updateClassifyDatas(retMap , nlpSentenceEntities , params ,"classifyCategories");
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: searchClassify
	* @Description: 类别查询
	* @author: FengTao
	* @date 2020年9月10日 上午11:31:33
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/search/classify")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> searchClassify(){
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "查询失败！") ;
		}else{
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId()) ; 
			String rootPath = commonService.getFileRootPath(params ,
									FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/"))) ;
			params.put("rootPath", rootPath) ;
			classifyCommonService.searchClassify(retMap, params,"classifyCategories");
		}
		return retMap ;
	}
	
	@POST
	@Path("/update/classifyCategories")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> updateLabelCategories(List<ClassifyCategoriesEntity> classifyCategories){
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "更新失败！") ;
			return retMap ;
		}
		if(classifyCategories != null && classifyCategories.size() > 0){			
			for(ClassifyCategoriesEntity entity : classifyCategories){
				String result  = CheckParametersUtil.getInstance()
						.put(entity.getId(), "id") 
						.put(entity.getCategory() , "category")
						.checkParameter() ;
				if(result != null){
					retMap.putAll(RetInfo.RETFAIL);
					retMap.put("retmsg", "更新失败！") ;
					return retMap ;
				}
			}
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId()) ; 
			String rootPath = commonService.getFileRootPath(params ,
									FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/"))) ;
			params.put("rootPath", rootPath) ;
			classifyCommonService.saveClassifyJson(retMap, classifyCategories, null, params);
		}else{
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "更新失败！") ;
			return retMap ;
		}
		return retMap ;
	}
}
