package app.action;

import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * 表格视图公共Controller
 */
public class TableController extends BaseController {

    @FXML
    public TextField tf_jump_page;
    @FXML
    public Hyperlink hl_prev_page, hl_next_page;
    @FXML
    public Label lb_current_page, lb_total_page, lb_total_size;

    @FXML
    public TableView<Map<String, Object>> tv_table;
    @FXML
    public TableColumn<Map<String, Object>, String> tc_num;
}
