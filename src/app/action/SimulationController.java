package app.action;

import java.net.URL;
import java.util.ResourceBundle;

import app.util.common.DateHelper;
import javafx.beans.binding.When;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

/**
 * FXML Controller class
 *
 * @author
 */
public class SimulationController implements Initializable {

    @FXML
    private Button abortbutton;

    private volatile Service<String> backgroundThread;
    @FXML
    private TextArea consoleLogscreen;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void addToRunMsg(String runMsg) {
    	consoleLogscreen.setText(consoleLogscreen.getText()+"\r\n"+DateHelper.getToday("yyyy-MM-dd HH:mm:ss")+"  "+runMsg);
    }

    @FXML
    private void onAbortClicked(ActionEvent event) {
        if (event.getSource() == abortbutton) {
        	backgroundThread = new Service<String>() {
                @Override
                protected Task<String> createTask() {
                    return new Task<String>() {
                        StringBuilder results = new StringBuilder();
                        @Override
                        protected String call() throws Exception {
                        	for(int i=0;i<10;i++) {
                        		String runMsg = DateHelper.getToday("yyyy-MM-dd HH:mm:ss")+"  开始从FTP复制文件！"+i+"\r\n";
                        		results.append(runMsg);
                        		updateValue(results.toString());
                        		Thread.sleep(100);
            		    	}
                            return results.toString();
                        }
                    };
                }
            };
            consoleLogscreen.textProperty().bind(backgroundThread.valueProperty());
            backgroundThread.start();
        }
    }
}