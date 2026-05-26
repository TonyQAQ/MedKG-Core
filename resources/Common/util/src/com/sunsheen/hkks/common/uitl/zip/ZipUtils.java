package com.sunsheen.hkks.common.uitl.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

//import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
//import org.apache.commons.compress.archivers.zip.ZipFile;

public class ZipUtils {
	@SuppressWarnings("serial")
	private final static Set<String> markFileType = new HashSet<String>() {
		{
			add("csv");
			add("txt");
			add("json");
			add("doc");
			add("docx");
		}
	};
	
	
	@SuppressWarnings("serial")
	private final static Set<String> markPictureType = new HashSet<String>() {
		{
			add("jpg");
			add("jpeg");
			add("png");
		}
	};
	/**
	 * 
	* @Title: unZipFiles
	* @Description:
	* @author: FengTao
	* @date 2020年9月8日 下午2:19:57
	* @param zipFile 待解压的zip文件
	* @param descDir 指定目录
	* @throws IOException void
	* @version
	 */
	public static boolean unZipFiles(String zipDir, String descDir) throws IOException {
		boolean flag = true ;
        try {
//        	String fileCode = EncodingDetect.getJavaEncode( zipDir ); //获取编码格式
        	File zipFile = new File( zipDir ) ;
        	//开始构建
            ZipFile zip = new ZipFile(zipFile, Charset.forName( "GBK" ));//解决中文文件夹乱码,构件zip输入流
            ZipEntry entry = null;
            File file = null;
            for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
            	entry = (ZipEntry) entries.nextElement();
                if (!entry.isDirectory()) {
                     InputStream in = zip.getInputStream(entry);
                     // 判断路径是否存在,不存在则创建文件路径
                     file = new File(descDir , entry.getName());
                     if (!file.exists()) {
                         new File(file.getParent()).mkdirs();//创建此文件的上级目录
                     }
                	// 输出文件路径信息
                    FileOutputStream out = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                    in.close();
                    out.close();
                }
            }
            zip.close();
            System.out.println("******************解压完毕********************");
        } catch (Exception e) {
            e.printStackTrace();
            flag = false ;
            System.out.println("******************解压文件出错********************");
        }
        return flag;
//		boolean flag = true ;
//		try {
//			ZipFile zipFile = new ZipFile(new File(zipDir));
//	        byte[] buffer = new byte[4096];
//	    	ZipArchiveEntry  entry;
//	        Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();// 获取全部文件的迭代器
//	        InputStream inputStream;
//	        while (entries.hasMoreElements()) {
//	            entry = entries.nextElement();
//	            if (entry.isDirectory()) {
//	                continue;
//	            }
//	            File outputFile = new File(descDir + entry.getName());
//	            if (!outputFile.getParentFile().exists()) {
//	                outputFile.getParentFile().mkdirs();
//	            }
//	            inputStream = zipFile.getInputStream(entry);
//	            FileOutputStream fos = new FileOutputStream(outputFile);
//	            try {
//	            	while (inputStream.read(buffer) > 0) {
//		                fos.write(buffer);
//		            }
//				} catch (Exception e) {
//					// TODO: handle exception
//					e.printStackTrace();
//				}finally{
//					if( fos != null){
//						fos.close();
//					}
//				}           
//	        }
//	        zipFile.close();
//		} catch (Exception e) {
//			// TODO: handle exception
//			 flag = false ;
//			e.printStackTrace();			
//		}   
//		return flag;
    }
	
	public static boolean unZipPictureIsLegal(String zipDir) throws IOException {
		boolean flag = true ;
        try {
//        	String fileCode = EncodingDetect.getJavaEncode( zipDir ); //获取编码格式
        	File zipFile = new File( zipDir ) ;
        	//开始构建
            ZipFile zip = new ZipFile(zipFile, Charset.forName( "GBK" ));//解决中文文件夹乱码,构件zip输入流
            ZipEntry entry = null;
            for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
            	entry = (ZipEntry) entries.nextElement();
                if (!entry.isDirectory()) {
                     // 判断路径是否存在,不存在则创建文件路径
                     String fileType = entry.getName().substring(entry.getName().lastIndexOf(".")+1) ;
                     if( !markPictureType.contains(fileType) ){
                    	 flag = false ;
                     }
                }
            }
            zip.close();
        } catch (Exception e) {
            e.printStackTrace();
            flag = false ;
        }
        return flag;
    }
	
	
	/**
	 * 
	* @Title: unZipFileIsLegal
	* @Description: 查看zip文件里面内容是否规范
	* @author: FengTao
	* @date 2020年9月8日 下午4:02:25
	* @param zipDir
	* @param descDir
	* @throws IOException void
	* @version
	 */
	public static boolean unZipFileIsLegal(String zipDir) throws IOException {
		boolean flag = true ;
        try {
//        	String fileCode = EncodingDetect.getJavaEncode( zipDir ); //获取编码格式
        	File zipFile = new File( zipDir ) ;
        	//开始构建
            ZipFile zip = new ZipFile(zipFile, Charset.forName( "GBK" ));//解决中文文件夹乱码,构件zip输入流
            ZipEntry entry = null;
            for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
            	entry = (ZipEntry) entries.nextElement();
                if (!entry.isDirectory()) {
                     // 判断路径是否存在,不存在则创建文件路径
                     String fileType = entry.getName().substring(entry.getName().lastIndexOf(".")+1) ;
                     if( !markFileType.contains(fileType) ){
                    	 flag = false ;
                     }
                }
            }
            zip.close();
        } catch (Exception e) {
            e.printStackTrace();
            flag = false ;
        }
        return flag;
//		boolean flag = true ;
//		try {
//			ZipFile zipFile = new ZipFile(new File(zipDir));
//			ZipArchiveEntry  entry;
//	        Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();// 获取全部文件的迭代器
//	        while (entries.hasMoreElements()) {
//	            entry = entries.nextElement();
//	            if (!entry.isDirectory()) {
//	            	String fileType = entry.getName().substring(entry.getName().lastIndexOf(".")+1) ;
//                  if( !markFileType.contains(fileType) ){
//                 	 flag = false ;
//                 	 break;
//                  }
//	            }
//	        }
//	        zipFile.close();
//		} catch (Exception e) {
//			// TODO: handle exception
//			 flag = false ;
//			e.printStackTrace();			
//		}   
//		return flag;
    }
	
	/**
	 * 
	* @Title: compress
	* @Description: 递归压缩
	* @author: FengTao
	* @date 2020年9月10日 下午4:26:15
	* @param out
	* @param bos
	* @param sourceFile void
	* @version
	 */
	public static void compress(ZipOutputStream out, BufferedOutputStream bos, File sourceFile , String dir , String filterPath){
		FileInputStream fos = null;
		BufferedInputStream bis = null;
		try {
			//如果路径为目录（文件夹）
			if (sourceFile.isDirectory() && !(sourceFile.getName()+"").equals(filterPath) ) {
				//取出文件夹中的文件（或子文件夹）
				File[] flist = sourceFile.listFiles();
				if (flist.length == 0) {//如果文件夹为空，则只需在目的地zip文件中写入一个目录进入点
					out.putNextEntry(new ZipEntry(  dir ));
				} else {//如果文件夹不为空，则递归调用compress，文件夹中的每一个文件（或文件夹）进行压缩
					for (int i = 0; i < flist.length; i++) {
						if(dir.equals("")){
							compress(out, bos, flist[i] , flist[i].getName() , filterPath);
						}else{
							compress(out, bos, flist[i] , dir+File.separator + flist[i].getName() , filterPath);
						}
					}
				}
			} else if( !sourceFile.isDirectory() && !(sourceFile.getName()+"").endsWith(".zip") ){//如果不是目录（文件夹），即为文件，则先写入目录进入点，之后将文件写入zip文件中
				out.putNextEntry(new ZipEntry(  dir ));
				fos = new FileInputStream(sourceFile);
				bis = new BufferedInputStream(fos) ;
				int tag;
				//将源文件写入到zip文件中
				while ((tag = bis.read()) != -1) {
					out.write(tag);
				}
				bis.close();
				fos.close();
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    public static void main(String[] args) {
        try {
        	unZipFileIsLegal("D:/complay-file/相关文件/文件解压.zip");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
