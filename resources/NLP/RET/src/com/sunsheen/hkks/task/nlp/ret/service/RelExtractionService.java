package com.sunsheen.hkks.task.nlp.ret.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.common.util.PageParamsUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.nlp.common.entity.ConnCategoriesEntity;
import com.sunsheen.hkks.nlp.common.entity.LabelCategoriesEntity;
import com.sunsheen.hkks.nlp.common.entity.NLPLabelsEntity;
import com.sunsheen.hkks.nlp.common.entity.NLPSentenceEntity;
import com.sunsheen.hkks.nlp.common.utils.AnnotionEntityUtils;

public class RelExtractionService {

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
	@SuppressWarnings("unchecked")
	public void searchMarkFile( Map<String , Object> retMap , Map<String , Object> params){
		//目标标注信息
		String targetPath = params.get("rootPath") + "target" ;
		//标签信息
		String schemaPath = params.get("rootPath") + "schema" ;
		try{
			//实体标签
			List<LabelCategoriesEntity> labelCategories = new ArrayList<LabelCategoriesEntity>() ;
			//关系标签
			List<ConnCategoriesEntity> connectionCategories = new ArrayList<ConnCategoriesEntity>() ; 
			//文件获取
			labelCategories = JSONObject.parseObject(FileUtils.readJsonFile(schemaPath+File.separator + "labelCategories.json"),labelCategories.getClass()) ;
			connectionCategories = JSONObject.parseObject(FileUtils.readJsonFile(schemaPath+File.separator + "connectionCategories.json"),connectionCategories.getClass()) ;
			AnnotionEntityUtils.judgeandAddInitLabelCategories(labelCategories);
			//标注文件获取
			String path =  targetPath + File.separator + params.get("paths") ;
			String targetNewFile = path.replace(File.separator+"source"+File.separator, File.separator+"target"+File.separator) ;
			String fileFullName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1,  targetNewFile.length()) ;
			String fileName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1, targetNewFile.lastIndexOf(".")) ;
			String type = targetNewFile.substring( targetNewFile.lastIndexOf(".")+1 , targetNewFile.length()) ;
			String fileNewFullName = fileName+"-"+type+".json" ;
			targetNewFile = targetNewFile.replace( fileFullName , fileNewFullName ) ;
			NLPLabelsEntity nlpLabelsEntity = JSONObject.parseObject( FileUtils.readJsonFile( targetNewFile ),NLPLabelsEntity.class ) ;

			List<NLPSentenceEntity> nlpSentenceEntities = nlpLabelsEntity.getSentences();
			Integer totalCount = nlpSentenceEntities.size() ;

			if( nlpSentenceEntities.size() > 0  ){
				PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), (String)params.get("pageSize"), totalCount+"");
				//分页截取
				Integer startIndex = Integer.parseInt(params.get("pageIndex")+"");
				Integer endIndex = (Integer.parseInt(params.get("pageIndex")+"")+Integer.parseInt(params.get("pageSize")+"") ) > nlpSentenceEntities.size() ? nlpSentenceEntities.size() :
					 Integer.parseInt(params.get("pageIndex")+"")+Integer.parseInt(params.get("pageSize")+"");
				nlpSentenceEntities = nlpSentenceEntities.subList(startIndex , endIndex) ;
				for(NLPSentenceEntity sentence : nlpSentenceEntities){
					sentence.getAnnotation().setLabelCategories(labelCategories);
					sentence.getAnnotation().setConnectionCategories(connectionCategories);
					//初始化json
					AnnotionEntityUtils.judgeandAddInitLabelCategories(sentence.getAnnotation());
				}
				retMap.putAll(RetInfo.RETSUCCESS) ;
			}else{
				retMap.putAll(RetInfo.RETSUCCESS) ;
				retMap.put("retmsg","查询成功，暂无数据") ;
			}
			retMap.put("data", nlpSentenceEntities) ;
			retMap.put("totalCount", totalCount) ;
		}catch(Exception e){
			e.printStackTrace();
			retMap.putAll(RetInfo.RETFAIL) ;
		}
	}
}
