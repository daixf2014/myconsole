package app.action.dailywork;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.net.ftp.FTPFile;

import app.action.TableController;
import app.service.dailywork.DailyWorkService;
import app.util.common.DateHelper;
import app.util.common.FileHelper;
import app.util.ftp.FtpUtil;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;

/**
 * 日常工作
 * @author daixf
 */
public class DailyWorkController extends TableController implements Initializable {

    private DailyWorkService dailyWorkService = new DailyWorkService();

    private static DailyWorkController instance;

    public static DailyWorkController getInstance() {
        return instance;
    }

    @FXML
    private TextArea run_msg;
    @FXML
    private TextField tf_save_path;
    @FXML
    private ChoiceBox<String> sel_month;

    private volatile Service<String> backgroundThread;
    private ReadOnlyObjectProperty<String> textRecu;

    /**
     * 初始化
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        logger.debug("初始化日常工作！");

        sel_month.setItems(FXCollections.observableArrayList(DateHelper.getMonthStringList(-1, -5, "yyyyMM")));
        sel_month.setValue(DateHelper.getMonth(-1, "yyyyMM"));
    }

    public void init() {

    }

    @FXML
    private void getItRyWorkFiles(ActionEvent event) {
    	backgroundThread = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    StringBuilder results = new StringBuilder();

                    @Override
                    protected String call() throws Exception {
                    	append("开始同步IT工作量后评估文档！");
                    	String newMonth = sel_month.getValue();//本地月份目录
                    	String folderPath = tf_save_path.getText()+"/";
                    	String remotePath = "/qdyydata01/rytask/";

                    	FtpUtil ftp = new FtpUtil();
                        if(ftp.connectServer("10.33.201.163", 21, "weblogic", "Rd4oJm8z", "/", "UTF-8")) {
                            //下载模板文件夹
                    		append("正在下载模板文件夹......");
                    		append("——————————————————————————————");
                    		downLoadDirectory(ftp, folderPath, remotePath+"template", newMonth);
                            File file = new File(folderPath+"template");
                            file.renameTo(new File(folderPath+newMonth));
                    		append("——————————————————————————————\r\n");

                            //关键业务检查
                    		append("正在同步”关键业务检查“文档......");
                    		append("——————————————————————————————");
                            downLoadDirectory2(ftp, folderPath+newMonth+"/关键业务检查/", remotePath+"corebusicheck/"+newMonth);
                    		append("——————————————————————————————\r\n");

                            //值班（飞信、电话、邮件方式）
                    		append("正在同步”值班（飞信、电话、邮件方式）“文档......");
                    		append("——————————————————————————————");
                            downLoadDirectory2(ftp, folderPath+newMonth+"/值班（飞信、电话、邮件方式）/", remotePath+"onwatch/"+newMonth);
                    		append("——————————————————————————————\r\n");

                            //系统巡检
                    		append("正在同步”系统巡检“文档......");
                    		append("——————————————————————————————");
                            downLoadDirectory2(ftp, folderPath+newMonth+"/系统巡检/", remotePath+"qdyycheck/"+newMonth);
                    		append("——————————————————————————————\r\n");

                            //周报月报
                    		append("正在同步”周报月报“文档......");
                    		append("——————————————————————————————");
                            String pattern = "^" + newMonth + "[0-9][0-9]$";
                            List<String> folderList = ftp.getFolderList(remotePath+"qdyyrpt/", pattern);
                            for(String folderName : folderList) {
                                downLoadDirectory(ftp, folderPath+newMonth+"/周报月报/", remotePath+"qdyyrpt/"+folderName, newMonth);
                            }
                    		append("——————————————————————————————\r\n");

                            //上线后跟踪
                    		append("正在同步”上线后跟踪“文档......");
                    		append("——————————————————————————————");
                            downLoadDirectory2(ftp, folderPath+newMonth+"/上线后跟踪/", remotePath+"syssxgz/"+newMonth);
                    		append("——————————————————————————————\r\n");

                            //应用软件变更
                    		append("正在同步”应用软件变更“文档......");
                    		append("——————————————————————————————");
                            downLoadDirectory2(ftp, folderPath+newMonth+"/应用软件变更/", remotePath+"sysyybg/"+newMonth);
                    		append("——————————————————————————————\r\n");

                        }
                        ftp.disconnectServer();
                		append("同步任务完毕！");

                        return results.toString();
                    }

                    /**
                     * 下载文件夹
                     * @param localDirectoryPath本地地址
                     * @param remoteDirectory 远程文件夹
                     */
                    public boolean downLoadDirectory(FtpUtil ftp, String localDirectoryPath, String remoteDirectory, String newMonth) {
                        try {
                            String fileName = new File(remoteDirectory).getName();//远程目录文件夹名称
                            localDirectoryPath = localDirectoryPath + fileName + "/";
                            FTPFile[] allFiles = ftp.ftpClient.listFiles(remoteDirectory);
                            if(allFiles != null && allFiles.length > 0) { //如果有文件
                                new File(localDirectoryPath).mkdirs();
                                // 遍历FTP服务器上的文件，单个下载到本地
                                for(FTPFile file : allFiles) {
                                    logger.debug("开始下载文件:【" + file.getName() + "】");
                                    append("开始下载文件:【" + file.getName() + "】");

                                    if(!file.isDirectory()) {//如果是文件，则下载
                                        String newFileName = file.getName().replaceAll("201608", newMonth);
                                        ftp.downloadFile(file.getName(), remoteDirectory, newFileName, localDirectoryPath);
                                    }
                                    else { //如果是文件夹，则先下载文件夹，再下载文件
                                        String remoteDirectoryPath = remoteDirectory + "/" + file.getName();
                                        downLoadDirectory(ftp, localDirectoryPath, remoteDirectoryPath, newMonth); //递归调用
                                    }
                                }
                            }
                            else {//若该目录下没有文件
                                new File(localDirectoryPath).mkdirs();
                                return false;
                            }
                        }
                        catch (IOException e) {
                            logger.error("下载文件夹失败！");
                            e.printStackTrace();
                            return false;
                        }
                        return true;
                    }

                    /**
                     * 下载文件夹
                     * @param localDirectoryPath本地地址
                     * @param remoteDirectory 远程文件夹
                     */
                    public boolean downLoadDirectory2(FtpUtil ftp, String localDirectoryPath, String remoteDirectory) {
                        try {
                            FTPFile[] allFiles = ftp.ftpClient.listFiles(remoteDirectory);
                            if(allFiles != null && allFiles.length > 0) { //如果有文件
                                // 遍历FTP服务器上的文件，单个下载到本地
                                for(FTPFile file : allFiles) {
                                    append("开始下载文件:【" + file.getName() + "】");
                                    if(!file.isDirectory()) {//如果是文件，则下载
                                        ftp.downloadFile(file.getName(), remoteDirectory, localDirectoryPath);
                                    }
                                }
                            }
                            else {//若该目录下没有文件
                            	append("未找到任何文档！！！");
                            	return false;
                            }
                        }
                        catch (IOException e) {
                            logger.error("下载文件夹失败！");
                            e.printStackTrace();
                            return false;
                        }
                        return true;
                    }

                    public void append(String runMsg) {
                    	results.append(DateHelper.getToday("yyyy-MM-dd HH:mm:ss")+"  "+runMsg+"\r\n");
                		updateValue(results.toString());
                    }
                };
            }
        };

        textRecu = backgroundThread.valueProperty();
        run_msg.textProperty().bind(textRecu);

        textRecu.addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
            	run_msg.selectPositionCaret(run_msg.getLength());
            	run_msg.deselect();
            	run_msg.setScrollTop(Double.MAX_VALUE);
            }
        });
        backgroundThread.start();
    }

    @FXML
    private void openDirectoryChooser(MouseEvent e) {
    	DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("请选择文件");

		String defaultPath = tf_save_path.getText();
		if(defaultPath == null || "".equals(defaultPath)) {
			defaultPath = "E:/SVN目录/103_IT业务室/IT工作量后评估/2018年度/";
			defaultPath = "C:/Users/Administrator/Desktop/新建文件夹";
		}
		directoryChooser.setInitialDirectory(new File(defaultPath));

		// Show open file dialog
		File file = directoryChooser.showDialog(null);
		if(file != null) {
			tf_save_path.setText(FileHelper.getFilePath(file));
		}
    }
}