package app.util.ftp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * SFTP�������ļ��ϴ����ع�����
 * @author daixf
 * @date 2016-07-01
 */
public class SftpUtil implements FtpHelper {
    public final Logger logger = Logger.getLogger(this.getClass());
    private ChannelSftp sftpClient;

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
     * ����sftp������
     * @param host ����
     * @param port �˿�
     * @param username �û���
     * @param password ����
     * @param path ����FTP���������ļ�·��
     * @return
     */
    public boolean connectServer(String serverIp, int port, String user, String password,
    		String path, String encoding) {
    	boolean isConn = false;
        sftpClient = new ChannelSftp();
        Session sftpSession = null;
        Channel channel = null;
        try {
            JSch jsch = new JSch();
            if(port <= 0) {
                //���ӷ�����������Ĭ�϶˿�
                sftpSession = jsch.getSession(user, serverIp);
            } else {
                //����ָ���Ķ˿����ӷ�����
                sftpSession = jsch.getSession(user, serverIp, port);
            }

            //������������Ӳ���
            if(sftpSession == null) {
                logger.debug("SFTP����ʧ�ܣ�");
                return false;
            }

            //���õ�½����������
            sftpSession.setPassword(password);//��������
            //���õ�һ�ε�½��ʱ����ʾ����ѡֵ��(ask | yes | no)
            sftpSession.setConfig("StrictHostKeyChecking", "no");
            //���õ�½��ʱʱ��
            sftpSession.connect(30000);
            //����sftpͨ��ͨ��
            channel = (Channel) sftpSession.openChannel("sftp");
            channel.connect(10000);
            sftpClient = (ChannelSftp) channel;
            sftpClient.setFilenameEncoding(encoding);
            //���������ָ�����ļ���
            sftpClient.cd(path);
            logger.debug("SFTP���ӳɹ���");
            isConn = true;

        } catch (Exception e) {
        	isConn = false;
            logger.debug("SFTP����ʧ�ܣ�");
        }
        return isConn;
    }

    /**
     * �Ͽ���sftp������������
     */
    public void disconnectServer() {
        try {
            if(this.sftpClient != null) {
            	this.sftpClient.getSession().disconnect();
                this.sftpClient.disconnect();
                this.sftpClient.exit();
                logger.debug("sftp�ѶϿ����ӣ���");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ���sftp�Ƿ�����
     */
    public boolean isConnected() {
        if(this.sftpClient != null) {
            return this.sftpClient.isConnected();
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
			sftpClient.cd(remotePath+folderName);
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
				sftpClient.mkdir(remotePath+folderName);
			} catch (SftpException e) {
				return false;
			}
    	}

		return true;
	}

    /**
     * �ϴ��ļ�
     * @param localFilePath �����ļ�ȫ·���������ļ�����
     * @param remotePath    ������Զ��Ŀ¼
     * @param newFileName   �ϴ����ļ�����
     */
    public boolean uploadFile(String localFilePath, String remotePath, String newFileName) {
    	boolean uploadResult = false;
        try {
            sftpClient.cd(remotePath);
            File file = new File(localFilePath);
            if(file != null && file.isFile()) {
	            sftpClient.put(new FileInputStream(file), newFileName);
	            uploadResult = true;
            }
        }
        catch (Exception e) {
        	logger.debug("�ļ���"+newFileName+"���ϴ�ʧ�ܣ�"+e.getMessage());
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
        boolean downloadResult = false;
        try {
            //�ж������ļ��Ƿ����������֣���û��Ĭ��Ϊsftp���ص��ļ�����ͬ
            if("".equals(newFileName)) {
                newFileName = remoteFileName;
            }

            //�ļ�·��
            String newFilePath = !"".equals(localPath) ? localPath + newFileName : newFileName;

            //�жϴ�ű����ļ�Ŀ¼�Ƿ���ڣ�������ʱ�����ļ���
            File dirFile = new File(newFilePath.substring(0, newFilePath.lastIndexOf("/")));
            if(!dirFile.exists()) {
            	dirFile.mkdirs();
            }

            File file = new File(newFilePath);
            sftpClient.cd(remotePath);
            sftpClient.get(remoteFileName, new FileOutputStream(file));
            downloadResult = true;
        }
        catch (Exception e) {
            downloadResult = false;
            logger.error(remoteFileName + "����ʧ�ܣ�");
            e.printStackTrace();
        }
        return downloadResult;
    }

    /**
     * ɾ���ļ�
     * @param remotePath �ļ�����Ŀ¼
     * @param fileName 	   �ļ�����
     */
    public boolean deleteFile(String remotePath, String fileName) {
    	boolean result = false;
        try {
            sftpClient.cd(remotePath);
            sftpClient.rm(fileName);
            result = true;
        }
        catch (Exception e) {
        	logger.debug("�ļ�ɾ��ʧ��:��" + fileName + "��");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * ɾ���ļ�
     * @param fileName 	   �ļ�����
     */
    public boolean deleteFile(String fileName) {
    	boolean result = false;
        try {
            sftpClient.rm(fileName);
            result = true;
        }
        catch (Exception e) {
        	logger.debug("�ļ�ɾ��ʧ��:��" + fileName + "��");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * ȡ��SFTP��������ĳ��Ŀ¼�µ������ļ��б����Ϣ
     * @param remotePath
     * @return
     */
    public List<String> getFileList(String remotePath) {
        List<String> fileList = new ArrayList<String>();
        String fileName = "";
        try {
            Vector<?> vectorFiles = sftpClient.ls(remotePath);
            for(int i = 0; i < vectorFiles.size(); i++) {
                Object obj = vectorFiles.get(i);
                if(obj instanceof com.jcraft.jsch.ChannelSftp.LsEntry) {
                    fileName = ((com.jcraft.jsch.ChannelSftp.LsEntry) obj).getFilename();
                    fileList.add(fileName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileList;
    }

    /**
     * ȡ��SFTP������ĳ��Ŀ¼�µķ��������������ļ���
     * @param remotePath FTP��������·��
     * @param pattern    ����ƥ���ļ�����������ʽ
     * @return List<String> �����ļ�������fileName
     */
	public List<String> getFileList(String remotePath, String pattern) {
        List<String> nameList = new ArrayList<String>();
        String fileName = "";
        try {
            Vector<?> vectorFiles = sftpClient.ls(remotePath);
            for(int i = 0; i < vectorFiles.size(); i++) {
                Object obj = vectorFiles.get(i);
                if(obj instanceof com.jcraft.jsch.ChannelSftp.LsEntry) {
                    fileName = ((com.jcraft.jsch.ChannelSftp.LsEntry) obj).getFilename();
                    Pattern p = Pattern.compile(pattern);
                    Matcher m = p.matcher(fileName);
                    if(m.find()){
                        nameList.add(fileName);
                    };
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nameList;
    }

    /**
     * FTP�ļ�����
     */
    public class MyFtpFileFilter implements FTPFileFilter {
        private String fileNamePattern;
        public MyFtpFileFilter(String fileNamePattern) {
            this.fileNamePattern = fileNamePattern;
        }

        public boolean accept(FTPFile ftpFile) {
            boolean result = false;
            if(ftpFile.isFile()) {
                String fileName = ftpFile.getName().toUpperCase();
                Pattern p = Pattern.compile(fileNamePattern.toUpperCase());
                Matcher m = p.matcher(fileName);
                result = m.find();
            }
            return result;
        }

        public String getFileNamePattern() {
            return fileNamePattern;
        }

        public void setFileNamePattern(String fileNamePattern) {
            this.fileNamePattern = fileNamePattern;
        }
    }


    public ChannelSftp getSftpClient() {
        return sftpClient;
    }

    public void setSftpClient(ChannelSftp sftpClient) {
        this.sftpClient = sftpClient;
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

//    public static void main(String[] args) throws IOException
//    {
//        SftpUtil ftp = new SftpUtil();
//
//        try {
//            ftp.connectServer("10.32.145.70", 22, "toptea", "toptea123", "/");
//            ftp.checkFolderExists("/bomcapp/smp/qd_smp/create/", "201607");
//
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        finally {
//            ftp.closeServer();
//        }
//    }
}