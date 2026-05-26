package com.sunsheen.hkks.task.structure.ssm.entity;

public class Filter {
	//过滤规则ID
	private String filterId ;
	//规则名称
	private String filterValue ;
	
	public String getFilterId() {
		return filterId;
	}
	public void setFilterId(String filterId) {
		this.filterId = filterId;
	}
	public String getFilterValue() {
		return filterValue;
	}
	public void setFilterValue(String filterValue) {
		this.filterValue = filterValue;
	}
	@Override
	public String toString() {
		return "Filter [filterId=" + filterId
				+ ", filterValue=" + filterValue + "]";
	}
	
}
