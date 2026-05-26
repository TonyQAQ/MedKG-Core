package com.sunsheen.hkks.task.pictureMark.entity;

import java.util.List;

public class PictureLabelsConnection {
	private List<String> labelId;
	private String pictureName;
	public List<String> getLabelId() {
		return labelId;
	}
	public void setLabelId(List<String> labelId) {
		this.labelId = labelId;
	}
	public String getPictureName() {
		return pictureName;
	}
	public void setPictureName(String pictureName) {
		this.pictureName = pictureName;
	}
	@Override
	public String toString() {
		return "PictureLabelsConnection [labelId=" + labelId + ", pictureName="
				+ pictureName + "]";
	}
	
	
}
