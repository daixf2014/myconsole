package app.action.alertevent;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

public class EditAlertEventController implements Initializable {
	@FXML
	private Button btn_save;
	@FXML
	private Button btn_cancel;
	@FXML
	private ChoiceBox<String> sel_alert_frequency;
	@FXML
	private ToggleGroup radio_group_pre_alert;
	@FXML
	private RadioButton radio_pre_alert_yes;
	@FXML
	private RadioButton radio_pre_alert_no;
	@FXML
	private DatePicker text_alert_date;
	private boolean is_pre_alert;

	@FXML
	private Label label_pre_hours1;
	@FXML
	private Label label_pre_hours2;
	@FXML
	private TextField text_pre_hours;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//提醒频率
		sel_alert_frequency.setItems(FXCollections.observableArrayList("仅一次", "每一天", "每一周", "每一月"));

		String alert_frequency = sel_alert_frequency.getValue();
		if(null == alert_frequency || "".equals(alert_frequency)) {
			alert_frequency = "仅一次";
		}
		sel_alert_frequency.setValue(alert_frequency);

		initPreAlertGroup();
	}

	public void closeEditWin(ActionEvent event) {

		((Node) (event.getSource())).getScene().getWindow().hide();

//		Date now = new Date();
//
//		DateFormat df = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
//		String dateTimeString = df.format(now);
//		// Show in VIEW
//		text_pre_hours.setText(dateTimeString);
	}

	public void initPreAlertGroup() {
		radio_pre_alert_yes.setUserData(true);
		radio_pre_alert_no.setUserData(false);

		showOrHidePreHour();

		//节假日提前提醒
		radio_group_pre_alert.selectedToggleProperty().addListener(
            new ChangeListener<Toggle>() {
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                	if (radio_group_pre_alert.getSelectedToggle() != null) {
                    	showOrHidePreHour();
                    }
                	System.out.println("选的值："+is_pre_alert);
                }
            });
	}

	public void showOrHidePreHour() {
		is_pre_alert = (boolean) radio_group_pre_alert.getSelectedToggle().getUserData();
		if(is_pre_alert) {
    		label_pre_hours1.setVisible(true);
    		label_pre_hours2.setVisible(true);
    		text_pre_hours.setVisible(true);
    	}
    	else {
    		label_pre_hours1.setVisible(false);
    		label_pre_hours2.setVisible(false);
    		text_pre_hours.setVisible(false);
    		text_pre_hours.setText("");
    	}
	}
}
