package app.bean;

import java.util.Map;

import app.util.StringHelper;

public class DictionaryItem {
	private String itemId;
	private String groupId;
	private String itemName;
    private String itemValue;
    private int itemOrder;
    private String remark;

    public DictionaryItem() {

    }

    public DictionaryItem(Map<String, Object> map) {
    	this.itemId = StringHelper.get(map, "item_id");
    	this.groupId = StringHelper.get(map, "group_id");
    	this.itemName = StringHelper.get(map, "item_name");
    	this.itemValue = StringHelper.get(map, "item_value");
    	this.itemOrder = StringHelper.getInt(map, "item_order");
    	this.remark = StringHelper.get(map, "remark");
    }

	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getItemValue() {
		return itemValue;
	}
	public void setItemValue(String itemValue) {
		this.itemValue = itemValue;
	}
	public int getItemOrder() {
		return itemOrder;
	}
	public void setItemOrder(int itemOrder) {
		this.itemOrder = itemOrder;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
}
