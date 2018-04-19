package app.bean;

import java.util.Map;

import app.util.StringHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AlertEvent {
	private final StringProperty rowNum;
	private final StringProperty aId;
	private final StringProperty alertFrequency;
    private final StringProperty alertDate;
    private final StringProperty alertTime;
    private final StringProperty isPreAlert;
    private final StringProperty preHours;
    private final StringProperty alertContent;
    private final StringProperty status;

    public AlertEvent() {
    	this(null, null, null, null, null, 0, 0, null, 1);
    }

    public AlertEvent(String rowNum, String aId, String alertFrequency, String alertDate, String alertTime, int isPreAlert,
    		double preHours, String alertContent, int status) {
    	this.rowNum = new SimpleStringProperty(rowNum);
    	this.aId = new SimpleStringProperty(aId);
    	this.alertFrequency = new SimpleStringProperty(alertFrequency);
    	this.alertDate = new SimpleStringProperty(alertDate);
    	this.alertTime = new SimpleStringProperty(alertTime);
    	this.isPreAlert = new SimpleStringProperty(isPreAlert == 1 ? "是" : "否");
    	this.preHours = new SimpleStringProperty(String.valueOf(preHours));
    	this.alertContent = new SimpleStringProperty(alertContent);
    	this.status = new SimpleStringProperty(this.getStatusNameByStatus(status));
    }

    public AlertEvent(Map<String, Object> map) {
    	this.rowNum = new SimpleStringProperty(StringHelper.get(map, "row_num"));
    	this.aId = new SimpleStringProperty(StringHelper.get(map, "a_id"));
    	this.alertFrequency = new SimpleStringProperty(StringHelper.get(map, "alert_frequency"));
    	this.alertDate = new SimpleStringProperty(StringHelper.get(map, "alert_date"));
    	this.alertTime = new SimpleStringProperty(StringHelper.get(map, "alert_time"));
    	this.isPreAlert = new SimpleStringProperty(StringHelper.getInt(map, "is_pre_alert") == 1 ? "是" : "否");
    	this.preHours = new SimpleStringProperty(StringHelper.get(map, "pre_hours"));
    	this.alertContent = new SimpleStringProperty(StringHelper.get(map, "alert_content"));
    	this.status = new SimpleStringProperty(this.getStatusNameByStatus(StringHelper.getInt(map, "status")));
    }

    public StringProperty getRowNumProperty() {
		return rowNum;
	}
	public String getRowNum() {
		return rowNum.get();
	}
	public void setRowNum(String rowNum) {
        this.rowNum.set(rowNum);
    }
	public StringProperty getAIdProperty() {
		return aId;
	}
	public String getAId() {
		return aId.get();
	}
	public void setAId(String aId) {
		this.aId.set(aId);
	}
    public StringProperty getAlertFrequencyProperty() {
		return alertFrequency;
	}
	public String getAlertFrequency() {
		return alertFrequency.get();
	}
	public void setAlertFrequency(String alertFrequency) {
        this.alertFrequency.set(alertFrequency);
    }
	public StringProperty getAlertDateProperty() {
		return alertDate;
	}
	public String getAlertDate() {
		return alertDate.get();
	}
	public void setAlertDate(String alertDate) {
        this.alertDate.set(alertDate);
    }
	public StringProperty getAlertTimeProperty() {
		return alertTime;
	}
	public String getAlertTime() {
		return alertTime.get();
	}
	public void setAlertTime(String alertTime) {
        this.alertTime.set(alertTime);
    }
	public String getIsPreAlert() {
		return isPreAlert.get();
	}
	public void setIsPreAlert(String isPreAlert) {
        this.isPreAlert.set(isPreAlert);
    }
	public StringProperty getPreHoursProperty() {
		return preHours;
	}
	public double getPreHours() {
		return Double.parseDouble(preHours.get());
	}
	public void setPreHours(double preHours) {
        this.preHours.set(String.valueOf(preHours));
    }
	public StringProperty getAlertContentProperty() {
		return alertContent;
	}
	public String getAlertContent() {
		return alertContent.get();
	}
	public void setAlertContent(String alertContent) {
        this.alertContent.set(alertContent);
    }
	public StringProperty getStatusProperty() {
		return status;
	}
	public void setStatus(int status) {
		this.status.set(this.getStatusNameByStatus(status));
    }
	public int getStatus() {
		int status = 1;
		String statusName = this.status.get();
		if("已过期".equals(statusName)) {
			status = 0;
		}
		if("已删除".equals(statusName)) {
			status = -1;
		}
		return status;
	}

    public String getStatusNameByStatus(int status) {
    	return status == 1 ? "未过期" : (status == -1 ? "已删除" : "已过期");
    }
}
