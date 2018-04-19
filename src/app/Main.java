package app;

import app.action.MainPageController;
import app.util.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application {
    private static Main instance;
    public static DatabaseManager dbManager;
    public static AnchorPane mainPage,alertEventPanel,dailyWorkPanel;

    public static Main getInstance(){
        return instance;
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
    	instance = this;
        mainPage = FXMLLoader.load(getClass().getResource("/view/MainPage.fxml"));
        alertEventPanel = FXMLLoader.load(getClass().getResource("/view/AlertEventFrame.fxml"));//事件提醒
        dailyWorkPanel = FXMLLoader.load(getClass().getResource("/view/dailywork/DailyWorkPanel.fxml"));//日常工作

        stage.setTitle("我的工作台  V1.0 | Copyright by daixf 2018");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/res/image/icon.png")));
        stage.setMinHeight(520);
        stage.setMinWidth(650);
        stage.setScene(new Scene(mainPage));

        MainPageController.getInstance().showAnimation();
        MainPageController.getInstance().initPanel();

        stage.show();
    }
}
