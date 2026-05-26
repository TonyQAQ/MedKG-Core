package com.sunsheen.hkks.task.unstructure.common.entity;

public class ConnectionStringEntity {

    private String fromId ;
    private String toId ;
    private Integer id ;
    private String categoryId ;

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

	@Override
	public String toString() {
		return "ConnectionEntity [fromId=" + fromId + ", toId=" + toId
				+ ", id=" + id + ", categoryId=" + categoryId + "]";
	}
    
}
