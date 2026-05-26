package com.sunsheen.hkks.task.unstructure.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.mysql.cj.util.StringUtils;
import com.sunsheen.hkks.task.unstructure.common.entity.AnnotateEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.ConnCategoriesEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.ConnectionEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.ConnectionStringEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.LabelCategoriesEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.LabelEntity;
import com.sunsheen.hkks.task.unstructure.common.entity.RVUserAndAnnotationEntity;

public class AnnotionEntityUtils {
	
	public static List<Map<String,Object>> getStatInfoByFromAndTo(List<RVUserAndAnnotationEntity> entityList , String fromId ,String toId){
		List<Map<String,Object>> statData = new ArrayList<Map<String,Object>>() ; //同一位置上的统计信息
		Map<String,Object> dataMap = new HashMap<String,Object>() ;
		if(entityList != null && entityList.size() > 0){
			//处理统计信息
			if( fromId.contains("--AA--") || toId.contains("--AA--") ){ //查询关系信息
				String from = "" ;
				String to = "" ;
				//先找出fromId ，再找出toId
				for(RVUserAndAnnotationEntity annotationAndUser : entityList){
					for(LabelEntity labels : annotationAndUser.getAnnotation().getLabels()){
						if(labels.getStartIndex().equals( Integer.parseInt(fromId.split("--AA--")[0]) ) &&
									labels.getEndIndex().equals( Integer.parseInt(fromId.split("--AA--")[1])) ){ //是否是此位置
							from = labels.getId()+"" ;
						}
						if(labels.getStartIndex().equals( Integer.parseInt(toId.split("--AA--")[0]) ) &&
								labels.getEndIndex().equals( Integer.parseInt(toId.split("--AA--")[1])) ){ //是否是此位置
							to = labels.getId()+"" ;
						}
					}
				}
				//依据from to 找出 关系标签信息.
				for(RVUserAndAnnotationEntity annotationAndUser : entityList){
					String catagorId = "" ;
					for(ConnectionEntity conns : annotationAndUser.getAnnotation().getConnections()){
						if(conns.getFromId().equals( Integer.parseInt(from) ) && conns.getToId().equals( Integer.parseInt(to) )){ //是否是此位置
							catagorId = conns.getCategoryId() ;
						}
					}
					if( !StringUtils.isEmptyOrWhitespaceOnly(catagorId) ){
						for(ConnCategoriesEntity conncates : annotationAndUser.getAnnotation().getConnectionCategories()){
							if(conncates.getId().equals(catagorId)){ //是否是此Id
								addUUKeyMap(dataMap, conncates.getText(), annotationAndUser.getUserName());
							}
						}
					}
				}
			}else{//查询实体标注信息
				for(RVUserAndAnnotationEntity annotationAndUser : entityList){
					String catagorId = "" ;
					for(LabelEntity labels : annotationAndUser.getAnnotation().getLabels()){
						if(labels.getStartIndex().equals( Integer.parseInt(fromId) ) && labels.getEndIndex().equals( Integer.parseInt(toId) )){ //是否是此位置
							catagorId = labels.getCategoryId() ;
						}
					}
					if( !StringUtils.isEmptyOrWhitespaceOnly(catagorId) ){
						for(LabelCategoriesEntity labelcates : annotationAndUser.getAnnotation().getLabelCategories()){
							if(labelcates.getId().equals(catagorId)){ //是否是此Id
								addUUKeyMap(dataMap, labelcates.getText(), annotationAndUser.getUserName());
							}
						}
					}
				}
			}
			for(Map.Entry<String, Object> map : dataMap.entrySet()){
				Map<String,Object> temp = new HashMap<String,Object>() ;
				String users = map.getValue().toString() ;
				users = users.substring(1, users.length()) ;
				temp.put("users", users ) ;
				temp.put("schemaName", map.getKey()) ;
				temp.put("id", UUID.randomUUID().toString().replace("-", "")) ;
				statData.add(temp) ;
			}
		}else {
			return statData ;
		}
		return statData ;
	}
	
	/**
	 * 
	* @Title: fusionAnnotionAllFromAnnotionList
	* @Description:去重，计数后返回JSON文件信息
	* @author: FengTao
	* @date 2020年9月7日 下午12:02:24
	* @param entityList
	* @return AnnotateEntity
	* @version
	 */
	public static AnnotateEntity fusionAnnotionAllFromAnnotionList(List<AnnotateEntity> entityList){
		//初步校验
		AnnotateEntity annotateEntity = new AnnotateEntity() ;
		if(entityList == null || entityList.size() <= 0){
			return annotateEntity ;
		}
		//1、遍历标注实体信息
		for(int i = 0 ; i < entityList.size() ; i ++){
			for(int j = i+1 ; j < entityList.size() ; j ++){
				//1.1、判断标注内容content、实体标签、关系标签是否相同（来源统一），不同则不做融合
				if(!entityList.get(i).getContent().toString().equals(entityList.get(j).getContent().toString()) ||
						!entityList.get(i).getLabelCategories().equals(entityList.get(j).getLabelCategories()) ||
							!entityList.get(i).getConnectionCategories().equals(entityList.get(j).getConnectionCategories())){
					return annotateEntity ; //1.1.1不做融合，返回空的信息
				}
			}
		}
		//2、到这一步，所有实体就只有标注信息有区别，用第一个实体作为标本
		annotateEntity = entityList.get(0) ;
		List<LabelCategoriesEntity> labelCategories = entityList.get(0).getLabelCategories() ; 
		List<ConnCategoriesEntity> connCategories = entityList.get(0).getConnectionCategories() ; 
		//3、处理出来的融合实体标签信息
		List<LabelEntity> labels = new  ArrayList<LabelEntity>() ; 
		//4、处理出来的融合关系标签信息
		List<ConnectionStringEntity> connections = new ArrayList<ConnectionStringEntity>() ;
		//5、用于每次生成新的ID信息，保证原子性
		AtomicInteger  id = new AtomicInteger(0) ; 
		AtomicInteger  entityIndex = new AtomicInteger(100) ; //叠加数量
		//int  entityIndex = 100 ; 
		for( AnnotateEntity entity : entityList){
			//遍历实体标签信息,添加至labels
			List<LabelEntity> labelsTemp = entity.getLabels() ;
			for( LabelEntity labelEntity : labelsTemp){
			labelEntity.setId(labelEntity.getId()+entityIndex.get()) ; //重新生成ID信息
				labels.add(labelEntity) ;
			}
			//遍历关系标签信息,添加至connections
			List<ConnectionEntity> connectionsTemp = entity.getConnections() ;
			for( ConnectionEntity connectionsEntity : connectionsTemp){
				ConnectionStringEntity connectionStringEntity = new ConnectionStringEntity() ;
				connectionStringEntity.setId(id.getAndIncrement());
				connectionStringEntity.setCategoryId(connectionsEntity.getCategoryId());
				for( LabelEntity labelEntity : labelsTemp){
					String position = labelEntity.getStartIndex()+"--A--"+labelEntity.getEndIndex()+"--A--"+labelEntity.getCategoryId() ;
					if( (connectionsEntity.getFromId()+entityIndex.get()+"").equals(labelEntity.getId()+"")){
						connectionStringEntity.setFromId(position);
					}
					if((connectionsEntity.getToId()+entityIndex.get()+"").equals(labelEntity.getId()+"")){
						connectionStringEntity.setToId(position);
					}
				}
				connections.add(connectionStringEntity) ;
			}
			entityIndex.getAndIncrement() ;
			entityIndex.getAndIncrement() ;
		}
		//6、将信息统计进map信息中,--AA--分割
		Map<String,Object> labelsMap = new HashMap<String,Object>() ;
		for(LabelEntity lalbelEntity : labels){
			String key = lalbelEntity.getStartIndex()+"--AA--"+lalbelEntity.getEndIndex() ;
			if(labelsMap.containsKey(key)) {
				labelsMap.put(key, labelsMap.get(key)+"--AA--"+lalbelEntity.getCategoryId());
			}else {
				labelsMap.put(key, lalbelEntity.getCategoryId());
			}
		}
		Map<String,Object> connectionsMap = new HashMap<String,Object>() ;
		for(ConnectionStringEntity connEntity : connections){
			String key = connEntity.getFromId()+"--AA--"+connEntity.getToId() ;
			if(connectionsMap.containsKey(key)) {
				connectionsMap.put(key, connectionsMap.get(key)+"--AA--"+connEntity.getCategoryId());
			}else {
				connectionsMap.put(key, connEntity.getCategoryId());
			}
		}
		//labels.clear(); 
		//connections.clear();
		//3、处理出来的融合实体标签信息
		List<LabelEntity> labels02 = new  ArrayList<LabelEntity>() ; 
		//4、处理出来的融合关系标签信息
		List<ConnectionEntity> connections02 = new ArrayList<ConnectionEntity>() ;
		List<LabelCategoriesEntity> labelCategories02 = new ArrayList<LabelCategoriesEntity>()  ;
		List<ConnCategoriesEntity> connCategories02 = new ArrayList<ConnCategoriesEntity>() ; 
		//6、去重，统计标注信息
		int index02 = 0 ; //用于构建新的类别id
		for(Map.Entry<String, Object> map : labelsMap.entrySet()){
			String[] keys = map.getKey().split("--AA--") ;
			String[] values = map.getValue().toString().split("--AA--") ;
			Map<String,Integer> result = statKeyAndCount(values) ;
			for(Map.Entry<String, Integer> keyAndCountMap : result.entrySet()){
				LabelEntity labelEntity = new LabelEntity() ;
				labelEntity.setId(id.getAndIncrement());
				labelEntity.setStartIndex( Integer.parseInt(keys[0]) );
				labelEntity.setEndIndex( Integer.parseInt(keys[1]) );
				//筛选并修改相应的实体标签信息
				//String[] keyAndCount = statMaxKey(values).split("--AA--") ;
				String key = keyAndCountMap.getKey() ; //keyAndCount[0] ;
				Integer count = keyAndCountMap.getValue() ; //Integer.parseInt( keyAndCount[1]  ) ;
				labelEntity.setCategoryId(key);
				LabelCategoriesEntity labelCategoriesEntity = new LabelCategoriesEntity() ;
				for(LabelCategoriesEntity lalbelEntity : labelCategories){
					if(lalbelEntity.getId().equals(key)) {
						labelEntity.setCategoryId(key+(++index02));
						labelCategoriesEntity.setId(key+index02);
						labelCategoriesEntity.setBorderColor(lalbelEntity.getBorderColor());
						labelCategoriesEntity.setColor(lalbelEntity.getColor()) ;
						labelCategoriesEntity.setText(lalbelEntity.getText()+"("+count+")");
					}
				}
				labels02.add(labelEntity) ;
				labelCategories02.add(labelCategoriesEntity) ;
			}
		}
		for(Map.Entry<String, Object> map : connectionsMap.entrySet()){
			ConnectionEntity connectionsEntity = new ConnectionEntity() ;
			String[] keys = map.getKey().split("--AA--") ;
			String[] values = map.getValue().toString().split("--AA--") ;
			Map<String,Integer> result = statKeyAndCount(values) ;
			for(Map.Entry<String, Integer> keyAndCountMap : result.entrySet()){
				for( LabelEntity labelEntity : labels02){ //查询当前这个实体是不是fromId或者toId所指的实体信息，是，则构件一个新的关系信息
					String keyId = labelEntity.getStartIndex()+"--A--"+labelEntity.getEndIndex()+"--A--"+
										labelEntity.getCategoryId().toString().substring(0,  labelEntity.getCategoryId().toString().length()-1) ;
					if( keyId.equals(keys[0]) ){ //key2---fromId(由startIndex，endIndex，catagoriesId组成)
						connectionsEntity.setFromId( labelEntity.getId() );
					}
					if( keyId.equals(keys[1]) ){ //key1---toId
						connectionsEntity.setToId( labelEntity.getId() );
					}
				}
				connectionsEntity.setId(id.getAndIncrement());
				//筛选并 相应的关系标签信息
				//String[] keyAndCount = statMaxKey(values).split("--AA--") ;
			
				String key = keyAndCountMap.getKey() ; //keyAndCount[0] ;
				Integer count = keyAndCountMap.getValue() ; //Integer.parseInt( keyAndCount[1]  ) ;
				connectionsEntity.setCategoryId(key );
				ConnCategoriesEntity connCategoriesEntity = new ConnCategoriesEntity() ;
				for(ConnCategoriesEntity connEntity : connCategories){
					if(connEntity.getId().equals(key)) {
						connectionsEntity.setCategoryId(key+(++index02));
						connCategoriesEntity.setId(key+index02);
						connCategoriesEntity.setText(connEntity.getText()+"("+count+")");
					}
				}
				connections02.add(connectionsEntity) ;
				connCategories02.add(connCategoriesEntity);
			}
		}
		annotateEntity.setLabels(labels02);
		annotateEntity.setConnections(connections02);
		annotateEntity.setLabelCategories(labelCategories02);
		annotateEntity.setConnectionCategories(connCategories02);
		//融合完毕，返回
		return annotateEntity ;
	}
	
	/**
	 * 
	* @Title: fusionAnnotionFromAnnotionList
	* @Description:融合content相同的标注信息，相同位置的标注信息，则计数
	* @author: FengTao
	* @date 2020年8月26日 下午3:30:43
	* @param entityList
	* @return AnnotateEntity
	* @version
	 */
	public static AnnotateEntity fusionAnnotionFromAnnotionList(List<AnnotateEntity> entityList){
		//初步校验
		AnnotateEntity annotateEntity = new AnnotateEntity() ;
		if(entityList == null || entityList.size() <= 0){
			return annotateEntity ;
		}
		//1、遍历标注实体信息
		for(int i = 0 ; i < entityList.size() ; i ++){
			for(int j = i+1 ; j < entityList.size() ; j ++){
				//1.1、判断标注内容content、实体标签、关系标签是否相同（来源统一），不同则不做融合
				if(!entityList.get(i).getContent().toString().equals(entityList.get(j).getContent().toString()) ||
						!entityList.get(i).getLabelCategories().equals(entityList.get(j).getLabelCategories()) ||
							!entityList.get(i).getConnectionCategories().equals(entityList.get(j).getConnectionCategories())){
					return annotateEntity ; //1.1.1不做融合，返回空的信息
				}
			}
		}
		//2、到这一步，所有实体就只有标注信息有区别，用第一个实体作为标本
		annotateEntity = entityList.get(0) ;
		List<LabelCategoriesEntity> labelCategories = entityList.get(0).getLabelCategories() ; 
		List<ConnCategoriesEntity> connCategories = entityList.get(0).getConnectionCategories() ; 
		//3、处理出来的融合实体标签信息
		List<LabelEntity> labels = new  ArrayList<LabelEntity>() ; 
		//4、处理出来的融合关系标签信息
		List<ConnectionStringEntity> connections = new ArrayList<ConnectionStringEntity>() ;
		//5、用于每次生成新的ID信息，保证原子性
		AtomicInteger  id = new AtomicInteger(0) ; 
		int  entityIndex = 100 ; 
		for( AnnotateEntity entity : entityList){
			//遍历实体标签信息,添加至labels
			List<LabelEntity> labelsTemp = entity.getLabels() ;
			for( LabelEntity labelEntity : labelsTemp){
			labelEntity.setId(labelEntity.getId()+entityIndex) ; //重新生成ID信息
				labels.add(labelEntity) ;
			}
			//遍历关系标签信息,添加至connections
			List<ConnectionEntity> connectionsTemp = entity.getConnections() ;
			for( ConnectionEntity connectionsEntity : connectionsTemp){
				ConnectionStringEntity connectionStringEntity = new ConnectionStringEntity() ;
				connectionStringEntity.setId(id.getAndIncrement());
				connectionStringEntity.setCategoryId(connectionsEntity.getCategoryId());
				for( LabelEntity labelEntity : labelsTemp){
					String position = labelEntity.getStartIndex()+"--A--"+labelEntity.getEndIndex()+"--A--"+labelEntity.getCategoryId() ;
					if( (connectionsEntity.getFromId()+entityIndex+"").equals(labelEntity.getId()+"")){
						connectionStringEntity.setFromId(position);
					}
					if((connectionsEntity.getToId()+entityIndex+"").equals(labelEntity.getId()+"")){
						connectionStringEntity.setToId(position);
					}
				}
				connections.add(connectionStringEntity) ;
			}
			entityIndex++ ;
			entityIndex++ ;
		}
		//6、将信息统计进map信息中,--AA--分割
		Map<String,Object> labelsMap = new HashMap<String,Object>() ;
		for(LabelEntity lalbelEntity : labels){
			String key = lalbelEntity.getStartIndex()+"--AA--"+lalbelEntity.getEndIndex() ;
			if(labelsMap.containsKey(key)) {
				labelsMap.put(key, labelsMap.get(key)+"--AA--"+lalbelEntity.getCategoryId());
			}else {
				labelsMap.put(key, lalbelEntity.getCategoryId());
			}
		}
		Map<String,Object> connectionsMap = new HashMap<String,Object>() ;
		for(ConnectionStringEntity connEntity : connections){
			String key = connEntity.getFromId()+"--AA--"+connEntity.getToId() ;
			if(connectionsMap.containsKey(key)) {
				connectionsMap.put(key, connectionsMap.get(key)+"--AA--"+connEntity.getCategoryId());
			}else {
				connectionsMap.put(key, connEntity.getCategoryId());
			}
		}
		//labels.clear(); 
		//connections.clear();
		//3、处理出来的融合实体标签信息
		List<LabelEntity> labels02 = new  ArrayList<LabelEntity>() ; 
		//4、处理出来的融合关系标签信息
		List<ConnectionEntity> connections02 = new ArrayList<ConnectionEntity>() ;
		List<LabelCategoriesEntity> labelCategories02 = new ArrayList<LabelCategoriesEntity>()  ;
		List<ConnCategoriesEntity> connCategories02 = new ArrayList<ConnCategoriesEntity>() ; 
		//6、去重，统计标注信息
		int index02 = 0 ; //用于构建新的类别id
		for(Map.Entry<String, Object> map : labelsMap.entrySet()){
			String[] keys = map.getKey().split("--AA--") ;
			String[] values = map.getValue().toString().split("--AA--") ;
			LabelEntity labelEntity = new LabelEntity() ;
			labelEntity.setId(id.getAndIncrement());
			labelEntity.setStartIndex( Integer.parseInt(keys[0]) );
			labelEntity.setEndIndex( Integer.parseInt(keys[1]) );
			//筛选并修改相应的实体标签信息
			String[] keyAndCount = statMaxKey(values).split("--AA--") ;
			String key = keyAndCount[0] ;
			Integer count = Integer.parseInt( keyAndCount[1]  ) ;
			labelEntity.setCategoryId(key);
			LabelCategoriesEntity labelCategoriesEntity = new LabelCategoriesEntity() ;
			for(LabelCategoriesEntity lalbelEntity : labelCategories){
				if(lalbelEntity.getId().equals(key)) {
					labelEntity.setCategoryId(key+(++index02));
					labelCategoriesEntity.setId(key+index02);
					labelCategoriesEntity.setBorderColor(lalbelEntity.getBorderColor());
					labelCategoriesEntity.setColor(lalbelEntity.getColor()) ;
						labelCategoriesEntity.setText(lalbelEntity.getText()+"("+count+")");
				}
			}
			labels02.add(labelEntity) ;
			labelCategories02.add(labelCategoriesEntity) ;
		}
		for(Map.Entry<String, Object> map : connectionsMap.entrySet()){
			ConnectionEntity connectionsEntity = new ConnectionEntity() ;
			String[] keys = map.getKey().split("--AA--") ;
			String[] values = map.getValue().toString().split("--AA--") ;
			for( LabelEntity labelEntity : labels02){ //查询当前这个实体是不是fromId或者toId所指的实体信息，是，则构件一个新的关系信息
				String keyId = labelEntity.getStartIndex()+"--A--"+labelEntity.getEndIndex()+"--A--"+
									labelEntity.getCategoryId().toString().substring(0,  labelEntity.getCategoryId().toString().length()-1) ;
				if( keyId.equals(keys[0]) ){ //key2---fromId(由startIndex，endIndex，catagoriesId组成)
					connectionsEntity.setFromId( labelEntity.getId() );
				}
				if( keyId.equals(keys[1]) ){ //key1---toId
					connectionsEntity.setToId( labelEntity.getId() );
				}
			}
			connectionsEntity.setId(id.getAndIncrement());
			//筛选并 相应的关系标签信息
			String[] keyAndCount = statMaxKey(values).split("--AA--") ;
			String key = keyAndCount[0] ;
			Integer count = Integer.parseInt( keyAndCount[1]  ) ;
			connectionsEntity.setCategoryId(key );
			ConnCategoriesEntity connCategoriesEntity = new ConnCategoriesEntity() ;
			for(ConnCategoriesEntity connEntity : connCategories){
				if(connEntity.getId().equals(key)) {
					connectionsEntity.setCategoryId(key+(++index02));
					connCategoriesEntity.setId(key+index02);
					connCategoriesEntity.setText(connEntity.getText()+"("+count+")");
				}
			}
			connections02.add(connectionsEntity) ;
			connCategories02.add(connCategoriesEntity);
		}
		annotateEntity.setLabels(labels02);
		annotateEntity.setConnections(connections02);
		annotateEntity.setLabelCategories(labelCategories02);
		annotateEntity.setConnectionCategories(connCategories02);
		//融合完毕，返回
		return annotateEntity ;
	}
	
	/**
	 * 	
	* @Title: subEntityByRelFromAndTo
	* @Description: 根据关系标签截取json数据
	* @author: FengTao
	* @date 2020年8月17日 下午12:02:18 void
	* @version
	 */
	public static AnnotateEntity subAnnotionByRelFromAndTo(AnnotateEntity entity,List<String> connCateIds, Integer fromId ,Integer toId){
		
		//获取最大的下标和最小的下标
		Integer maxIndex = 0 ; 
		Integer minIndex = 0 ; 
		
		//只留下fromId/toId的实体标签
		Iterator<LabelEntity> labelIterator = entity.getLabels().iterator();
		while(labelIterator.hasNext()){
			LabelEntity label = labelIterator.next() ;
			if(fromId.equals(label.getId()) || toId.equals(label.getId())){	
				if(label.getEndIndex() > maxIndex){
					maxIndex = label.getEndIndex() ;
				}
				if(label.getStartIndex() < minIndex){
					minIndex = label.getStartIndex() ;
				}
			}else{
				labelIterator.remove() ;
			}
		}
		
		//前后截取窗口大小为5个字符
		String originContent = entity.getContent() ;
		String content = entity.getContent() ;
		Integer leftIndex = (minIndex-5) < 0 ? 0 : (minIndex-5) ;
		Integer rightIndex = (maxIndex+5) > originContent.length() ? originContent.length() : (maxIndex+5) ;
		Integer subLeftLength =  originContent.substring(0, leftIndex).length();
		content = originContent.substring( leftIndex,rightIndex ) ;		
	    entity.setContent(content);
		
	  //只留下fromId/toId的实体标签
	  	while(labelIterator.hasNext()){
	  		LabelEntity label = labelIterator.next() ;
	  		if(fromId.equals(label.getId()) || toId.equals(label.getId())){	
				label.setStartIndex(label.getStartIndex()-subLeftLength);
				label.setEndIndex(label.getEndIndex()-subLeftLength);
	  		}
	  	}
	    
		//遍历更新关系标注数据
		Iterator<ConnectionEntity> iterator = entity.getConnections().iterator();
		while(iterator.hasNext()){
			ConnectionEntity label = iterator.next() ;
			if(connCateIds.contains(label.getCategoryId())
						&&fromId.equals(label.getFromId())
							&&toId.equals(label.getToId())){	
			}else{
				iterator.remove() ;
			}
		}
		return entity ;
	}
	
	/**
	 * 	
	* @Title: subEntityByRelFromAndTo
	* @Description: 根据实体标签截取json数据
	* @author: FengTao
	* @date 2020年8月17日 下午12:02:18 void
	* @version
	 */
	public static AnnotateEntity subAnnotionByEntityFromAndTo(AnnotateEntity entity,List<String> labelCateIds, Integer fromId ,Integer toId){
		
		//前后截取窗口大小为10个字符
		String originContent = entity.getContent() ;
		String content = entity.getContent() ;
		Integer leftIndex = (fromId-15) < 0 ? 0 : (fromId-15) ;
		Integer rightIndex = (toId+15) > originContent.length() ? originContent.length() : (toId+15) ;
		Integer subLeftLength =  originContent.substring(0, leftIndex).length();
		content = originContent.substring( leftIndex,rightIndex ) ;
		 
		entity.setContent(content);
		//更新实体标注信息
		Iterator<LabelEntity> labelIterator = entity.getLabels().iterator();
		while(labelIterator.hasNext()){
			LabelEntity label = labelIterator.next() ;
			if(labelCateIds.contains(label.getCategoryId())
						&&fromId.equals(label.getStartIndex())
							&&toId.equals(label.getEndIndex())){
				//将内容截为前后5个字符窗口大小
				label.setStartIndex(label.getStartIndex()-subLeftLength);
				label.setEndIndex(label.getEndIndex()-subLeftLength);
			}else{
				labelIterator.remove() ;
			}
		}
		entity.setConnections(new ArrayList<>());
		return entity;
	}
	
	public static String[] replaceFromToId(AnnotateEntity entity,List<String> labelCateIds, String fromId ,String toId){
		String[] fromIds = fromId.split("--AA--") ;
		String[] toIds = toId.split("--AA--") ;
		String[] ids = new String[2] ;
		//根据实体标注信息查找fromID，toId
		Iterator<LabelEntity> labelIterator = entity.getLabels().iterator();
		while(labelIterator.hasNext()){
			LabelEntity label = labelIterator.next() ;
			if( fromIds[0].equals(label.getStartIndex()+"") && fromIds[1].equals(label.getEndIndex()+"") ){
				ids[0] = label.getId()+"" ;
			}
			if( toIds[0].equals(label.getStartIndex()+"") && toIds[1].equals(label.getEndIndex()+"") ){
				ids[1] = label.getId()+"" ;
			}
	   }
	   return ids;
	} 
	
	/**
	 * 
	* @Title: replaceLabelCategories
	* @Description: 替换实体标签list，更新了实体标签，会更新，关系标签，标注实体数据，标注关系数据
	* @author: FengTao
	* @date 2020年8月17日 下午2:12:42
	* @param entity
	* @param labelCategories void
	* @version
	 */
	public static AnnotateEntity replaceLabelCategories(AnnotateEntity entity,List<LabelCategoriesEntity> labelCategories){
		
		entity.setLabelCategories(labelCategories);
		
		Set<String> connCateIds = new HashSet<String>() ;
		for(LabelCategoriesEntity labelCate : labelCategories){
			connCateIds.add(labelCate.getId()) ;
		}
		//更新实体标注信息
		Iterator<LabelEntity> labelIterator = entity.getLabels().iterator();
		while(labelIterator.hasNext()){
			String id = labelIterator.next().getCategoryId() ;
			if(!connCateIds.contains(id)){
				labelIterator.remove() ;
			}
		}
		
		Set<Integer> labelIds = new HashSet<Integer>() ;
		for(LabelEntity label : entity.getLabels()){
			labelIds.add(label.getId()) ;
		}
		//遍历更新关系标注数据
		Iterator<ConnectionEntity> connIterator = entity.getConnections().iterator();
		while(connIterator.hasNext()){
			Integer fromId = connIterator.next().getToId() ;
			Integer toId = connIterator.next().getFromId() ;
			if(!labelIds.contains(fromId) && !labelIds.contains(toId)){
				connIterator.remove() ;
			}
		}
		
		
		return entity ;
	}
	
	/**
	 * 
	* @Title: replaceLabelCategories
	* @Description: 替换关系标签list，更新了关系标签，只需要更新关系标注数据
	* @author: FengTao
	* @date 2020年8月17日 下午2:12:42
	* @param entity
	* @param labelCategories void
	* @version
	 */
	public static AnnotateEntity replaceConnectionCategories(AnnotateEntity entity,List<ConnCategoriesEntity> connectionCategories){
		
		entity.setConnectionCategories(connectionCategories);
		
		List<String> connCateIds = new ArrayList<String>() ;
		for(ConnCategoriesEntity connectionCate : connectionCategories){
			connCateIds.add(connectionCate.getId()) ;
		}
		//遍历更新关系标注数据
		Iterator<ConnectionEntity> iterator = entity.getConnections().iterator();
		while(iterator.hasNext()){
			String id = iterator.next().getCategoryId() ;
			if(!connCateIds.contains(id)){
				iterator.remove() ;
			}
		}
		return entity ;
	}
	
	public static void judgeandAddInitLabelCategories(AnnotateEntity annotation){
		if(annotation != null && (annotation.getLabelCategories()==null || annotation.getLabelCategories().size() <= 0)){
			LabelCategoriesEntity temp = new LabelCategoriesEntity();
			temp.setId("originId");
			temp.setText("初始化");
			temp.setColor("#ffff");
			temp.setBorderColor("#ffff") ;
			annotation.setLabelCategories(new ArrayList<LabelCategoriesEntity>() );
			annotation.getLabelCategories().add(temp) ;
		}
	}
	
	public static void judgeandAddInitLabelCategories(List<LabelCategoriesEntity> labelCategories){
			LabelCategoriesEntity temp = new LabelCategoriesEntity();
			temp.setId("originId");
			temp.setText("初始化");
			temp.setColor("#ffff");
			temp.setBorderColor("#ffff") ;
			labelCategories.add(temp) ;
	}
	
	/**
	 * 
	* @Title: statMaxKey
	* @Description: 找出最大的map信息
	* @author: FengTao
	* @date 2020年8月26日 下午5:49:38
	* @param params
	* @return Map<String,Integer>
	* @version
	 */
	public static String statMaxKey(String[] params){
		Map<String,Integer> result = new HashMap<String,Integer>() ;
		
		for (String string : params) {
			if(result.containsKey(string)) {
				result.put(string, result.get(string).intValue()+1);
			}else {
				result.put(string, new Integer(1));
			}
		}
		int max = 0 ;
		String key = "" ;
		for(Map.Entry<String, Integer> temp : result.entrySet()){
			if( temp.getValue() > max){
				key = temp.getKey() ;
				max = temp.getValue() ;
			}
		}
		Iterator<Map.Entry<String, Integer>> iterator = result.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<String, Integer> entry = iterator.next();
			if(!entry.getKey().equals(key)){ //只留下value最大的值
				iterator.remove();
			}
		}
		for(Map.Entry<String, Integer> temp : result.entrySet()){
			key = temp.getKey() ;
			max = temp.getValue() ;
		}
		
		return key+"--AA--"+max ;
	}
	/**
	 * 
	* @Title: statKeyAndCount
	* @Description: 找出各个Key出现的次数
	* @author: FengTao
	* @date 2020年9月7日 下午1:50:37
	* @param params
	* @return Map<String,Integer>
	* @version
	 */
	public static Map<String,Integer> statKeyAndCount(String[] params){
		Map<String,Integer> result = new HashMap<String,Integer>() ;
		
		for (String string : params) {
			if(result.containsKey(string)) {
				result.put(string, result.get(string).intValue()+1);
			}else {
				result.put(string, new Integer(1));
			}
		}
		return result ;
	}
	
    public static void addUUKeyMap(Map<String,Object> statMap , String key , String value){
    	value = ","+value ;
        if(statMap.containsKey(key)){
            statMap.put(key,statMap.get(key)+value) ;
        }else{
            statMap.put(key,value) ;
        }
    }
	
}
