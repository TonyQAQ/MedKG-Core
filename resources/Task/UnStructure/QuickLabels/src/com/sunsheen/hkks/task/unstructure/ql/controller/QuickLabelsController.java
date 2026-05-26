package com.sunsheen.hkks.task.unstructure.ql.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONReader;
import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.uitl.zip.ZipUtils;
import com.sunsheen.hkks.common.util.CheckParametersUtil;
import com.sunsheen.hkks.common.util.FileNode;
import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.task.unstructure.common.entity.ConnCategoriesEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.LabelCategoriesEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.QuickLabelSentenceEntity;
import com.sunsheen.hkks.task.unstructure.common.utils.FileUploadUtils;
import com.sunsheen.hkks.task.unstructure.ql.service.QuickLabelsService;
import com.sunsheen.jfids.commons.fileupload.FileItem;
import com.sunsheen.jfids.commons.fileupload.FileItemFactory;
import com.sunsheen.jfids.commons.fileupload.FileUploadException;
import com.sunsheen.jfids.commons.fileupload.disk.DiskFileItemFactory;
import com.sunsheen.jfids.commons.fileupload.servlet.ServletFileUpload;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

@Path("/task/us/ql")
public class QuickLabelsController extends BaseAPI {
	
	private QuickLabelsService quickLabelsService = new QuickLabelsService() ;

	/**
	 * 新增或更新任务信息
	 * @return
	 */
	@POST
	@Path("/save/task")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> addOrUpdateTask(){
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String taskName = request.getParameter("taskName") ;
		String startTime = request.getParameter("startTime") ;
		String endTime = request.getParameter("endTime") ;
		String typeCode = request.getParameter("typeCode") ;
		String memo = request.getParameter("memo") ;
		List<FileItem> items = null;
		if (ServletFileUpload.isMultipartContent(request)) {
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			try {
				items = upload.parseRequest(request);
			} catch (FileUploadException e) {
				e.printStackTrace();
			}
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(taskName)
				|| StringUtils.isEmptyOrWhitespaceOnly(startTime)
					|| StringUtils.isEmptyOrWhitespaceOnly(endTime)
						|| ( StringUtils.isEmptyOrWhitespaceOnly(taskId) && (items==null || items.size() <= 0)) ){
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "请检查表单") ;
			return retMap ;
		}else{
			if( StringUtils.isEmptyOrWhitespaceOnly(taskId) || "null".equals(taskId)){
				taskId = UUID.randomUUID().toString().replace("-", "") ;
			}
			String filePath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
					+ "TaskFile" + FileUtils.FILE_SEPARATOR + "QuickLabels" + FileUtils.FILE_SEPARATOR + taskId + FileUtils.FILE_SEPARATOR);
			System.out.print("filePath   "+filePath );
			params.put("taskId", taskId) ;
			params.put("taskName", taskName) ;
			params.put("startTime", startTime) ;
			params.put("endTime", endTime) ;
			params.put("typeCode", typeCode) ;
			params.put("filePath", filePath) ; //文件上传的路径信息
			params.put("memo", memo) ;
			params.put("userId", Session.getCurrUser().getId()) ; 
			params.put("userName", Session.getCurrUser().getUsername()) ; 
			if( ( !StringUtils.isEmptyOrWhitespaceOnly(taskId) && (items==null || items.size() <= 0)) ){
				//先进行文件上传，再进行任务修改或新增,如果任务ID不为空并且，item个数为0,则为编辑
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}else{ //否则进行文件更新
				FileUploadUtils.uploadFilesService(items, retMap, params) ;
			}
			if("01".equals(retMap.get("retcode"))){
				quickLabelsService.saveOrUpdateTaskInfo(retMap, params);
			}
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: serachTaskById
	* @Description: 根据任务ID查询任务信息，用于编辑
	* @author: FengTao
	* @date 2020年9月11日 上午9:42:42
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/search/taskEditInfo")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> serachTaskById() {
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId") ;
		if( StringUtils.isEmptyOrWhitespaceOnly(taskId) ){
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "查询失败") ;
			return retMap ;
		}
		String rootPath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
				+ "TaskFile" + FileUtils.FILE_SEPARATOR + "QuickLabels" + FileUtils.FILE_SEPARATOR + taskId + FileUtils.FILE_SEPARATOR);
		params.put("taskId", taskId) ;
		params.put("rootPath", rootPath) ;
		params.put("userId", Session.getCurrUser().getId()) ; 
		quickLabelsService.searchTaskInfo(retMap , params) ;
		
		JSONArray array = null;
		String sourcePath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
				+ "TaskFile" + FileUtils.FILE_SEPARATOR + "QuickLabels" + FileUtils.FILE_SEPARATOR + taskId + FileUtils.FILE_SEPARATOR + "schema"  + FileUtils.FILE_SEPARATOR +"labelCategories.json" ) ;
		try{
			File file = new File(sourcePath);
		    FileReader fileReader = new FileReader(file);
		    JSONReader jsonReader = new JSONReader(fileReader);
			 array =  JSON.parseArray(jsonReader.readString());
			jsonReader.close();
		}catch(Exception e){
			array = new JSONArray();
		}
		retMap.put("lables", array);
		return retMap ;
	}
	
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
		if( StringUtils.isEmptyOrWhitespaceOnly(taskId) || 
				 StringUtils.isEmptyOrWhitespaceOnly(stateCode)){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "准备失败！") ;
		}else if( "300000".equals(stateCode) ){ //以及准备过了
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "已经预先准备！") ;
		}else if( "100000".equals(stateCode) ){ //等待执行阶段才能进行任务准备
			String rootPath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
							+ "TaskFile" + File.separator + "QuickLabels" + File.separator + taskId + File.separator );
			params.put("taskId", taskId) ;
			params.put("rootPath", rootPath) ;
			//任务文件路
			quickLabelsService.readyMarkFile(retMap, params);
		}
		return retMap ;
	}
	
	@POST
	@Path("/ready/markPicture")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> markPictureReady(){
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String stateCode = request.getParameter("stateCode") ;
		if( StringUtils.isEmptyOrWhitespaceOnly(taskId) || 
				 StringUtils.isEmptyOrWhitespaceOnly(stateCode)){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "准备失败！") ;
		}else if( "300000".equals(stateCode) ){ //以及准备过了
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "已经预先准备！") ;
		}else if( "100000".equals(stateCode) ){ //等待执行阶段才能进行任务准备
			retMap.putAll(RetInfo.RETSUCCESS) ;
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
			String rootPath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
					+ "TaskFile" + File.separator + "QuickLabels" + File.separator + taskId + File.separator );
			params.put("taskId", taskId) ;
			params.put("paths", dirs ) ;
			params.put("rootPath", rootPath) ;
			params.put("pageSize", pageSize) ;
			params.put("pageCount", pageCount) ;
			
			quickLabelsService.searchMarkFile(retMap, params);
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
	@Path("/update/markDatas")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> saveMarkData(List<QuickLabelSentenceEntity> quickLabelList){
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
			String rootPath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
					+ "TaskFile" + File.separator + "QuickLabels" + File.separator + taskId + File.separator );
			params.put("rootPath", rootPath) ;
			params.put("paths", dirs ) ;
			quickLabelsService.updateMarkDatas(retMap , quickLabelList , params);
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: searchSchema
	* @Description: schema查询
	* @author: FengTao
	* @date 2020年9月10日 上午11:31:33
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/search/schema")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> searchSchema(){
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "查询失败！") ;
		}else{
			params.put("taskId", taskId) ;
			String rootPath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
					+ "TaskFile" + File.separator + "QuickLabels" + File.separator + taskId + File.separator );
			params.put("rootPath", rootPath) ;
			quickLabelsService.searchSchema(retMap, params);
		}
		return retMap ;
	}
	
	@POST
	@Path("/update/labelCategories")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> updateLabelCategories(List<LabelCategoriesEntity> labelCategories){
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETFAIL) ;
			retMap.put("retmsg", "更新失败！") ;
			return retMap ;
		}
		if(labelCategories != null && labelCategories.size() > 0){			
			for(LabelCategoriesEntity entity : labelCategories){
				String result  = CheckParametersUtil.getInstance()
						.put(entity.getColor() , "color") 
						.put(entity.getId(), "id") 
						.put(entity.getText(), "text")
						.checkParameter() ;
				if(result != null){
					retMap.putAll(RetInfo.RETFAIL);
					retMap.put("retmsg", "更新失败！") ;
					return retMap ;
				}
				entity.setBorderColor("#"+randomHexStr(6));
			}
			params.put("taskId", taskId) ;
			String rootPath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
					+ "TaskFile" + File.separator + "QuickLabels" + File.separator + taskId + File.separator );
			params.put("rootPath", rootPath) ;
			quickLabelsService.saveSchemaJson(retMap, labelCategories, null, params);
		}else{
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "更新失败！") ;
			return retMap ;
		}
		return retMap ;
	}
	
	@POST
	@Path("/update/connectionCategories")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String,Object> updateConnectionCategories(List<ConnCategoriesEntity> connectionCategories){
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "更新失败！") ;
			return retMap ;
		}
		if(connectionCategories != null && connectionCategories.size() > 0){			
			for(ConnCategoriesEntity entity : connectionCategories){
				String result  = CheckParametersUtil.getInstance()
						.put(entity.getId(), "id") 
						.put(entity.getText(), "text")
						.checkParameter() ;
				if(result != null){
					retMap.putAll(RetInfo.RETFAIL);
					retMap.put("retmsg", "更新失败！") ;
					return retMap ;
				}
			}
			params.put("taskId", taskId) ;
			String rootPath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
					+ "TaskFile" + File.separator + "QuickLabels" + File.separator + taskId + File.separator );
			params.put("rootPath", rootPath) ;
			quickLabelsService.saveSchemaJson(retMap, null, connectionCategories, params);
		}else{
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "更新失败！") ;
			return retMap ;
		}
		
		return retMap ;
	}
	
	/**
	 * 新增或更新任务信息
	 * @return
	 */
	@POST
	@Path("/update/state")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> updateTaskState(){
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String stateCode = request.getParameter("stateCode") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId)
				|| StringUtils.isEmptyOrWhitespaceOnly(stateCode)){
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "更新失败！") ;
		}else{
			params.put("taskId", taskId) ;
			params.put("stateCode", stateCode) ;
			quickLabelsService.updateTaskState(retMap, params);
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: updateTaskState
	* @Description: 获取完整文件树信息
	* @author: FengTao
	* @date 2020年9月9日 上午11:15:25
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/search/tree")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> getFileTree(){
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId") ;
		FileNode tree = new FileNode() ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskId) ){
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "查询失败！") ;
		}else{
			params.put("taskId", taskId) ;
			String filePath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
					+ "TaskFile" + File.separator + "QuickLabels" + File.separator + taskId + File.separator + "source" + File.separator );
			FileUtils.genDirTree(filePath, 0, "" , tree );
			retMap.putAll(RetInfo.RETSUCCESS);
			retMap.put("tree",  tree.getChildren() ) ;
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: getFileTree
	* @Description: 文件下载
	* @author: FengTao
	* @date 2020年9月10日 下午2:39:34
	* @return Map<String,Object>
	* @version
	 */
	@GET
	@POST
	@Path("/download/files")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> downloadFiles(){
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId") ;
		String taskName = request.getParameter("taskName") ;
		System.out.print(taskName);
		if( StringUtils.isEmptyOrWhitespaceOnly(taskId) ||
				StringUtils.isEmptyOrWhitespaceOnly(taskName)){
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "下载失败！") ;
			return retMap ;
		}
		String rootPath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
					+ "TaskFile" + File.separator + "QuickLabels" + File.separator + taskId + File.separator ); // + "target" + File.separator );
		File[] files = new File(rootPath).listFiles() ;
		if (files.length == 1 && !files[0].isDirectory()) { //只有一个文件
			OutputStream toClient = null ;
			try {
				String content = FileUtils.ReadFile(files[0].getAbsolutePath()) ;
				response.setContentType("application/octet-stream");
				response.addHeader("Content-Disposition","attachment;filename=" + new String( taskName.getBytes("gb2312"), "ISO8859-1" ) +".json");
				toClient = new BufferedOutputStream(response.getOutputStream());
				toClient.write(content.getBytes());
				toClient.flush();	
				toClient.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				retMap.putAll(RetInfo.RETFAIL);
				retMap.put("retmsg", "文件下载失败！");
			}
		} else {//多文件下载，组装成ZIP
			zip(new File(rootPath),taskName,response) ;
		}
		return retMap ;
	}
	
	/**
	 * 
	* @Title: zip
	* @Description:
	* @author: FengTao
	* @date 2020年9月10日 下午4:26:09
	* @param file
	* @param fileName
	* @param response void
	* @version
	 */
	public static void zip(File file , String fileName , HttpServletResponse response){
		ZipOutputStream out = null;
		BufferedOutputStream bos = null;
		try {
			//将zip以流的形式输出到前台
			response.setContentType("application/octet-stream");
			response.setHeader(
						"Content-Disposition","attachment;fileName="+ new String( fileName.getBytes("gb2312"), "ISO8859-1" )+System.currentTimeMillis() + ".zip");
			//创建zip输出流
			out = new ZipOutputStream(response.getOutputStream());
			//创建缓冲输出流
			bos = new BufferedOutputStream(out);
			//调用压缩函数
			ZipUtils.compress(out, bos, file , fileName , "source");
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
				bos.close();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
	}
	
	/**
     * 随机颜色
     *
     * @param len
     * @return
     */
    public static String randomHexStr(int len) {
        try {
            StringBuffer result = new StringBuffer();
            for (int i = 0; i < len; i++) {
                //随机生成0-15的数值并转换成16进制
                result.append(Integer.toHexString(new Random().nextInt(16)));
            }
            return result.toString().toUpperCase();
        } catch (Exception e) {
            System.out.println("获取16进制字符串异常，返回默认...");
            return "00CCCC";
        }
    }
}
