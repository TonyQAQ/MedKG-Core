package com.sunsheen.hkks.task.nlp.cct.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.sunsheen.hkks.common.util.FileUtils;
import com.sunsheen.hkks.common.util.PageParamsUtils;
import com.sunsheen.hkks.common.util.RetInfo;
import com.sunsheen.hkks.nlp.common.entity.NLPLabelsEntity;
import com.sunsheen.hkks.nlp.common.entity.NLPSentenceEntity;

public class CategoriesClassifyService {

	/**
	 * 
	* @Title: searchClassifyFile
	* @Description: 读取文件信息
	* @author: FengTao
	* @date 2020年9月10日 上午10:01:58
	* @param retMap
	* @param params void
	* @version
	 */
	public void searchClassifyFile( Map<String , Object> retMap , Map<String , Object> params){
		//目标标注信息
		String targetPath = params.get("rootPath") + "target" ;
		try{
			//标注文件获取
			String path =  targetPath + File.separator + params.get("paths") ;
			String targetNewFile = path.replace(File.separator+"source"+File.separator, File.separator+"target"+File.separator) ;
			String fileFullName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1,  targetNewFile.length()) ;
			String fileName = targetNewFile.substring(targetNewFile.lastIndexOf(File.separator)+1, targetNewFile.lastIndexOf(".")) ;
			String type = targetNewFile.substring( targetNewFile.lastIndexOf(".")+1 , targetNewFile.length()) ;
			String fileNewFullName = fileName+"-"+type+".json" ;
			targetNewFile = targetNewFile.replace( fileFullName , fileNewFullName ) ;
			NLPLabelsEntity nlpLabelsEntity = JSONObject.parseObject( FileUtils.readJsonFile( targetNewFile ),NLPLabelsEntity.class ) ;

			List<NLPSentenceEntity> nlpSentenceEntities = nlpLabelsEntity.getSentences() ;
			Integer totalCount = nlpSentenceEntities.size() ;
			if( nlpSentenceEntities.size() > 0  ){
				PageParamsUtils.pageParamsDetail(params, (String)params.get("pageCount"), (String)params.get("pageSize"), totalCount+"");
				//分页截取
				Integer startIndex = Integer.parseInt(params.get("pageIndex")+"");
				Integer endIndex = (Integer.parseInt(params.get("pageIndex")+"")+Integer.parseInt(params.get("pageSize")+"") ) > nlpSentenceEntities.size() ? nlpSentenceEntities.size() :
					 Integer.parseInt(params.get("pageIndex")+"")+Integer.parseInt(params.get("pageSize")+"");
				nlpSentenceEntities = nlpSentenceEntities.subList(startIndex , endIndex) ;
				for(NLPSentenceEntity sentence : nlpSentenceEntities){//去除标注信息，只留下分类信息
					sentence.setTags(new ArrayList<>());
					sentence.getAnnotation().setConnectionCategories(new ArrayList<>());
					sentence.getAnnotation().setConnections(new ArrayList<>());
					sentence.getAnnotation().setLabels(new ArrayList<>());
					sentence.getAnnotation().setLabelCategories(new ArrayList<>());
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
