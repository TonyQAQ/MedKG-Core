package com.sunsheen.hkks.task.nlp.sst.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alibaba.fastjson.JSONObject;
import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.nlp.common.entity.NLPLabelsEntity;
import com.sunsheen.hkks.nlp.common.entity.NLPSentenceEntity;
import com.sunsheen.hkks.nlp.common.utils.AnnotionEntityUtils;
import com.sunsheen.hkks.nlp.common.utils.SentenceReadUtils;

public class SentenceSegmentService {
	
	private static final String COUNT_STR = "--AA--" ;
	
	/**
	 * 
	* @Title: readyMarkFile
	* @Description: 分句准备工作
	* @author: FengTao
	* @date 2020年9月9日 下午5:38:06
	* @param retMap
	* @param params void
	* @version
	 */
	public void readySentencesFile( Map<String , Object> retMap , Map<String , Object> params ) {
		//源标注信息
		String sourcePath = params.get("rootPath") + "source" ;
		try {
			//遍历目标文件列表获取所有文件（不获取文件夹），为每个文件重新构建目标文件
			List<String> files = FileUtils.getAllFile(sourcePath, true) ;
			for(String soruceFile : files){
				StringBuffer content =  new StringBuffer() ;//每个文件的内容
				//文件信息的实体
				NLPLabelsEntity nlpLabelsEntity = new NLPLabelsEntity() ; 
				//句子标注信息
//				List<NLPSentenceEntity> sentencesEntity = new ArrayList<NLPSentenceEntity>() ;
				String targetNewFile = soruceFile ;
				//构件目标文件的文件名
				String fileFullName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1,  targetNewFile.length()) ;
				String fileName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1, targetNewFile.lastIndexOf(".")) ;
				String type = targetNewFile.substring( targetNewFile.lastIndexOf(".")+1 , targetNewFile.length()).toLowerCase();
				String fileNewFullName = fileName+"-"+type+".json" ;
				//生成最终文件名称信息
				targetNewFile = targetNewFile.replace( fileFullName , fileNewFullName )
										.replace(File.separator+"source"+File.separator, File.separator+"target"+File.separator) ;
//				String[] sentences = 
				SentenceReadUtils.readFileSentence(type,soruceFile,content,false) ;
				
//				sentencesEntity = createEntityObject(sentences) ;
				nlpLabelsEntity.setId(UUID.randomUUID().toString());
				nlpLabelsEntity.setContent(content.toString());
				nlpLabelsEntity.setSourceName(fileFullName);
//				nlpLabelsEntity.setSentences(sentencesEntity);
				nlpLabelsEntity.setSentences(new ArrayList<>());
				//构件标注文件信息
				FileUtils.createJsonFile(nlpLabelsEntity, targetNewFile);//创建初始化标签信息
			}
			retMap.putAll(RetInfo.RETSUCCESS) ;
			retMap.put("retmsg","准备成功！") ;
		} catch (Exception e) {
			retMap.putAll(RetInfo.RETFAIL) ;
			e.printStackTrace();
		}
	}
	/**
	 * 
	* @Title: updateSentences
	* @Description: 更新句子信息
	* @author: FengTao
	* @date 2020年11月25日 下午5:56:22
	* @param retMap
	* @param quickLabelList
	* @param params void
	* @version
	 */
	public void saveSentences( Map<String , Object> retMap , Map<String , Object> params){
		//标签信息
		String targetPath = params.get("rootPath") + "target" ;
		try{
			
			String path =  targetPath + File.separator + params.get("paths") ;
			String targetNewFile = path.replace(File.separator+"source"+File.separator, File.separator+"target"+File.separator) ;
			String fileFullName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1,  targetNewFile.length()) ;
			String fileName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1, targetNewFile.lastIndexOf(".")) ;
			String type = targetNewFile.substring( targetNewFile.lastIndexOf(".")+1 , targetNewFile.length()) ;
			String fileNewFullName = fileName+"-"+type+".json" ;
			targetNewFile = targetNewFile.replace( fileFullName , fileNewFullName ) ;
			NLPLabelsEntity nlpLabelsEntity = JSONObject.parseObject( FileUtils.readJsonFile( targetNewFile ),NLPLabelsEntity.class ) ;
			//句子标注信息
			List<NLPSentenceEntity> sentencesEntity = new ArrayList<NLPSentenceEntity>() ;
			if(!StringUtils.isEmptyOrWhitespaceOnly(params.get("indexs")+"")){
				String[] indexs = params.get("indexs").toString().split(",") ;
				String[] sentences = new String[indexs.length] ;
				String content = nlpLabelsEntity.getContent() ;
				for(int i = 0 ; i < indexs.length ; i++){
					sentences[i] = content.substring( Integer.parseInt(indexs[i].split(COUNT_STR)[0]),
													Integer.parseInt(indexs[i].split(COUNT_STR)[1]) ) ;
				}
				sentencesEntity = AnnotionEntityUtils.createEntityObject(sentences) ; 
			}else{
				sentencesEntity = AnnotionEntityUtils.createEntityObject(new String[0]) ; 
			}
			
			nlpLabelsEntity.setSentences(sentencesEntity);
			//更新标注文件信息
			FileUtils.createJsonFile(nlpLabelsEntity, targetNewFile) ;
			retMap.putAll(RetInfo.RETSUCCESS) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
	
	/**
	 * 
	* @Title: searchMarkFile
	* @Description: 读取文件信息
	* @author: FengTao
	* @date 2020年9月10日 上午10:01:58
	* @param retMap
	* @param params void
	* @version
	 */
	public void searchSentencesFile( Map<String , Object> retMap , Map<String , Object> params){
		//目标标注信息
		String targetPath = params.get("rootPath") + "target" + File.separator;
		Map<String , Object> data = new HashMap<String ,Object>() ;
		try{
			//文件获取
			String targetNewFile =  targetPath + params.get("paths");
			//文件原名称
			String fileFullName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1,  targetNewFile.length()) ;
			String fileName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1, targetNewFile.lastIndexOf(".")) ;
			String type = targetNewFile.substring( targetNewFile.lastIndexOf(".")+1 , targetNewFile.length()) ;
			String fileNewFullName = fileName+"-"+type+".json" ;
			targetNewFile = targetNewFile.replace( fileFullName , fileNewFullName ) ;
			
			NLPLabelsEntity nlpLabelsEntity = JSONObject.parseObject( FileUtils.readJsonFile( targetNewFile ),NLPLabelsEntity.class ) ;
			List<NLPSentenceEntity> nlpSentences = new ArrayList<NLPSentenceEntity>() ; 
			nlpSentences.addAll(nlpLabelsEntity.getSentences()) ;
//			Integer totalCount = nlpSentences.size() ;
			//分页截取
//			PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), (String)params.get("pageSize"), totalCount+"");
//			
//			Integer startIndex = Integer.parseInt(params.get("pageIndex")+"");
//			Integer endIndex = (Integer.parseInt(params.get("pageIndex")+"")+Integer.parseInt(params.get("pageSize")+"") ) > nlpSentences.size() ? nlpSentences.size() :
//				 Integer.parseInt(params.get("pageIndex")+"")+Integer.parseInt(params.get("pageSize")+"");
//			nlpSentences = nlpSentences.subList(startIndex , endIndex) ;
			StringBuffer sentences = new StringBuffer() ;
			List<Map<String , Object>> stentenceAndIndex = new ArrayList<>() ;
			Integer endIndex = 0 ;
			for(NLPSentenceEntity entity : nlpSentences){
				Map<String , Object> temp = new HashMap<String ,Object>() ;
				temp.put("sentence", entity.getAnnotation().getContent()) ;
				temp.put("startIndex", entity.getStartIndex()) ;
				temp.put("endIndex", entity.getEndIndex()) ;
				stentenceAndIndex.add(temp) ;
				sentences.append(entity.getAnnotation().getContent()+COUNT_STR) ;
				endIndex = Integer.parseInt(entity.getEndIndex()) ;
			}
			retMap.putAll(RetInfo.RETSUCCESS) ;
			//分句信息处理
			String remainSentence = nlpLabelsEntity.getContent().substring(endIndex, nlpLabelsEntity.getContent().length()) ;
			remainSentence = remainSentence.substring(0, remainSentence.length() > 4000 ? 4000 : remainSentence.length()) ;
			
			data.put("remainSentence", remainSentence) ;
			data.put("sentences", stentenceAndIndex) ;
			retMap.put("data", data) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
	
}
