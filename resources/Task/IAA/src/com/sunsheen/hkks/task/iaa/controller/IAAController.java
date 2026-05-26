package com.sunsheen.hkks.task.iaa.controller;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sunsheen.hkks.task.iaa.service.IAAService;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;

@Path("/task")
public class IAAController extends BaseAPI {
	@GET
	@Path("/getIAA")
	@Produces(MediaType.APPLICATION_JSON)
	public Object getIAA() {
		String taskId = request.getParameter("taskId");
		String type = request.getParameter("type");
		String path = request.getSession().getServletContext().getRealPath("")
				+ File.separator + "TaskFile" + File.separator + "Train"
				+ File.separator + "Json" + File.separator + taskId;
		return new IAAService().getIAA(taskId, path, type);
	}
}
