package com.sunsheen.hkks.common.uitl.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 
 * @Title: AlgorithmUtils
 * @Description: 数据抽取相关分配算法
 * @author: FengTao
 * @date 2020年8月24日 下午3:56:15
 */
public class AlgorithmUtils {
	
	/**
	 * 
	* @Title: randomRangeByMap
	* @Description: 根据任务执行人平均分配任务信息
	* @author: FengTao
	* @date 2020年8月28日 上午9:03:03
	* @param map
	* @param users
	* @return List<String>
	* @version
	 */
	public static List<String> randomRangeByMap(Map<String,Integer> map ,List<String> users){
		System.out.print("属性列表---"+map);
		System.out.print("用户列表---"+users);
		List<String> list = new ArrayList<String>() ;
		Integer userCount = users.size() ; //用户数量
		for( Map.Entry<String, Integer> entry : map.entrySet()){
			String attrMappingId = entry.getKey() ; //字段
			Integer columnsTotalCount = entry.getValue() ; //字段过滤规则下的数据总数
			Integer assignCount = columnsTotalCount/userCount ; //均分数量
			Integer remainder = columnsTotalCount%userCount ; //余数
			Integer rowStart = 0 ;
			Integer rowEnd = 0 ;
			for(int i = 0 ; i < users.size() ; i++){
				rowEnd = assignCount*(i+1)-1 ;
				if(i == users.size()-1){
					assignCount += remainder ;
					rowEnd += remainder ;
				}
				String key = attrMappingId + "--AA--" + users.get(i) + "--AA--" + rowStart + "--AA--" + rowEnd + "--AA--" + assignCount;
				list.add(key) ;
				rowStart += assignCount ;
			}
		}
		return list ;
	}
	
	/**
	 * 
	* @Title: randomIndexByMap
	* @Description: 根据权重分配下标信息
	* @author: FengTao
	* @date 2020年8月24日 下午4:03:38
	* @param map
	* @param totalCount
	* @return List<String>
	* @version
	 */
	public static List<String> randomIndexByMap(Map<String,Integer> map , Integer trainCount){
		Integer totalCount = 0 ; //数据总量
		Integer deal = 0 ; //筛选总量
		String key = "" ; //数据量最大的key
		Integer max = 0 ; //数据量最大的个数
		List<String> list = new ArrayList<String>() ; //处理完毕的数据信息(--AA--分隔，，，tableName-columns-rowIndex)
		Map<String,Integer> extractCountMap = new HashMap<String,Integer>() ;
		//首次遍历，获取数据总量
		for(Map.Entry<String, Integer> temp : map.entrySet()){ 
			totalCount += temp.getValue() ; 
		}
		//再次遍历，按比例分配
		for(Map.Entry<String, Integer> temp : map.entrySet()){
			if(temp.getValue() > max){
				max = temp.getValue() ;
				key = temp.getKey() ;
			}
			int count = 0 ;
			count = (int)Math.round( ((double)temp.getValue()/totalCount)*trainCount ) ;
			extractCountMap.put(temp.getKey(), count) ;
			deal += count ;
		}
		//精度丢失的数据进行特殊处理
		if(deal > trainCount){ //筛选总数大于需要训练量
			extractCountMap.put(key, extractCountMap.get(key)-(deal-trainCount)) ;
		}else if(deal < trainCount){ //筛选总数小于训练量
			extractCountMap.put(key, extractCountMap.get(key)+(trainCount-deal)) ;
		}
		//根据分配信息,两次遍历去除需要的下标信息
		
		for(Map.Entry<String, Integer> temp : extractCountMap.entrySet()){
			List<Integer> indexList = new ArrayList<Integer>() ;
			for(int i = 0 ; i < temp.getValue() ; i ++ ){
				int index = 0 ;
				//while防止随机数相同
				while(!indexList.contains(index)){
					index = new Random().nextInt(map.get(temp.getKey())-1) ;
					indexList.add(index) ;
				}
				list.add(temp.getKey()+index) ;
			}
		}
		return list ;
	}
	
	/**
	 * 
	* @Title: randomByMax
	* @Description:
	* @author: FengTao
	* @date 2020年8月24日 下午5:28:01
	* @return int
	* @version
	 */
	public static int randomByMax(Integer max){
		Random random = new Random() ;
		return random.nextInt(max) ;
	}
}
