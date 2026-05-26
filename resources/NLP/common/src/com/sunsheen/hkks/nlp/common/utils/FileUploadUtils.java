package com.sunsheen.hkks.nlp.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.sunsheen.hkks.common.uitl.zip.ZipUtils;
import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.jfids.commons.fileupload.FileItem;

public class FileUploadUtils {
	@SuppressWarnings("serial")
	private final static Set<String> uploadFileType = new HashSet<String>() {
		{
			add("csv");
			add("txt");
			add("json");
			add("doc");
			add("docx");
		}
	};
	
	/**
	 * 文件上传
	* @Title: uploadFilesService
	* @Description:
	* @author: FengTao
	* @date 2020年9月8日 下午3:48:35
	* @param items 文件列表
	* @param params
	* @param retMap
	* @return Map<String,Object>
	* @version
	 */
	public static synchronized  Map<String , Object> uploadFilesService(List<FileItem> items , Map<String,Object> retMap , Map<String,Object> params){
		String filePath = params.get("filePath")+"" ;  //task目录信息，在失败的时候，用于删除task目录信息
		String sourcePath = params.get("sourcePath")+"" ; //上传文件解压存放地址
		try {
			if ( items != null ) { //一次只能上传一个文件
				Iterator<FileItem> iter = items.iterator();
				String fileName = "";
				String fullName = "";
				String type = "" ;
				int zipCount = 0 ; 
				Long lengthLong = 0L ;
				List<String> list = new ArrayList<String>() ;
				//查看上传的文件 zip 数量是多少个
				while (iter.hasNext()) {
					FileItem item = iter.next();
					if (!item.isFormField() && item.getSize() > 0) {
						fullName = item.getName();
						fileName = fullName.substring(fullName.lastIndexOf(File.separator) + 1);
						type = fileName.substring(fileName.lastIndexOf(".") + 1);
					}
					list.add(type) ;
					lengthLong += item.getSize() ;
				}
				//上传最大量是10M
				String size = FileUtils.FormetFileSize(lengthLong) ;
				if( (size.contains("MB") || size.contains("GB") ) && Double.parseDouble( size.substring(0, size.length()-2) ) >50 ){
					System.out.println(size);
					retMap.putAll(RetInfo.RETFAIL);
					retMap.put("retmsg", "使用版仅支持最大【5M】文档上传，若要支持更大文件上传请联系管理员！") ;
					return retMap;
				}
				
				for(String str : list){
					if("zip".equals(str)){
						zipCount ++ ;
						if( zipCount > 1 ){
							retMap.putAll(RetInfo.RETFAIL);
							retMap.put("retmsg", "单次只能上传一个zip格式的文件") ;
							return  retMap;
						}
					}
				}
				Iterator<FileItem> iterator = items.iterator();
				//遍历上传文件
				while (iterator.hasNext()) {
					FileItem item = iterator.next();
					if (!item.isFormField() && item.getSize() > 0) {
						fullName = item.getName();
						fileName = fullName.substring(fullName.lastIndexOf(File.separator) + 1);
						type = fileName.substring(fileName.lastIndexOf(".") + 1);
						InputStream inputStream = item.getInputStream();
						byte[] buffer = null; //读取文件的字节流
						//请初始化缓冲区大小
						ByteArrayOutputStream baos = new ByteArrayOutputStream(inputStream.available());
						byte[] bytes = new byte[1024];
						int temp;
						while ((temp = inputStream.read(bytes)) != -1) {
							baos.write(bytes, 0, temp);
						}
						buffer = baos.toByteArray();
						inputStream.close();
						baos.close();
						inputStream.close();
						if( "zip".equals(type) ){ //是zip
							String dir = filePath+fileName ; //上传的文件
							boolean flag = FileUtils.createFileFromBytes( buffer, dir ) ; //创建上传的文件信息 
							if(flag){ //文件创建成功
								//判断文件信息是否合法
								boolean isLegal = ZipUtils.unZipFileIsLegal(dir) ; // 判断zip是否合法
								if(isLegal){ //判断文件是否合法，合法则解压
									boolean zipState = ZipUtils.unZipFiles( dir, sourcePath ) ;// 文件解压
									if(zipState){
										retMap.putAll(RetInfo.RETSUCCESS);
									}else{
										retMap.putAll(RetInfo.RETFAIL);
										retMap.put("retmsg", "压缩文件解压失败，请重试！") ;
									}
								}else{
									retMap.putAll(RetInfo.RETFAIL);
									retMap.put("retmsg", "上传的zip文件含有非txt、csv、docx、doc格式的文件！") ;
								}
							}else{
								retMap.putAll(RetInfo.RETFAIL);
								retMap.put("retmsg", "压缩文件上传失败") ;
							}
						}else if( uploadFileType.contains(type.toLowerCase()) ){ //是csv、txt文件，直接放到source目录下
							String dir = sourcePath+fileName ; //文件上传的
							boolean flag = FileUtils.createFileFromBytes( buffer, dir ) ; //创建上传的文件信息 
							if(flag){ //文件创建成功
								//判断文件信息是否合法
								retMap.putAll(RetInfo.RETSUCCESS);
							}else{
								retMap.putAll(RetInfo.RETFAIL);
								retMap.put("retmsg", "文件上传失败！") ;
							}
						}else {
							retMap.putAll(RetInfo.RETFAIL);
							retMap.put("retmsg", "目前仅支持csv、txt、docx、json、doc、zip文件格式上传！") ;
						}
					}
				}
			}else{
				retMap.putAll(RetInfo.RETFAIL);
				retMap.put("retmsg", "请上传文件！") ;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL);
		}
		return retMap;	
	}
}
