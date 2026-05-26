package com.sunsheen.hkks.task.seqlabel.controller;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;



import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.task.seqlabel.service.LabelService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;

@Path("/label")
public class LabelController extends BaseAPI{
	@GET
	@Path("/model/info")
	@Produces(MediaType.APPLICATION_JSON)
	public Object getModelInfo() {
		return new LabelService().getModelInfo();
	}
	
	@GET
	@Path("/task/model/save")
	@Produces(MediaType.APPLICATION_JSON)
	public Object saveTaskModel() {
		Map<String, Object> retMap = new HashMap<String, Object>();
		Map<String, Object> param = new HashMap<>();
		String taskId = request.getParameter("taskId");
		String modelId = request.getParameter("modelId");
		retMap.putAll(RetInfo.RETSUCCESS);
		param.put("taskId", taskId);
		param.put("modelId", modelId);
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId) || StringUtils.isEmptyOrWhitespaceOnly(modelId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			new LabelService().saveTaskModel(param, retMap);
		}
		return retMap;
	}
	
	@GET
	@Path("/task/result")
	@Produces(MediaType.APPLICATION_JSON)
	public Object saveLabelResult() {
		Map<String, Object> retMap = new HashMap<String, Object>();
		Map<String, Object> param = new HashMap<>();
		String taskId = request.getParameter("taskId");
		String state = request.getParameter("state");
		retMap.putAll(RetInfo.RETSUCCESS);
		param.put("taskId", taskId);
		param.put("state", state);
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId) || StringUtils.isEmptyOrWhitespaceOnly(state)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			new LabelService().saveLabelResult(param, retMap);
		}
		return retMap;
	}
	
	@GET
	@Path("/task/start")
	@Produces(MediaType.APPLICATION_JSON)
	public Object startTask() {
		Map<String, Object> retMap = new HashMap<String, Object>();
		Map<String, Object> param = new HashMap<>();
		String taskId = request.getParameter("taskId");
		String model = request.getParameter("model");
		retMap.putAll(RetInfo.RETSUCCESS);
		param.put("taskId", taskId);
		param.put("model", model);
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL);
		}else{
			new LabelService().startTask(param, retMap);
		}
		return retMap;
	}
}
