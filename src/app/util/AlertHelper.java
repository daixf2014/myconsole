package app.util;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

/**
 * �Ի��������
 */
public class AlertHelper {
	public static void showInfoAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
		alert.setTitle("��ʾ");
        alert.setHeaderText("");
        alert.show();
	}

	public static boolean showConfirmAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message);
		alert.setTitle("��ʾ");
        alert.setHeaderText("");

        Optional<ButtonType> _buttonType = alert.showAndWait();
        if(_buttonType.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE)){//���ȷ��
        	return true;
        }
        return false;
	}
}