package app.util.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import app.util.common.DateHelper;


/**
 * FTP�������ļ��ϴ����ع�����
 * @author daixf
 * @date 2016-07-04
 */
public class FtpUtil implements FtpHelper {
    public final Logger logger = Logger.getLogger(this.getClass());
    public FTPClient ftpClient;

    /**
     * ����FTP������
     * @param serverIp FTP��������IP��ַ
     * @param port FTP�������˿ں�
     * @param user ��¼FTP���������û���
     * @param password ��¼FTP���������û����Ŀ���
     * @param path ����FTP���������ļ�·��
     */
    public boolean connectServer(String serverIp, int port, String user, String password,
            String path) {
        return this.connectServer(serverIp, port, user, password, path, "GBK");
    }

    /**
     * ����FTP������
     * @param serverIp FTP��������IP��ַ
     * @param intPort FTP�������˿ں�
     * @param user ��¼FTP���������û���
     * @param password ��¼FTP���������û����Ŀ���
     * @param path ����FTP���������ļ�·��
     * @param encoding �ַ�������
     * @throws IOException
     */
    public boolean connectServer(String serverIp, int port, String user, String password,
            String path, String encoding) {
        boolean isConn = false;
        ftpClient = new FTPClient();
        this.ftpClient.setControlEncoding(encoding);    //�����ַ���
        this.ftpClient.setDataTimeout(30 * 1000);		//�������ݴ��䳬ʱʱ��
        this.ftpClient.setConnectTimeout(30 * 1000);	//�������ӳ�ʱʱ��Ϊ20��
        this.ftpClient.setBufferSize(1024 * 2);

        try {
            if(port > 0) {// ftp�˿ں�
                this.ftpClient.connect(serverIp, port);
            }
            else {//ʹ��Ĭ�ϵĶ˿ں�
                this.ftpClient.connect(serverIp);
            }

            isConn = this.ftpClient.login(user, password);
            int reply = this.ftpClient.getReplyCode();  // FTP���������ӻش�
            if(!FTPReply.isPositiveCompletion(reply)) {// �ж��������ӵķ������Ƿ�Ϊ 230
                this.ftpClient.disconnect();
                logger.error("��¼FTP����ʧ�ܣ�");
                return isConn;
            }
            if(isConn) {
                logger.info("��ϲ" + user + "�ɹ���½FTP��������");
                this.ftpClient.enterLocalPassiveMode(); //��������ģʽ��һ��Ҫ���ã�
                //���ö����ƴ���
            	this.ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            	this.ftpClient.setFileTransferMode(FTPClient.STREAM_TRANSFER_MODE);
                if(path.length() != 0) {// path��ftp��������Ŀ¼����Ŀ¼
                	this.ftpClient.changeWorkingDirectory(path);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("��¼FTP����ʧ�ܣ�" + e.getMessage());
        }
        return isConn;
    }

    /**
     * �Ͽ���ftp������������
     */
    public void disconnectServer() {
        try {
            if(this.ftpClient != null) {
                this.ftpClient.disconnect();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ���FTP�Ƿ�����
     */
    public boolean isConnected() {
        if(this.ftpClient != null) {
            return this.ftpClient.isConnected();
        }
        return false;
    }

    /**
     * ����ļ����Ƿ��Ѵ���
     * @param remotePath ftpĿ¼���ԡ�/��Ϊ��׺
     * @param folderName �ļ�������
     * @return �����ļ����Ѵ��ڣ��򷵻�true���������ڣ�����false
     */
    public boolean checkFolderExists(String remotePath, String folderName) {
    	try {
			this.ftpClient.changeWorkingDirectory(remotePath+folderName);
			return true;
		}
		catch (Exception ex) {
			logger.debug("�ļ��в����ڣ�");
		}
		return false;
    }

    /**
     * ��FTP�������ϴ����ļ���
     * @param remotePath ftpĿ¼���ԡ�/��Ϊ��׺
     * @param folderName �ļ�������
     * @return �����ļ����Ѵ��ڣ��򷵻�true���������ڣ������ɹ��򷵻�true��ʧ���򷵻�false
     */
    public boolean createFtpFolder(String remotePath, String folderName) {
    	if(!this.checkFolderExists(remotePath, folderName)) {
    		try {
    			this.ftpClient.makeDirectory(remotePath+folderName);
			} catch (Exception e) {
				return false;
			}
    	}

		return true;
	}

    /**
     * �ϴ�Ftp�ļ�
     * @param localFilePath �����ļ�ȫ·���������ļ�����
     * @param remotePath    ������Զ��Ŀ¼
     * @param newFileName   �ϴ����ļ�����
     */
    public boolean uploadFile(String localFilePath, String remotePath, String newFileName) {
        java.io.File localFile = new java.io.File(localFilePath);
        return this.uploadFile(localFile, remotePath, newFileName);
    }

    /**
     * �ϴ�Ftp�ļ�
     * @param localFile �����ļ�
     * @param String newFileName �ϴ���FTP�������ϵ����ļ���
     * @param remotePath �ϴ�������·�� - Ӧ���ԡ�/������
     * @return boolean
     */
    public boolean uploadFile(File localFile, String remotePath, String newFileName) {
        BufferedInputStream inStream = null;
        String localFileName = localFile.getName();
        boolean uploadResult = false;
        try {
            inStream = new BufferedInputStream(new FileInputStream(localFile));
            if("".equals(newFileName)) {//�������ļ�����û�д���Ĭ�����ϴ��ı����ļ�����ͬ
                newFileName = localFileName;
            }
            //logger.info(localFileName + "�ļ���ʼ�ϴ�...");
            uploadResult = this.uploadFile(inStream, remotePath, newFileName);
            if(uploadResult) {
                return uploadResult;
            }
        }
        catch (FileNotFoundException e) {
        	logger.error(localFileName + "�ļ�δ�ҵ�...");
            e.printStackTrace();
        }
        return uploadResult;
    }

    /**
     * �ϴ�Ftp�ļ�
     * @param newFileName �ϴ���FTP�������ϵ����ļ���
     * @param inStream �����ϴ��ļ���������
     * @param remotePath �ϴ�������·�� - Ӧ���ԡ�/������
     * @return boolean
     */
    public boolean uploadFile(InputStream inStream, String remotePath, String newFileName) {
        boolean uploadResult = false;
        try {
            if(!"".equals(remotePath)) {//�ж��Ƿ���Ҫָ���ϴ��ļ���·��
                this.ftpClient.changeWorkingDirectory(remotePath);  //�ı乤��·��
            }
            uploadResult = this.ftpClient.storeFile(newFileName, inStream);  //�ϴ��ļ�
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(inStream != null) {
                try {
                    inStream.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return uploadResult;
    }

    /**
     * �����ļ�
     * @param remoteFileName �������ļ�����
     * @param remotePath 	  �������ļ����ڵ�Զ��·��
     * @param localPath 	  ���ش��·��
     */
    public boolean downloadFile(String remoteFileName, String remotePath, String localPath) {
        return this.downloadFile(remoteFileName, remotePath, "", localPath);
    }

    /**
     * �����ļ�
     * @param remoteFileName �������ļ�����
     * @param remotePath 	  �������ļ����ڵ�Զ��·��
     * @param newFileName    ���ص����ص����ļ������ƣ���Ϊ�գ���ȡԭ�ļ���
     * @param localPath 	  ���ش��·��
     */
    public boolean downloadFile(String remoteFileName, String remotePath, String newFileName, String localPath) {
        BufferedOutputStream outStream = null;
        boolean downloadResult = false;
        if("".equals(newFileName)) {//�ж������ļ��Ƿ����������֣���û��Ĭ��Ϊftp���ص��ļ�����ͬ
            newFileName = remoteFileName;
        }

        //�ļ�·��
        String newFilePath = !"".equals(localPath) ? localPath + newFileName : newFileName;

        //�жϴ�ű����ļ�Ŀ¼�Ƿ���ڣ�������ʱ�����ļ���
        File dirFile = new File(newFilePath.substring(0, newFilePath.lastIndexOf("/")));
        if(!dirFile.exists()) {
        	dirFile.mkdirs();
        }

        try {
            if(!"".equals(remotePath)) {//�ж�ftp������·���Ƿ����
                this.ftpClient.changeWorkingDirectory(remotePath);
            }
            outStream = new BufferedOutputStream(new FileOutputStream(newFilePath));
            downloadResult = this.ftpClient.retrieveFile(remoteFileName, outStream);
        }
        catch (Exception e) {
        	downloadResult = false;
            e.printStackTrace();
        }
        finally {
            if(outStream != null) {
                try {
                    outStream.flush();
                    outStream.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return downloadResult;
    }

    /**
     * ���ض���ļ�
     * @param list �����ص��ļ�list����
     * @param remoteDownLoadPath remoteFileName ���ڵ�·��
     * @param localDir ���ص������Ǹ�·����
     */
    public void downloadFiles(List<String> list, String remoteDownLoadPath, String localDir) {
        if(list != null && list.size() > 0) {
            for(String fileName : list) {
                downloadFile(fileName, remoteDownLoadPath, "", localDir);
            }
        }
    }

    /**
     * ɾ���ļ�
     * @param remotePath �ļ�����Ŀ¼
     * @param fileName 	   �ļ�����
     */
    public boolean deleteFile(String remotePath, String fileName) {
    	boolean result = false;
        try {
        	if(ftpClient.changeWorkingDirectory(remotePath)) {
        		result = this.ftpClient.deleteFile(fileName);
        	}
        }
        catch (Exception e) {
        	logger.debug("�ļ�ɾ��ʧ��:��" + fileName + "��");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * ɾ��Զ��FTP�������ϵ��ļ�
     * @param �ļ�����
     */
    public boolean deleteFile(String fileName) {
    	boolean result = false;
    	try {
    		result = this.ftpClient.deleteFile(fileName);
		}
    	catch (IOException e) {
    		logger.error("ɾ��Զ��FTP�������ϵ�"+fileName+"�ļ�ʧ�ܣ�");
			e.printStackTrace();
		}
    	return result;
    }

    /**
     * ȡ��FTP��������ĳ��Ŀ¼�µ������ļ��б����Ϣ
     * @param remotePath FTP��������·��
     * @return List �����ļ�������fileName �� �ļ�����ʱ�� createTime String���͵�
     */
    public List<String> getFileList(String remotePath) {
        List<String> fileList = new ArrayList<String>();
        try {
            FTPListParseEngine engine = this.ftpClient.initiateListParsing(remotePath);
            FTPFile[] files = engine.getFiles();
            logger.debug("files.length ===> "+files.length);
            for(int i=0; i<files.length; i++) {
                if(files[i].isFile()) {//�ж��Ƿ�Ϊ�ļ�
                    fileList.add(files[i].getName());
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return fileList;
    }

    /**
     * ȡ��FTP������ĳ��Ŀ¼�µķ��������������ļ���
     * @param remotePath FTP��������·��
     * @param pattern ����ƥ���ļ�����������ʽ
     * @return List<String> �����ļ�������fileName
     */
    public List<String> getFileList(String remotePath, String pattern) {
        List<String> nameList = new ArrayList<String>();
        try {
            FTPFile[] files = ftpClient.listFiles(remotePath, new MyFtpFileFilter(pattern));
            for(int i=0; i<files.length; i++) {
            	if(files[i].isFile()) {//�ж��Ƿ�Ϊ�ļ�
            		nameList.add(files[i].getName());
            	}
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return nameList;
    }

    /**
     * ȡ��FTP������ĳ��Ŀ¼�µķ��������������ļ���
     * @param remotePath FTP��������·��
     * @param pattern ����ƥ���ļ�����������ʽ
     * @return List<String> �����ļ�������fileName
     */
    public List<String> getFolderList(String remotePath, String pattern) {
        List<String> nameList = new ArrayList<String>();
        try {
            FTPFile[] files = ftpClient.listFiles(remotePath, new MyFtpFileFilter(pattern));
            for(int i=0; i<files.length; i++) {
                nameList.add(files[i].getName());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return nameList;
    }

    public boolean matchFile(FTPFile ftpFile, String pattern) {
        boolean result = false;
        if(ftpFile.isFile()) {
            String fileName = ftpFile.getName().toUpperCase();
            Pattern p = Pattern.compile(pattern.toUpperCase());
            Matcher m = p.matcher(fileName);
            result = m.find();
        }
        return result;
    }

    /**
     * ��ѹGZ���͵�ѹ���ļ�
     * @param localDir
     * @param fileName
     * @param toFileName
     */
    public void uncompressionGzFile(String localDir, String fileName, String toFileName) {
        try {
            // ���صĴ���ѹ���ļ�
            String gzFileName = localDir + fileName;
            GZIPInputStream gzi = new GZIPInputStream(new FileInputStream(gzFileName));
            if("".equals(toFileName)) {
                toFileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
            // ��ѹ�������ļ�����
            toFileName = localDir + toFileName;
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(toFileName));
            int b;
            do {
                b = gzi.read();
                if(b == -1)
                    break;
                bos.write(b);
            }
            while(true);
            gzi.close();
            bos.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ��ѹZIP�ļ�
     * @param localDir
     * @param fileName
     * @param toFileName
     */
    public void unZipFile(String zipFile, String toFileDir) {
        try {
            ZipInputStream Zin = new ZipInputStream(new FileInputStream(zipFile));
            BufferedInputStream bis = new BufferedInputStream(Zin);
            File file = null;
            ZipEntry entry;
            while((entry = Zin.getNextEntry()) != null && !entry.isDirectory()) {
                file = new File(toFileDir, entry.getName());
                if(!file.exists()) {
                    (new File(file.getParent())).mkdirs();
                }
                FileOutputStream out = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(out);
                int n;
                while((n = bis.read()) != -1) {
                    bos.write(n);
                }
                bos.flush();
                bos.close();
                out.close();
                logger.debug("��" + file.getName() + "����ѹ�ɹ�");
            }
            bis.close();
            Zin.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ��FTP������������XML�ļ�
     * @author ������ 2013-09-17
     * @throws java.lang.Exception
     * @param document Document����
     */
    public void upload(Document document) throws Exception {
        OutputStream os = null;
        try {
            String xmlFileName = "log" + DateHelper.getToday("yyyyMMddhhmmss") + ".xml";
            // ��ʼ��Documentӳ�䵽�ļ�
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transFormer = transFactory.newTransformer();
            // ����������
            DOMSource domSource = new DOMSource(document);
            // ���������
            os = ftpClient.storeFileStream("log/" + xmlFileName);
            StreamResult xmlResult = new StreamResult(os);
            // ���xml�ļ�
            transFormer.transform(domSource, xmlResult);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(os != null) {
                os.close();
            }
        }
    }

    /**
     * �ϴ��ļ���
     * @param localDirectory �����ļ���
     * @param remoteDirectoryPath Ftp ������·�� ��Ŀ¼"/"����
     */
    public boolean uploadDirectory(String localDirectory, String remoteDirectoryPath) {
        File src = new File(localDirectory);
        try {
            remoteDirectoryPath = remoteDirectoryPath + src.getName() + "/";
            this.ftpClient.makeDirectory(remoteDirectoryPath);
        }
        catch (IOException e) {
            e.printStackTrace();
            logger.info(remoteDirectoryPath + "Ŀ¼����ʧ�ܣ�");
        }
        File[] allFile = src.listFiles();

        // ѭ�������������е��ļ��������ļ��ϴ�
        for(int currentFile = 0; currentFile < allFile.length; currentFile++) {
            if(!allFile[currentFile].isDirectory()) {//�жϱ����ļ��Ƿ����ļ���
                String srcName = allFile[currentFile].getPath().toString();
                uploadFile(new File(srcName), remoteDirectoryPath, ""); //�ϴ��ļ�
            }
        }

        for(int currentFile = 0; currentFile < allFile.length; currentFile++) {
            if(allFile[currentFile].isDirectory()) {
                // �ݹ���ã��������а������ļ���
                uploadDirectory(allFile[currentFile].getPath().toString(), remoteDirectoryPath);
            }
        }
        return true;
    }

    /**
     * �����ļ���
     * @param localDirectoryPath���ص�ַ
     * @param remoteDirectory Զ���ļ���
     */
    public boolean downLoadDirectory(String localDirectoryPath, String remoteDirectory) {
        try {
            String fileName = new File(remoteDirectory).getName();//Զ��Ŀ¼�ļ�������
            localDirectoryPath = localDirectoryPath + fileName + "/";
            FTPFile[] allFile = this.ftpClient.listFiles(remoteDirectory);
            if(allFile.length > 0) { //������ļ�
                new File(localDirectoryPath).mkdirs();
                // ����FTP�������ϵ��ļ����������ص�����
                for(int currentFile = 0; currentFile < allFile.length; currentFile++) {
                    logger.debug("��ʼ�����ļ�:��" + allFile[currentFile].getName() + "��");
                    if(!allFile[currentFile].isDirectory()) {//������ļ���������
                        downloadFile(allFile[currentFile].getName(), remoteDirectory, localDirectoryPath);

                    }
                    else { //������ļ��У����������ļ��У��������ļ�
                        String remoteDirectoryPath = remoteDirectory + "/" + allFile[currentFile].getName();
                        downLoadDirectory(localDirectoryPath, remoteDirectoryPath); //�ݹ����
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

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    /**
     * FTP�ļ�����
     * @author ������ 2013-09-17
     */
    public class MyFtpFileFilter implements FTPFileFilter {
        private String fileNamePattern;

        public MyFtpFileFilter(String fileNamePattern) {
            this.fileNamePattern = fileNamePattern;
        }

        public boolean accept(FTPFile ftpFile) {
            boolean result = false;
            String fileName = ftpFile.getName().toUpperCase();
            Pattern p = Pattern.compile(fileNamePattern.toUpperCase());
            Matcher m = p.matcher(fileName);
            result = m.find();
            return result;
        }

        public String getFileNamePattern() {
            return fileNamePattern;
        }

        public void setFileNamePattern(String fileNamePattern) {
            this.fileNamePattern = fileNamePattern;
        }
    }
}