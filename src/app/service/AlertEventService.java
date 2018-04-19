package app.service;

import java.util.List;
import java.util.Map;

import app.action.TableController;

/**
 * 事件提醒
 * @author daixf
 */
public class AlertEventService extends BaseService {

    public List<Map<String, Object>> getAlertEventList(TableController tbc, String sql, List<String> paramList, int pageSize, int curentPage) {
    	return db.getForList(tbc, sql, paramList, pageSize, curentPage);
    }

    public int deleteAlertEvent(String aId) {
    	String sql = "update t_alert_event set status = -1 where a_id = ? ";
    	return db.update(sql, new Object[]{aId});
    }
}
