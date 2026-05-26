package com.sunsheen.hkks.task.unstructure.common.entity;

public class ConnectionEntity {

    private Integer fromId ;
    private Integer toId ;
    private Integer id ;
    private String categoryId ;

    public Integer getFromId() {
        return fromId;
    }

    public void setFromId(Integer fromId) {
        this.fromId = fromId;
    }

    public Integer getToId() {
        return toId;
    }

    public void setToId(Integer toId) {
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
