package app.action;

import java.net.URL;
import java.util.ResourceBundle;

import app.Main;
import app.action.alertevent.AlertEventController;
import app.action.dailywork.DailyWorkController;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * FXML Controller class
 * @author daixf
 */
public class MainPageController extends BaseController implements Initializable {

    private static MainPageController instance;

    public static MainPageController getInstance() {
        return instance;
    }
    @FXML
    AnchorPane titlepane, menupane, central_panel;
    @FXML
    Label message;
    @FXML
    Button btn_alert_event;

    public void showMessage(String msg) {
        System.out.println("[Message]: " + msg);
        message.setTextFill(Color.WHITE);
        message.setText(msg);
        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(0.3), new KeyValue(message.opacityProperty(), 1), new KeyValue(message.translateZProperty(), 10)),
                new KeyFrame(Duration.seconds(3.0), new KeyValue(message.opacityProperty(), 0), new KeyValue(message.translateZProperty(), 0)));
        tl.play();
    }

    public void showErrorMessage(String msg) {
        System.err.println("[Error]: " + msg);
        message.setTextFill(Color.YELLOW);
        message.setText(msg);
        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(0.3), new KeyValue(message.opacityProperty(), 1), new KeyValue(message.translateZProperty(), 10)),
                new KeyFrame(Duration.seconds(3.0), new KeyValue(message.opacityProperty(), 0), new KeyValue(message.translateZProperty(), 0)));
        tl.play();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
    }

    private Button defaultBtn = null;

    @FXML
    private void showAlertEventPanel(ActionEvent e) {
        setPanel(Main.alertEventPanel, "打开事件提醒面板");
        Button btn = (Button)e.getSource();
        if(defaultBtn != null) {
            defaultBtn.setDefaultButton(false);
        }
        btn.setDefaultButton(true);
        defaultBtn = btn;
        AlertEventController.getInstance().loadTable(1);
    }

    @FXML
    private void showDailyWorkPanel(ActionEvent e) {
    	setPanel(Main.dailyWorkPanel, "打开日常工作面板");
    	Button btn = (Button)e.getSource();
    	if(defaultBtn != null) {
    		defaultBtn.setDefaultButton(false);
    	}
    	btn.setDefaultButton(true);
    	defaultBtn = btn;
    	DailyWorkController.getInstance().init();
    }

    @FXML
    private void showMyToolsPanel(ActionEvent e) {
    	setPanel(Main.dailyWorkPanel, "打开常用工具面板");
    	Button btn = (Button)e.getSource();
    	if(defaultBtn != null) {
    		defaultBtn.setDefaultButton(false);
    	}
    	btn.setDefaultButton(true);
    	defaultBtn = btn;
    	DailyWorkController.getInstance().init();
    }

    public void showAnimation() {
        new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(menupane.translateXProperty(), -200), new KeyValue(titlepane.opacityProperty(), 0)),
                new KeyFrame(Duration.seconds(0.3), new KeyValue(menupane.translateXProperty(), -200), new KeyValue(titlepane.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(0.6), new KeyValue(menupane.translateXProperty(), 0), new KeyValue(titlepane.opacityProperty(), 1))).play();
    }


    public void setPanel(final AnchorPane panel, String name) {
        if (panel == null) {
            return;
        }
        if (central_panel.getChildren().size() > 0 && central_panel.getChildren().get(0) == panel) {
            return;
        }
        showMessage("正在" + name + "...");
        EventHandler<ActionEvent> eh = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    central_panel.getChildren().clear();
                    central_panel.getChildren().add(panel);
                    menupane.toFront();
                    titlepane.toFront();
                } catch (Exception e) {
                    showErrorMessage(e.getLocalizedMessage());
                }
            }
        };
        if (central_panel.getChildren().size() > 0) {
            if (central_panel.getChildren().get(0) == panel) {
                return;
            }
            new Timeline(
                    new KeyFrame(Duration.seconds(0.15), new KeyValue(central_panel.translateXProperty(), -600),
                    new KeyValue(central_panel.opacityProperty(), 0)), new KeyFrame(Duration.seconds(0.16), eh),
                    new KeyFrame(Duration.seconds(0.3), new KeyValue(central_panel.translateXProperty(), 0),
                    new KeyValue(central_panel.opacityProperty(), 1))).play();
        } else {
            new Timeline(
                    new KeyFrame(Duration.seconds(0.45), new KeyValue(central_panel.opacityProperty(), 0)),
                    new KeyFrame(Duration.seconds(0.46), eh),
                    new KeyFrame(Duration.seconds(0.6), new KeyValue(central_panel.opacityProperty(), 1))).play();
        }
    }

    public void initPanel() {
        final AnchorPane panel = Main.alertEventPanel;
        showMessage("正在初始化面板...");

        try {
            defaultBtn = btn_alert_event;
            btn_alert_event.setDefaultButton(true);
        	central_panel.getChildren().clear();
            central_panel.getChildren().add(panel);
            menupane.toFront();
            titlepane.toFront();
        } catch (Exception e) {
            showErrorMessage(e.getLocalizedMessage());
        }
    }
}
