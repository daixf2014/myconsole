package app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import app.bean.DictionaryItem;
import app.util.db.DBHelper;

/**
 * FXML Controller class
 * @author E40-G8C
 */
public class BaseService {

	public static Logger logger = Logger.getLogger(BaseService.class);
	public static DBHelper db = DBHelper.getNewInstance();

	public DBHelper getDBHelper() {
		return db;
	}

	/**
	 * 获取字典项集合
	 * @param groupId
	 * @return
	 */
	public List<DictionaryItem> getDictionaryItemList(String groupId) {
		String sql = "select a.item_id, a.group_id, a.item_name, a.item_value, a.item_order, a.remark from t_dictionary_item a " +
					 " where a.group_id = ? and a.status = 1 order by a.item_order ";
    	List<Map<String, Object>> list = db.queryForList(sql, new Object[]{groupId});

    	List<DictionaryItem> dictionaryItemList = new ArrayList<DictionaryItem>();
    	for(Map<String, Object> map : list) {
    		DictionaryItem alertEvent = new DictionaryItem(map);
    		dictionaryItemList.add(alertEvent);
    	}
    	return dictionaryItemList;
    }

	/**
	 * 获取字典项实例
	 * @param groupId
	 * @param itemName
	 * @return
	 */
	public DictionaryItem getDictionaryItem(String groupId, String itemName) {
		String sql = "select a.item_id, a.group_id, a.item_name, a.item_value, a.item_order, a.remark from t_dictionary_item a " +
					 " where a.group_id = ? and a.item_name = ? and a.status = 1 order by a.item_order ";
    	Map<String, Object> map = db.queryForMap(sql, new Object[]{groupId, itemName});
    	return new DictionaryItem(map);
    }

	/**
	 * 获取字典项实例
	 * @param dictionaryItemList
	 * @param itemName
	 * @return
	 */
	public DictionaryItem getDictionaryItem(List<DictionaryItem> dictionaryItemList, String itemName) {
    	for(DictionaryItem item : dictionaryItemList) {
    		if(itemName.equals(item.getItemName())) {
    			return item;
    		}
    	}
    	return null;
    }
}
