package com.sunsheen.hkks.task.nlp.sst.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.nlp.common.service.NLPCommonService;
import com.sunsheen.hkks.task.nlp.sst.service.SentenceSegmentService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

@Path("/nlp/sst")
public class SentenceSegmentController extends BaseAPI{
	
	private SentenceSegmentService service = new SentenceSegmentService() ;
	private NLPCommonService commonService = new NLPCommonService() ;
	
	/**
	 * 
	* @Title: markReady
	* @Description: 句子切分标注准备
	* @author: FengTao
	* @date 2020年9月9日 下午5:13:12
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/ready/sentences")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> sentencesReady(){
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String stateCode = request.getParameter("stateCode") ;
		if( StringUtils.isEmptyOrWhitespaceOnly(taskId) || 
				 StringUtils.isEmptyOrWhitespaceOnly(stateCode)){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "准备失败！") ;
		}else if( "600000".equals(stateCode) ){ //以及准备过了,已经进入了句子切分阶段
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "已经预先准备！") ;
		}else if( "100000".equals(stateCode) ){ //等待执行阶段才能进行任务准备
			params.put("taskId", taskId) ;
			params.put("userId", Session.getCurrUser().getId()) ; 
			String rootPath = commonService.getFileRootPath(params ,
									FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/"))) ;
			params.put("rootPath", rootPath) ;
			//任务文件路
			service.readySentencesFile(retMap, params);
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: saveSentences
	* @Description: 保存分句文件信息
	* @author: FengTao
	* @date 2020年9月10日 上午9:22:11
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/save/sentences")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> saveSentences(){
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String paths = request.getParameter("paths") ; //文件路径，同逗号隔开
		String indexs = request.getParameter("indexs") ;
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
			params.put("indexs", indexs) ;
			service.saveSentences(retMap , params);
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: searchSentences
	* @Description: 查询分句信息
	* @author: FengTao
	* @date 2020年9月10日 上午9:22:11
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/search/sentences")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> searchSentences(){
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId") ; //任务ID
		String pageSize = request.getParameter("pageSize") ; //分页参数
		String pageCount = request.getParameter("pageCount") ; //分页参数
		String paths = request.getParameter("paths") ; //文件路径，同逗号隔开
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
			params.put("taskId", taskId) ;
			params.put("paths", dirs ) ;
			params.put("rootPath", rootPath) ;
			params.put("pageSize", pageSize) ;
			params.put("pageCount", pageCount) ;
			service.searchSentencesFile(retMap, params);
		}
		return retMap ;
	}
}
