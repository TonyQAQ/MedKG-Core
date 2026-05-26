package com.sunsheen.hkks.task.pictureMark.picture;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.SystemOutLogger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import sun.misc.BASE64Encoder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.TypeReference;
import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.nlp.common.service.MarkCommonService;
import com.sunsheen.hkks.nlp.common.service.NLPCommonService;
import com.sunsheen.hkks.task.pictureMark.entity.PictureLabelsConnection;
import com.sunsheen.hkks.task.unstructure.common.utils.FileUploadUtils;
import com.sunsheen.hkks.task.unstructure.ql.service.QuickLabelsService;
import com.sunsheen.jfids.commons.fileupload.FileItem;
import com.sunsheen.jfids.commons.fileupload.FileItemFactory;
import com.sunsheen.jfids.commons.fileupload.FileUploadException;
import com.sunsheen.jfids.commons.fileupload.disk.DiskFileItemFactory;
import com.sunsheen.jfids.commons.fileupload.servlet.ServletFileUpload;
import com.sunsheen.jfids.pm.web.rest.api.BaseAPI;
import com.sunsheen.jfids.system.security.login.Session;



@Path("/task/picture")
public class PictureController extends BaseAPI{

	private QuickLabelsService quickLabelsService = new QuickLabelsService() ;
	private NLPCommonService commonService = new NLPCommonService() ;
	private MarkCommonService markService = new MarkCommonService();
	
	
	@POST
	@Path("/save")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> savePictureTask() throws UnsupportedEncodingException{
		request.setCharacterEncoding("utf-8");
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
		String arrayString = ((FileItem)items.get(items.size()-1)).getString("utf-8");
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		ArrayList<File> fileList = new ArrayList<>();
	
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId");
		String taskName = request.getParameter("taskName") ;
		String startTime = request.getParameter("startTime") ;
		String endTime = request.getParameter("endTime") ;
		String typeCode = request.getParameter("typeCode") ;
		String memo = request.getParameter("memo") ;	
		String filePath = null;
		Map<String,ArrayList<String>>	labelsMap = new HashMap<>();
		
	try{
		if(StringUtils.isEmptyOrWhitespaceOnly(taskName)
				|| StringUtils.isEmptyOrWhitespaceOnly(startTime)
					|| StringUtils.isEmptyOrWhitespaceOnly(endTime)
						|| ( StringUtils.isEmptyOrWhitespaceOnly(taskId) && (items==null || items.size() <= 0)) ){
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("data", "请检查表单数据格式等问题") ;
			return retMap ;
		}else{
			if( StringUtils.isEmptyOrWhitespaceOnly(taskId) || "null".equals(taskId)){
				taskId = UUID.randomUUID().toString().replace("-", "") ;
			}
			filePath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
					+ "TaskFile" + FileUtils.FILE_SEPARATOR + "QuickLabels" + FileUtils.FILE_SEPARATOR + taskId + FileUtils.FILE_SEPARATOR);
			params.put("taskId", taskId);
			params.put("taskName", taskName);
			params.put("startTime", startTime);
			params.put("endTime", endTime);
			params.put("typeCode", typeCode);
			params.put("filePath", filePath); //文件上传的路径信息
			params.put("memo", memo);
			params.put("userId",Session.getCurrUser().getId()); 
			params.put("userName",Session.getCurrUser().getUsername()); 
			
			
			if( ( !StringUtils.isEmptyOrWhitespaceOnly(taskId) && (items==null || items.size() <= 0)) ){
				//先进行文件上传，再进行任务修改或新增,如果任务ID不为空并且，item个数为0,则为编辑
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}else{ //否则进行文件更新
				FileUploadUtils.uploadPictureFilesService(items, retMap, params) ;
			}
			
			
			String data = null;
			JSONArray array = null;
			try{
			String sourcePath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
					+ "TaskFile" + FileUtils.FILE_SEPARATOR + "QuickLabels" + FileUtils.FILE_SEPARATOR + taskId + FileUtils.FILE_SEPARATOR + "target"  + FileUtils.FILE_SEPARATOR +"pictureLablesInfo.json" ) ;
				File file = new File(sourcePath);
			    FileReader fileReader = new FileReader(file);
			    JSONReader jsonReader = new JSONReader(fileReader);
				 data   = jsonReader.readString();
				 array =  JSON.parseArray(data);
				jsonReader.close();
				System.out.println("data ================"+data);
			}catch(FileNotFoundException e){
				 array = new JSONArray();
			}
			
		
			for(int i = 0; i < array.size();i++){
				labelsMap.put(array.getJSONObject(i).getString("pictureName"),array.getJSONObject(i).getObject("labelId", new TypeReference<List<String>>(){}));
			}
			
			System.out.println("数据有：：：：：：：：：123456："+labelsMap);
			if("01".equals(retMap.get("retcode"))){ 
				quickLabelsService.saveOrUpdateTaskInfo(retMap, params);
			}	
			
		}
		
//        InputStream  inStream = maps.get("data").get(0).getBody(InputStream.class, null);
//		byte []b = IOUtils.toByteArray(inStream);
//		String arrayString = "[{\"text\":\"测试\", \"id\":\"3c4a28929a69421db3b919f8f180b75a\"}]";
		JSONArray array =	JSON.parseArray(arrayString);
//		if(maps.get("picture") != null){
				String sourcePath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
				       + "TaskFile" + FileUtils.FILE_SEPARATOR + "QuickLabels" + FileUtils.FILE_SEPARATOR + taskId + FileUtils.FILE_SEPARATOR) + "source";
				File file = new File(sourcePath);
				ArrayList<String> fileListName = new ArrayList<String>();
				FileUtils.getFileNames(file,fileListName);
				JSONArray jsonArray = new JSONArray();
				for(String fileName: fileListName){
					JSONObject object = new JSONObject();
					object.put("pictureName", fileName);
					if(labelsMap.containsKey(fileName)){
						object.put("labelId",labelsMap.get(fileName) );
					}else{
						object.put("labelId",new ArrayList<>());
					}
					
					jsonArray.add(object);
				}
				params.put("filePath", filePath);
				markService.savePictureLablesConnection(retMap,params,jsonArray);
//			}
		
				markService.savePictureSchemaJson(retMap, params, array);
		
		 }catch(Exception e){
			 e.printStackTrace();
		retMap.putAll(RetInfo.RETFAIL);
		
	}finally{
			for (File file : fileList){
			file.delete();
		}
	}
		return retMap;
	}
	
//	@POST
//	@Path("/save")
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Map<String,Object> savePictureTask11( MultipartFormDataInput input) throws IOException{
		request.setCharacterEncoding("utf-8");
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
		Map<String,List<InputPart>> maps = input.getFormDataMap();
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		ArrayList<File> fileList = new ArrayList<>();
		
		Iterator<FileItem> iter = items.iterator();
		String fName = "";
		String fullName = "";
		String type = "" ;
		int zipCount = 0 ; 
		List<String> list = new ArrayList<String>() ;
		//查看上传的文件 zip 数量是多少个
		while (iter.hasNext()) {
			FileItem item = iter.next();
			if (!item.isFormField() && item.getSize() > 0) {
				fullName = item.getName();
				 System.out.println("fullName ================"+fullName);
//				fileName = fullName.substring(fullName.lastIndexOf(File.separator) + 1);
//				type = fileName.substring(fileName.lastIndexOf(".") + 1);
			}
		}
	
		Map<String , Object> params = new HashMap<String ,Object>() ;
		String taskId = request.getParameter("taskId");
		String taskName = request.getParameter("taskName") ;
		String startTime = request.getParameter("startTime") ;
		String endTime = request.getParameter("endTime") ;
		String typeCode = request.getParameter("typeCode") ;
		String memo = request.getParameter("memo") ;	
		String filePath = null;
		Map<String,ArrayList<String>>	labelsMap = new HashMap<>();
		
	try{
		if(StringUtils.isEmptyOrWhitespaceOnly(taskName)
				|| StringUtils.isEmptyOrWhitespaceOnly(startTime)
					|| StringUtils.isEmptyOrWhitespaceOnly(endTime)
						|| ( StringUtils.isEmptyOrWhitespaceOnly(taskId) && (maps.get("picture")==null || maps.get("picture").size() <= 0)) ){
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("data", "请检查表单数据格式等问题") ;
			return retMap ;
		}else{
			if( StringUtils.isEmptyOrWhitespaceOnly(taskId) || "null".equals(taskId)){
				taskId = UUID.randomUUID().toString().replace("-", "") ;
			}
			filePath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
					+ "TaskFile" + FileUtils.FILE_SEPARATOR + "QuickLabels" + FileUtils.FILE_SEPARATOR + taskId + FileUtils.FILE_SEPARATOR);
			params.put("taskId", taskId);
			params.put("taskName", taskName);
			params.put("startTime", startTime);
			params.put("endTime", endTime);
			params.put("typeCode", typeCode);
			params.put("filePath", filePath); //文件上传的路径信息
			params.put("memo", memo);
			params.put("userId",Session.getCurrUser().getId()); 
			params.put("userName",Session.getCurrUser().getUsername()); 
			if(maps.get("picture") != null){
			  for(InputPart inputPart : maps.get("picture")){
				 inputPart.setMediaType(MediaType.TEXT_PLAIN_TYPE);
//				 System.out.println("inputPart.getBodyAsString() ================"+inputPart.getBodyAsString());
				 MultivaluedMap<String, String> multivaluedMap = inputPart.getHeaders();
				 System.out.println("multivaluedMap.getContent-Disposition ================"+multivaluedMap.get("Content-Disposition").get(0));
				 System.out.println("utf8======"+ new String(multivaluedMap.get("Content-Disposition").get(0).getBytes("ISO-8859-1"), "UTF-8"));
				 System.out.println("gbk======"+ new String(multivaluedMap.get("Content-Disposition").get(0).getBytes("ISO-8859-1"), "GBK"));
				 String disposition = URLDecoder.decode(multivaluedMap.get("Content-Disposition").get(0), "UTF-8");
				 System.out.println("disposition-utf8:"+disposition);
				 System.out.println("disposition-gbk:"+URLDecoder.decode(multivaluedMap.get("Content-Disposition").get(0), "GBK"));
				 String []dispositionString = disposition.split("; ");
					String pictureName = null;
//					if(dispositionString.length == 4){
//					 pictureName = dispositionString[3].split("''")[1];
//					 System.out.println("pictureName1======"+disposition);
//					}else{
//						String typeString = dispositionString[2];
//						pictureName = typeString.substring(10, typeString.length() - 1);
//						System.out.println("pictureName2======"+disposition);
//					}
					pictureName = getFileName(multivaluedMap);
					System.out.println("pictureName :" + pictureName);
					System.out.println("pictureName(decode) :" + URLDecoder.decode(pictureName, "ISO-8859-1"));
				 InputStream inputStream = inputPart.getBody(InputStream.class,null);
				 byte [] bytes = IOUtils.toByteArray(inputStream);
				 if(pictureName.contains(".zip") && maps.get("picture").size() >= 2){
					retMap.putAll(RetInfo.RETFAIL);
					retMap.put("data", "一次只能上传一个zip");
					return retMap;
				}
				File file = writeFile(bytes,filePath + pictureName);
				fileList.add(file);
			   }
			}
			
			if( ( !StringUtils.isEmptyOrWhitespaceOnly(taskId) && (maps.get("picture")==null || maps.get("picture").size() <= 0)) ){
				//先进行文件上传，再进行任务修改或新增,如果任务ID不为空并且，item个数为0,则为编辑
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}else{ //否则进行文件更新
				FileUploadUtils.uploadPictureFilesService(items, retMap, params) ;
			}
			
			
			String data = null;
			JSONArray array = null;
			try{
			String sourcePath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
					+ "TaskFile" + FileUtils.FILE_SEPARATOR + "QuickLabels" + FileUtils.FILE_SEPARATOR + taskId + FileUtils.FILE_SEPARATOR + "target"  + FileUtils.FILE_SEPARATOR +"pictureLablesInfo.json" ) ;
				File file = new File(sourcePath);
			    FileReader fileReader = new FileReader(file);
			    JSONReader jsonReader = new JSONReader(fileReader);
				 data   = jsonReader.readString();
				 array =  JSON.parseArray(data);
				jsonReader.close();
				System.out.println("data ================"+data);
			}catch(FileNotFoundException e){
				 array = new JSONArray();
			}
			
		
			for(int i = 0; i < array.size();i++){
				labelsMap.put(array.getJSONObject(i).getString("pictureName"),array.getJSONObject(i).getObject("labelId", new TypeReference<List<String>>(){}));
			}
			
			System.out.println("数据有：：：：：：：：：123456："+labelsMap);
			if("01".equals(retMap.get("retcode"))){ 
				quickLabelsService.saveOrUpdateTaskInfo(retMap, params);
			}	
			
		}
		
        InputStream  inStream = maps.get("data").get(0).getBody(InputStream.class, null);
		byte []b = IOUtils.toByteArray(inStream);
		JSONArray array =	JSON.parseArray(new String(b));
		if(maps.get("picture") != null){
				String sourcePath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
				       + "TaskFile" + FileUtils.FILE_SEPARATOR + "QuickLabels" + FileUtils.FILE_SEPARATOR + taskId + FileUtils.FILE_SEPARATOR) + "source";
				File file = new File(sourcePath);
				ArrayList<String> fileListName = new ArrayList<String>();
				FileUtils.getFileNames(file,fileListName);
				JSONArray jsonArray = new JSONArray();
				for(String fileName: fileListName){
					JSONObject object = new JSONObject();
					object.put("pictureName", fileName);
					if(labelsMap.containsKey(fileName)){
						object.put("labelId",labelsMap.get(fileName) );
					}else{
						object.put("labelId",new ArrayList<>());
					}
					
					jsonArray.add(object);
				}
				params.put("filePath", filePath);
				markService.savePictureLablesConnection(retMap,params,jsonArray);
			}
		
				markService.savePictureSchemaJson(retMap, params, array);
		
		 }catch(Exception e){
			 e.printStackTrace();
		retMap.putAll(RetInfo.RETFAIL);
		
	}finally{
		  for(File file: fileList){
			file.delete();
		}
	}
		return retMap;
	}
	
	private String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "unknown";
    }
	
	private File writeFile(byte[] content, String filename) throws IOException {

		File file = new File(filename);
		
		if(!file.exists() && !file.getParentFile().exists()){
			file.getParentFile().mkdir();
		}
		FileOutputStream fop = new FileOutputStream(file);
		
		fop.write(content);
		fop.flush();
		fop.close();
		return file;
	}

	
	@GET
	@Path("/getPictureList")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> getList(){
		Map<String , Object> retMap = new HashMap<String ,Object>() ;

		String data = null;
		JSONArray array = null;
		try{
			String taskId = request.getParameter("taskId");
				String sourcePath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
						+ "TaskFile" + FileUtils.FILE_SEPARATOR + "QuickLabels" + FileUtils.FILE_SEPARATOR + taskId + FileUtils.FILE_SEPARATOR + "target"  + FileUtils.FILE_SEPARATOR +"pictureLablesInfo.json" ) ;
		File file = new File(sourcePath);
		if(file.getParentFile().exists()){
		}
//	    FileReader fileReader = new FileReader(file);
	    InputStreamReader fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
	    JSONReader jsonReader = new JSONReader(fileReader);
		 data   = jsonReader.readString();
		 array =  JSON.parseArray(data);
		jsonReader.close();
		System.out.println(data);
		}catch(FileNotFoundException e){
			array = new JSONArray();
		}catch(Exception e){
		retMap.putAll(RetInfo.RETFAIL);
		return retMap;
		}
		
		retMap.put("connection", array);
		return retMap;
	}
	
	
	@GET
	@Path("/getPictureData")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> getPictureData(){
		Map<String , Object> retMap = new HashMap<String ,Object>();
		String base64PictureString = null;
		try{
			
		   String taskId =  request.getParameter("taskId");
		   String pictureName = request.getParameter("pictureName");
		   String rootPath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
				+ "TaskFile" + FileUtils.FILE_SEPARATOR + "QuickLabels" + FileUtils.FILE_SEPARATOR + taskId + FileUtils.FILE_SEPARATOR) + "source";
		
		File rootFile = new File(rootPath);
		
		File file = FileUtils.getFile(rootFile,pictureName);
		
		if(file == null){
			retMap.putAll(RetInfo.RETFAIL);
			retMap.put("data", "文件不存在");
			return retMap;
		}
		
	       InputStream inputStream = new FileInputStream(file);
	       byte[] data = null;
	       
	       data = new byte[inputStream.available()];
	       inputStream.read(data);
	       inputStream.close();
	       BASE64Encoder encoder = new BASE64Encoder();
	       base64PictureString  =   encoder.encode(data);
	       retMap.put("fileName", file.getName());
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL);
			e.printStackTrace();
		}
		
		retMap.putAll(RetInfo.RETSUCCESS);
		retMap.put("data",base64PictureString);
		
		return retMap;
	}
	
	@GET
	@Path("/getPictureLable")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> getPictureLable(){
		Map<String , Object> retMap = new HashMap<String ,Object>();
		String taskId = request.getParameter("taskId");
		String data = null;
		JSONArray array = null;
		String sourcePath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
				+ "TaskFile" + FileUtils.FILE_SEPARATOR + "QuickLabels" + FileUtils.FILE_SEPARATOR + taskId + FileUtils.FILE_SEPARATOR + "schema"  + FileUtils.FILE_SEPARATOR +"labelCategories.json" ) ;
		try{
			File file = new File(sourcePath);
		    FileReader fileReader = new FileReader(file);
		    JSONReader jsonReader = new JSONReader(fileReader);
			 data   = jsonReader.readString();
			 array =  JSON.parseArray(data);
			jsonReader.close();
		}catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL);
			return retMap;
		}
		
		retMap.putAll(RetInfo.RETSUCCESS);
		retMap.put("data", array);
		return retMap;
	}
	
	

	@POST
	@Path("/saveLablesConnection")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Map<String, Object> saveLablesConnection(List<PictureLabelsConnection> connections){
		Map<String,Object> params = new HashMap<String,Object>();
		Map<String , Object> retMap = new HashMap<String ,Object>() ;
		try{
		String taskId = request.getParameter("taskId");
		String filePath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
				+ "TaskFile" + FileUtils.FILE_SEPARATOR + "QuickLabels" + FileUtils.FILE_SEPARATOR + taskId + FileUtils.FILE_SEPARATOR);
		System.out.println("数据：：：：：：："+connections);
		params.put("filePath", filePath);
		markService.savePictureLablesConnection(retMap,params,JSON.parseArray(JSONArray.toJSONString(connections)));
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL);
		}
		return retMap;
	}
	
	@GET
	@Path("/getLablesConnection")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> getLablesConnection(){
		Map<String,Object> retMap = new HashMap<String,Object>();
		String data = null;
		JSONArray array = null;
		try{
			String taskId = request.getParameter("taskId");
		String sourcePath = FileUtils.getRealFilePath(request.getSession().getServletContext().getRealPath("/")
				+ "TaskFile" + FileUtils.FILE_SEPARATOR + "QuickLabels" + FileUtils.FILE_SEPARATOR + taskId + FileUtils.FILE_SEPARATOR + "target"  + FileUtils.FILE_SEPARATOR +"pictureLablesInfo.json" ) ;
			File file = new File(sourcePath);
		    FileReader fileReader = new FileReader(file);
		    JSONReader jsonReader = new JSONReader(fileReader);
			 data   = jsonReader.readString();
			 array =  JSON.parseArray(data);
			jsonReader.close();
			System.out.println(data);
		}catch(FileNotFoundException e){
			 array = new JSONArray();
		}
		catch(Exception e){
			retMap.putAll(RetInfo.RETFAIL);
			return retMap;
		}
		
		retMap.putAll(RetInfo.RETSUCCESS);
		retMap.put("data", array);
		return retMap;
	}
	
	
}
