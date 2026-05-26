package com.sunsheen.hkks.nlp.common.entity;

public class LabelEntity {
    private Integer startIndex ;
    private Integer endIndex ;
    private Integer id ;
    private String categoryId ;

    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    public Integer getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(Integer endIndex) {
        this.endIndex = endIndex;
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
		return "LabelEntity [startIndex=" + startIndex + ", endIndex="
				+ endIndex + ", id=" + id + ", categoryId=" + categoryId + "]";
	}
    
    
}
