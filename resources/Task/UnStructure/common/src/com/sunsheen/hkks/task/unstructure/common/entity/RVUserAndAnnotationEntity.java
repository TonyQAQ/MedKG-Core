package com.sunsheen.hkks.task.unstructure.common.entity;

/**
 * 
 * @Title: RVUserAndAnnotationEntity
 * @Description: 结果校验的标签实体信息
 * @author: FengTao
 * @date 2020年9月15日 下午2:27:41
 */
public class RVUserAndAnnotationEntity {
	private AnnotateEntity annotation ;
	private String userName ;
	private String userId ;
	public AnnotateEntity getAnnotation() {
		return annotation;
	}
	public void setAnnotation(AnnotateEntity annotation) {
		this.annotation = annotation;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	@Override
	public String toString() {
		return "RVUserAndAnnotationEntity [annotation=" + annotation
				+ ", userName=" + userName + ", userId=" + userId + "]";
	}
}

