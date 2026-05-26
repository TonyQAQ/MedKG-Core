package com.sunsheen.hkks.nlp.common.api;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
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
import com.sunsheen.hkks.common.util.FileNode;
import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.nlp.common.service.NLPCommonService;
import com.sunsheen.hkks.nlp.common.utils.FileUploadUtils;
import com.sunsheen.jfids.commons.fileupload.FileItem;
import com.sunsheen.jfids.commons.fileupload.FileItemFactory;
import com.sunsheen.jfids.commons.fileupload.FileUploadException;
import com.sunsheen.jfids.commons.fileupload.disk.DiskFileItemFactory;
import com.sunsheen.jfids.commons.fileupload.servlet.ServletFileUpload;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;

/**
 * 
 * @Title: NLPCommonAPI
 * @Description:自然语言处理过程中,任务创建的公用API
 * @author: FengTao
 * @date 2020年11月23日 下午3:38:24
 */
@Path("/nlp/common")
public class NLPCommonAPI extends BaseAPI {

	private NLPCommonService service = new NLPCommonService() ;
	
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
			params.put("userId", Session.getCurrUser().getId()) ;
			String rootPath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/"));
			rootPath = ( service.getFileRootPath(params , rootPath)+"target"+File.separator );
			FileUtils.genDirTree(rootPath, 0, "" , tree );
			retMap.putAll(RetInfo.RETSUCCESS);
			retMap.put("tree",  tree.getChildren() ) ;
		}
		return retMap ;
	}
	/**
	 * 
	* @Title: queryFromTaskList
	* @Description: 查询标注信息来源码表
	* @author: FengTao
	* @date 2020年11月25日 上午11:11:32
	* @return Map<String,Object>
	* @version
	 */
	@POST
	@Path("/search/taskTables")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> queryFromTaskList(){
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String typeCode = request.getParameter("typeCode") ;
		String pageSize = request.getParameter("pageSize") ; //分页参数
		String pageCount = request.getParameter("pageCount") ; //分页参数
		if( StringUtils.isEmptyOrWhitespaceOnly(typeCode) ){
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "查询失败") ;
			return retMap ;
		}
		params.put("typeCode", typeCode) ;
		params.put("pageSize", pageSize) ;
		params.put("pageCount", pageCount) ;
		params.put("userId", Session.getCurrUser().getId()) ; 
		service.searchTaskTables(retMap , params) ;
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
			service.updateTaskState(retMap, params);
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
		params.put("taskId", taskId) ;
		params.put("userId", Session.getCurrUser().getId()) ; 
		String rootPath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/"));
		rootPath = service.getFileRootPath(params , rootPath) ;
		params.put("rootPath", rootPath) ;
		service.searchTaskInfo(retMap , params) ;
		
		return retMap ;
	}
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
		String lastUpdateDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		String fromTaskId = request.getParameter("fromTaskId") ;
		String isStandard = request.getParameter("isStandard") ;
		String memo = request.getParameter("memo") ;
		if(StringUtils.isEmptyOrWhitespaceOnly(taskName)
				|| StringUtils.isEmptyOrWhitespaceOnly(startTime)
					|| StringUtils.isEmptyOrWhitespaceOnly(endTime) 
						|| StringUtils.isEmptyOrWhitespaceOnly(typeCode)){
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "请检查表单") ;
			return retMap ;
		}
		params.put("userId", Session.getCurrUser().getId()) ; 
		params.put("userName", Session.getCurrUser().getUsername()) ; 
		if(!StringUtils.isEmptyOrWhitespaceOnly(fromTaskId)){ //从上一阶段任务过来
			try {
				params.put("taskId", fromTaskId) ;
				String rootPath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/"));
				String fromPath = service.getFileRootPath(params , rootPath) ;
				String toPath = rootPath ;
				if( StringUtils.isEmptyOrWhitespaceOnly(taskId) || "null".equals(taskId)){//新增
					taskId = UUID.randomUUID().toString().replace("-", "") ;
					params.put("taskId", taskId) ;
					toPath += ( "TaskFile" + File.separator + "NLP" + File.separator + typeCode+File.separator
									+ lastUpdateDate.substring(0, lastUpdateDate.indexOf(" "))+File.separator+taskId+File.separator );
				}else{//修改
					params.put("taskId", taskId) ;
					toPath = service.getFileRootPath(params , rootPath) ;
				}
				//清空
				FileUtils.deleteAnyone(toPath) ;
				//文件复制
				com.sunsheen.jfids.commons.io.FileUtils.copyDirectory(new File(fromPath+File.separator+"source"), new File(toPath+File.separator+"source"));
				com.sunsheen.jfids.commons.io.FileUtils.copyDirectory(new File(fromPath+File.separator+"target"), new File(toPath+File.separator+"target"));
				File file = new File(fromPath+File.separator+"schema") ;
				//判断源目标是否存在
				if(!file.exists()){//不存在则创建目录信息
						new File(toPath+File.separator+"schema").mkdirs() ;
				}else{//存在，则copy
						com.sunsheen.jfids.commons.io.FileUtils.copyDirectory(new File(fromPath+File.separator+"schema"), new File(toPath+File.separator+"schema"));	
				}
				retMap.putAll(RetInfo.RETSUCCESS) ;
			} catch (IOException e) {
				e.printStackTrace();
				retMap.putAll(RetInfo.RETFAIL) ;
			}
		}else{//在线传文件信息
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
			if( StringUtils.isEmptyOrWhitespaceOnly(taskId) && (items==null || items.size() <= 0) ){
				retMap.putAll(RetInfo.RETFAIL) ;
				retMap.put("retmsg", "请上传文件") ;
				return retMap ;
			}else{
				String filePath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")) ;
				if( StringUtils.isEmptyOrWhitespaceOnly(taskId) || "null".equals(taskId)){//新增
					taskId = UUID.randomUUID().toString().replace("-", "") ;
					params.put("taskId", taskId) ;
					filePath += ( "TaskFile" + File.separator + "NLP" + File.separator + typeCode+File.separator
									+ lastUpdateDate.substring(0, lastUpdateDate.indexOf(" "))+File.separator+taskId+File.separator );
				}else{//修改
					params.put("taskId", taskId) ;
					filePath = service.getFileRootPath(params , filePath) ;
				}
				params.put("filePath", filePath) ; //文件上传的路径信息
				if( ( !StringUtils.isEmptyOrWhitespaceOnly(taskId) && (items==null || items.size() <= 0)) ){
					//先进行文件上传，再进行任务修改或新增,如果任务ID不为空并且，item个数为0,则为编辑
					retMap.putAll(RetInfo.RETSUCCESS) ;
				}else{ //否则进行文件更新
					if("yes".equalsIgnoreCase(isStandard)){
						params.put("sourcePath", filePath) ;
					}else{
						params.put("sourcePath", filePath+"source"+File.separator) ;
					}
					FileUtils.deleteAnyone(filePath) ;
					FileUploadUtils.uploadFilesService(items, retMap, params) ;//测试是否能操作成功
//					if("01".equals(retMap.get("retcode"))){ //如果返回状态码是01（操作成功），则删除文件信息，再次上传解压
//						
//						FileUploadUtils.uploadFilesService(items, retMap, params) ;
//					}
				}
			}
		}
		if("01".equals(retMap.get("retcode"))){
			params.put("taskName", taskName) ;
			params.put("startTime", startTime) ;
			params.put("endTime", endTime) ;
			params.put("lastUpdateDate", lastUpdateDate) ;
			params.put("typeCode", typeCode) ;
			params.put("memo", memo) ;
			params.put("fromTaskId", fromTaskId) ;
			service.saveOrUpdateTaskInfo(retMap, params);
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
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId") ;
		if( StringUtils.isEmptyOrWhitespaceOnly(taskId)){
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg", "下载失败！") ;
			return retMap ;
		}
		params.put("taskId", taskId) ;
		params.put("userId", Session.getCurrUser().getId()) ; 
		String rootPath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/"));
		rootPath = service.getFileRootPath(params , rootPath) ;
		String taskName = params.get("taskName")+"" ;
		String typeCode = params.get("typeCode")+"" ;
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
			String fileName = typeCode+"_"+taskName+"_"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis())) ;
			zip(new File(rootPath),fileName,response) ;
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
						"Content-Disposition","attachment;fileName="+ new String( fileName.getBytes("gb2312"), "ISO8859-1" ) + ".zip");
			//创建zip输出流
			out = new ZipOutputStream(response.getOutputStream());
			//创建缓冲输出流
			bos = new BufferedOutputStream(out);
			//调用压缩函数
			ZipUtils.compress(out, bos, file , "" , "source");
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
	
}
