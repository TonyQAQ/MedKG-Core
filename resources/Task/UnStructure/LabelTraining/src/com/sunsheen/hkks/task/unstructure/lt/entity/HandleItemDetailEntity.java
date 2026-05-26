package com.sunsheen.hkks.task.unstructure.lt.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;


@Entity
@Table(name = "HANDLE_ITEM_DETAIL")
public class HandleItemDetailEntity {
	@Id
	@Column(name = "ITEM_ID")
	@GeneratedValue(strategy = GenerationType.AUTO,generator = "UUID") //AUTO代表程序控制主键
	@GenericGenerator(name = "UUID", strategy = "uuid")
	private String itemId ;
	
	@Column(name = "EVAL_ID")
	private String evalId ;
	
	@Column(name = "ATTR_MAPPING_ID")
	private String attrMappingId ;
	
	@Column(name = "ROW_INDEX")
	private String rowIndex ;

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getEvalId() {
		return evalId;
	}

	public void setEvalId(String evalId) {
		this.evalId = evalId;
	}

	public String getAttrMappingId() {
		return attrMappingId;
	}

	public void setAttrMappingId(String attrMappingId) {
		this.attrMappingId = attrMappingId;
	}

	public String getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(String rowIndex) {
		this.rowIndex = rowIndex;
	}

	@Override
	public String toString() {
		return "HandleItemDetailEntity [itemId=" + itemId + ", evalId="
				+ evalId + ", attrMappingId=" + attrMappingId + ", rowIndex="
				+ rowIndex + "]";
	}
}
