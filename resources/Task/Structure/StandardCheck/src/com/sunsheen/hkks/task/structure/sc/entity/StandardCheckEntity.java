package com.sunsheen.hkks.task.structure.sc.entity;

import java.util.List;

import com.sunsheen.hkks.task.structure.ssm.entity.Filter;

/**
 * 
 * @Title: StandardCheckEntity
 * @Description: 保存提交的实体
 * @author: FengTao
 * @date 2020年7月29日 上午10:19:46
 */
public class StandardCheckEntity {

	//指定任务下的表ID
	private String tableMappingId ;
	//指定任务下的属性ID
	private String attrMappingId ;
	//属性别名
	private String attrAlias ;
	//执行者Id
	private String userId ;
	//执行者名称
	private String userName ;
	//属性过滤规则
	private List<Filter> filterList;
	
	public String getTableMappingId() {
		return tableMappingId;
	}
	public void setTableMappingId(String tableMappingId) {
		this.tableMappingId = tableMappingId;
	}
	public String getAttrMappingId() {
		return attrMappingId;
	}
	public void setAttrMappingId(String attrMappingId) {
		this.attrMappingId = attrMappingId;
	}
	public String getAttrAlias() {
		return attrAlias;
	}
	public void setAttrAlias(String attrAlias) {
		this.attrAlias = attrAlias;
	}
	public List<Filter> getFilterList() {
		return filterList;
	}
	public void setFilterList(List<Filter> filterList) {
		this.filterList = filterList;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	@Override
	public String toString() {
		return "SSMEntity [tableMappingId=" + tableMappingId
				+ ", attrMappingId=" + attrMappingId + ", attrAlias="
				+ attrAlias + ", userId=" + userId + ", userName=" + userName
				+ ", filterList=" + filterList + "]";
	}
}
