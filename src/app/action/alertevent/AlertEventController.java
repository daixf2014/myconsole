package app.action.alertevent;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import app.action.TableController;
import app.bean.DictionaryItem;
import app.service.AlertEventService;
import app.util.AlertHelper;
import app.util.StringHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * 事件提醒
 * @author daixf
 */
public class AlertEventController extends TableController implements Initializable {

    private AlertEventService alertEventService = new AlertEventService();

    private static AlertEventController instance;

    public static AlertEventController getInstance() {
        return instance;
    }

    @FXML
    private AnchorPane table_panel, loading_panel;

    @FXML
    private TextField tf_alert_content;
	@FXML
	private ChoiceBox<String> sel_alert_frequency;//提醒频率
	@FXML
	private ChoiceBox<DictionaryItem> sel_status;
	@FXML
	private TableColumn<Map<String, Object>, String> tc_alert_frequency;
	@FXML
	private TableColumn<Map<String, Object>, String> tc_alert_date;
	@FXML
	private TableColumn<Map<String, Object>, String> tc_alert_time;
	@FXML
	private TableColumn<Map<String, Object>, String> tc_remaining_time;
	@FXML
	private TableColumn<Map<String, Object>, String> tc_pre_hours;
	@FXML
	private TableColumn<Map<String, Object>, String> tc_alert_content;
	@FXML
	private TableColumn<Map<String, Object>, String> tc_status_name;
	@FXML
	private TableColumn<Map<String, Object>, String> tc_del;

	private static final String DIC_ALERT_STATUS = "CONSOLE.ALERT_STATUS";
	private static List<DictionaryItem> dictionaryItemList = null;

    /**
     * 初始化
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        logger.debug("初始化事件提醒！");
        dictionaryItemList = alertEventService.getDictionaryItemList(DIC_ALERT_STATUS);

        sel_alert_frequency.setItems(FXCollections.observableArrayList("请选择...", "仅一次", "每一天", "每一周", "每一月"));
        sel_status.setItems(FXCollections.observableArrayList(dictionaryItemList));

        sel_status.setConverter(new StringConverter<DictionaryItem>() {
        	@Override
        	public String toString(DictionaryItem object) {
        		return object.getItemName();
        	}

        	@Override
        	public DictionaryItem fromString(String string) {
        		return alertEventService.getDictionaryItem(dictionaryItemList, string);
        	}
        });

        if(null == sel_alert_frequency.getValue()) {
        	sel_alert_frequency.setValue("请选择...");
        }
        if(null == sel_status.getValue()) {
        	sel_status.setValue(alertEventService.getDictionaryItem(dictionaryItemList, "未过期"));
        }

//        tc_num.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map<String, Object>, String>, ObservableValue<String>>() {
//        	@Override
//        	public ObservableValue<String> call(CellDataFeatures<Map<String, Object>, String> arg0) {
//        		return new SimpleStringProperty(arg0.getValue().getFirstName());
//        	    }
//        	});
        tc_num.setCellValueFactory(cellData -> new SimpleStringProperty(StringHelper.get(cellData.getValue(), "row_num")));
    	tc_alert_frequency.setCellValueFactory(cellData -> new SimpleStringProperty(StringHelper.get(cellData.getValue(), "alert_frequency")));
        tc_alert_date.setCellValueFactory(cellData -> new SimpleStringProperty(StringHelper.get(cellData.getValue(), "alert_date")));
        tc_alert_time.setCellValueFactory(cellData -> new SimpleStringProperty(StringHelper.get(cellData.getValue(), "alert_time")));
        tc_alert_content.setCellValueFactory(cellData -> new SimpleStringProperty(StringHelper.get(cellData.getValue(), "alert_content")));
        tc_pre_hours.setCellValueFactory(cellData -> new SimpleStringProperty(StringHelper.get(cellData.getValue(), "pre_hours")));
        tc_status_name.setCellValueFactory(cellData -> new SimpleStringProperty(StringHelper.get(cellData.getValue(), "status_name")));

        tc_del.setCellFactory((cellData) -> {
            TableCell<Map<String, Object>, String> cell = new TableCell<Map<String, Object>, String>() {

                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    this.setText(null);
                    this.setGraphic(null);

                    if (!empty) {
                        Button delBtn = new Button("删除");
                        delBtn.setStyle("-fx-background-radius: 8, 8, 7, 6;-fx-padding: 3px 10px 3px 10px;");
                        this.setGraphic(delBtn);
                        delBtn.setOnMouseClicked((me) -> {
                        	Map<String, Object> clickedRow = this.getTableView().getItems().get(this.getIndex());
                        	if(AlertHelper.showConfirmAlert("确定删除【 " + StringHelper.get(clickedRow, "alert_content") +"】的记录？")) {
                        		int res = alertEventService.deleteAlertEvent(StringHelper.get(clickedRow, "a_id"));
                            	if(res == 1) {
                            		AlertHelper.showInfoAlert("删除成功！");
                            		int currentPage = Integer.parseInt(lb_current_page.getText());
                                	loadTable(currentPage);
                            	}
                        	}

                        });
                    }
                }
            };
            return cell;
        });

        loadTable(1);
    }

    /**
     * 加载表格
     * @param curentPage
     */
    public void loadTable(int curentPage) {
    	loading_panel.setVisible(true);
    	table_panel.setVisible(false);

    	String alertContent = tf_alert_content.getText().trim();
    	String alertFrequency = sel_alert_frequency.getValue();
    	DictionaryItem status = sel_status.getValue();

    	//查询语句及查询字段
    	StringBuffer sql = new StringBuffer("select a.a_id, a.alert_frequency, a.alert_date, a.alert_time, a.is_pre_alert, a.pre_hours, a.alert_content, a.status, "
    			+ "if(a.is_pre_alert=1,'是','否') is_pre_alert_name, fn_getdicitemname('CONSOLE.ALERT_STATUS', a.status) status_name from t_alert_event a where 1 = 1 ");
    	List<String> paramList = new ArrayList<String>();
    	if(!"".equals(alertContent)) {
    		sql.append(" and a.alert_content like ? ");
    		paramList.add("%"+alertContent+"%");
    	}
    	if(!"".equals(status.getItemValue())) {
    		sql.append(" and a.status = ? ");
    		paramList.add(status.getItemValue());
    	}

    	if(!"请选择...".equals(alertFrequency)) {
    		sql.append(" and a.alert_frequency = ? ");
    		paramList.add(alertFrequency);
    	}

    	sql.append(" order by a.a_id");

    	//加载表格
    	tv_table.getItems().clear();
        List<Map<String, Object>> dataList = alertEventService.getDBHelper().getForList(instance, sql.toString(), paramList, 12, curentPage);

        tv_table.setItems(FXCollections.observableArrayList(dataList));

//    	try {
//    		Thread.sleep(3000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        loading_panel.setVisible(false);
		table_panel.setVisible(true);
    }

    @FXML
    private void lookUpFun(MouseEvent e) {
    	tf_jump_page.setText("");
        loadTable(1);
    }

    @FXML
    private void jumpPageFun(ActionEvent e) {
    	loadTable(Integer.parseInt(tf_jump_page.getText()));
    }


    @FXML
    private void prevPageFun(ActionEvent e) {
    	tf_jump_page.setText("");
    	int currentPage = Integer.parseInt(lb_current_page.getText());
    	loadTable(currentPage-1);
    }

    @FXML
    private void nextPageFun(ActionEvent e) {
    	tf_jump_page.setText("");
    	int currentPage = Integer.parseInt(lb_current_page.getText());
    	loadTable(currentPage+1);
    }

    @FXML
    private void openAddEventWin(ActionEvent e) {
    	try {
			Stage stage = new Stage();
			Parent root = FXMLLoader.load(getClass().getResource("/view/EditAlertEvent.fxml"));

			stage.setScene(new Scene(root));
			stage.setTitle("添加事件");
			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(((Node)e.getSource()).getScene().getWindow() );
			stage.show();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
    }
}