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
 * �ճ�����
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
     * ��ʼ��
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        logger.debug("��ʼ���ճ�������");

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
                    	append("��ʼͬ��IT�������������ĵ���");
                    	String newMonth = sel_month.getValue();//�����·�Ŀ¼
                    	String folderPath = tf_save_path.getText()+"/";
                    	String remotePath = "/qdyydata01/rytask/";

                    	FtpUtil ftp = new FtpUtil();
                        if(ftp.connectServer("10.33.201.163", 21, "weblogic", "Rd4oJm8z", "/", "UTF-8")) {
                            //����ģ���ļ���
                    		append("��������ģ���ļ���......");
                    		append("������������������������������������������������������������");
                    		downLoadDirectory(ftp, folderPath, remotePath+"template", newMonth);
                            File file = new File(folderPath+"template");
                            file.renameTo(new File(folderPath+newMonth));
                    		append("������������������������������������������������������������\r\n");

                            //�ؼ�ҵ����
                    		append("����ͬ�����ؼ�ҵ���顰�ĵ�......");
                    		append("������������������������������������������������������������");
                            downLoadDirectory2(ftp, folderPath+newMonth+"/�ؼ�ҵ����/", remotePath+"corebusicheck/"+newMonth);
                    		append("������������������������������������������������������������\r\n");

                            //ֵ�ࣨ���š��绰���ʼ���ʽ��
                    		append("����ͬ����ֵ�ࣨ���š��绰���ʼ���ʽ�����ĵ�......");
                    		append("������������������������������������������������������������");
                            downLoadDirectory2(ftp, folderPath+newMonth+"/ֵ�ࣨ���š��绰���ʼ���ʽ��/", remotePath+"onwatch/"+newMonth);
                    		append("������������������������������������������������������������\r\n");

                            //ϵͳѲ��
                    		append("����ͬ����ϵͳѲ�조�ĵ�......");
                    		append("������������������������������������������������������������");
                            downLoadDirectory2(ftp, folderPath+newMonth+"/ϵͳѲ��/", remotePath+"qdyycheck/"+newMonth);
                    		append("������������������������������������������������������������\r\n");

                            //�ܱ��±�
                    		append("����ͬ�����ܱ��±����ĵ�......");
                    		append("������������������������������������������������������������");
                            String pattern = "^" + newMonth + "[0-9][0-9]$";
                            List<String> folderList = ftp.getFolderList(remotePath+"qdyyrpt/", pattern);
                            for(String folderName : folderList) {
                                downLoadDirectory(ftp, folderPath+newMonth+"/�ܱ��±�/", remotePath+"qdyyrpt/"+folderName, newMonth);
                            }
                    		append("������������������������������������������������������������\r\n");

                            //���ߺ����
                    		append("����ͬ�������ߺ���١��ĵ�......");
                    		append("������������������������������������������������������������");
                            downLoadDirectory2(ftp, folderPath+newMonth+"/���ߺ����/", remotePath+"syssxgz/"+newMonth);
                    		append("������������������������������������������������������������\r\n");

                            //Ӧ��������
                    		append("����ͬ����Ӧ�����������ĵ�......");
                    		append("������������������������������������������������������������");
                            downLoadDirectory2(ftp, folderPath+newMonth+"/Ӧ��������/", remotePath+"sysyybg/"+newMonth);
                    		append("������������������������������������������������������������\r\n");

                        }
                        ftp.disconnectServer();
                		append("ͬ��������ϣ�");

                        return results.toString();
                    }

                    /**
                     * �����ļ���
                     * @param localDirectoryPath���ص�ַ
                     * @param remoteDirectory Զ���ļ���
                     */
                    public boolean downLoadDirectory(FtpUtil ftp, String localDirectoryPath, String remoteDirectory, String newMonth) {
                        try {
                            String fileName = new File(remoteDirectory).getName();//Զ��Ŀ¼�ļ�������
                            localDirectoryPath = localDirectoryPath + fileName + "/";
                            FTPFile[] allFiles = ftp.ftpClient.listFiles(remoteDirectory);
                            if(allFiles != null && allFiles.length > 0) { //������ļ�
                                new File(localDirectoryPath).mkdirs();
                                // ����FTP�������ϵ��ļ����������ص�����
                                for(FTPFile file : allFiles) {
                                    logger.debug("��ʼ�����ļ�:��" + file.getName() + "��");
                                    append("��ʼ�����ļ�:��" + file.getName() + "��");

                                    if(!file.isDirectory()) {//������ļ���������
                                        String newFileName = file.getName().replaceAll("201608", newMonth);
                                        ftp.downloadFile(file.getName(), remoteDirectory, newFileName, localDirectoryPath);
                                    }
                                    else { //������ļ��У����������ļ��У��������ļ�
                                        String remoteDirectoryPath = remoteDirectory + "/" + file.getName();
                                        downLoadDirectory(ftp, localDirectoryPath, remoteDirectoryPath, newMonth); //�ݹ����
                                    }
                                }
                            }
                            else {//����Ŀ¼��û���ļ�
                                new File(localDirectoryPath).mkdirs();
                                return false;
                            }
                        }
                        catch (IOException e) {
                            logger.error("�����ļ���ʧ�ܣ�");
                            e.printStackTrace();
                            return false;
                        }
                        return true;
                    }

                    /**
                     * �����ļ���
                     * @param localDirectoryPath���ص�ַ
                     * @param remoteDirectory Զ���ļ���
                     */
                    public boolean downLoadDirectory2(FtpUtil ftp, String localDirectoryPath, String remoteDirectory) {
                        try {
                            FTPFile[] allFiles = ftp.ftpClient.listFiles(remoteDirectory);
                            if(allFiles != null && allFiles.length > 0) { //������ļ�
                                // ����FTP�������ϵ��ļ����������ص�����
                                for(FTPFile file : allFiles) {
                                    append("��ʼ�����ļ�:��" + file.getName() + "��");
                                    if(!file.isDirectory()) {//������ļ���������
                                        ftp.downloadFile(file.getName(), remoteDirectory, localDirectoryPath);
                                    }
                                }
                            }
                            else {//����Ŀ¼��û���ļ�
                            	append("δ�ҵ��κ��ĵ�������");
                            	return false;
                            }
                        }
                        catch (IOException e) {
                            logger.error("�����ļ���ʧ�ܣ�");
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
		directoryChooser.setTitle("��ѡ���ļ�");

		String defaultPath = tf_save_path.getText();
		if(defaultPath == null || "".equals(defaultPath)) {
			defaultPath = "E:/SVNĿ¼/103_ITҵ����/IT������������/2018���/";
			defaultPath = "C:/Users/Administrator/Desktop/�½��ļ���";
		}
		directoryChooser.setInitialDirectory(new File(defaultPath));

		// Show open file dialog
		File file = directoryChooser.showDialog(null);
		if(file != null) {
			tf_save_path.setText(FileHelper.getFilePath(file));
		}
    }
}