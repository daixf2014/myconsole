package app.util.ftp;

import java.util.List;

/**
 * FTP�ļ�����ӿ���
 * @author daixf
 * @date 2016-07-01
 */
public interface FtpHelper {
    /**
     * ����FTP������
     * @param serverIp FTP��������IP��ַ
     * @param port FTP�������˿ں�
     * @param user ��¼FTP���������û���
     * @param password ��¼FTP���������û����Ŀ���
     * @param path ����FTP���������ļ�·��
     */
    public boolean connectServer(String serverIp, int port, String user, String password, String path);

    /**
     * ����FTP������
     * @param host ����
     * @param port �˿�
     * @param username �û���
     * @param password ����
     * @param path ����FTP���������ļ�·��
     * @return
     */
    public boolean connectServer(String serverIp, int port, String user, String password, String path, String encoding);

    /**
     * �Ͽ���FTP������������
     */
    public void disconnectServer();

    /**
     * ���FTP�Ƿ�����
     * @return
     */
    public boolean isConnected();

    /**
     * ����ļ����Ƿ��Ѵ���
     * @param remotePath ftpĿ¼���ԡ�/��Ϊ��׺
     * @param folderName �ļ�������
     * @return �����ļ����Ѵ��ڣ��򷵻�true���������ڣ�����false
     */
    public boolean checkFolderExists(String remotePath, String folderName);

    /**
     * ��FTP�������ϴ����ļ���
     * @param remotePath ftpĿ¼���ԡ�/��Ϊ��׺
     * @param folderName �ļ�������
     * @return �����ļ����Ѵ��ڣ��򷵻�true���������ڣ������ɹ��򷵻�true��ʧ���򷵻�false
     */
    public boolean createFtpFolder(String remotePath, String folderName);

    /**
     * �ϴ��ļ�
     * @param localFilePath �����ļ�ȫ·���������ļ�����
     * @param remotePath    ������Զ��Ŀ¼
     * @param newFileName   �ϴ����ļ�����
     */
    public boolean uploadFile(String localFilePath, String remotePath, String newFileName);

    /**
     * �����ļ�
     * @param remoteFileName �������ļ�����
     * @param remotePath 	  �������ļ����ڵ�Զ��·��
     * @param localPath 	  ���ش��·��
     */
    public boolean downloadFile(String remoteFileName, String remotePath, String localPath);

    /**
     * �����ļ�
     * @param remoteFileName �������ļ�����
     * @param remotePath 	  �������ļ����ڵ�Զ��·��
     * @param newFileName    ���ص����ص����ļ������ƣ���Ϊ�գ���ȡԭ�ļ���
     * @param localPath 	  ���ش��·��
     */
    public boolean downloadFile(String remoteFileName, String remotePath, String newFileName, String localPath);

    /**
     * ɾ���ļ�
     * @param remotePath �ļ�����Ŀ¼
     * @param fileName 	   �ļ�����
     */
    public boolean deleteFile(String remotePath, String fileName);

    /**
     * ɾ���ļ�
     * @param fileName 	   �ļ�����
     */
    public boolean deleteFile(String fileName);

    /**
     * ȡ��ĳ��Զ��Ŀ¼�µ������ļ��б����Ϣ
     * @param remotePath
     * @return
     */
    public List<String> getFileList(String remotePath);

    /**
     * ȡ��SFTP������ĳ��Ŀ¼�µķ��������������ļ���
     * @param remotePath FTP��������·��
     * @param pattern    ����ƥ���ļ�����������ʽ
     * @return List<String> �����ļ�������fileName
     */
	public List<String> getFileList(String remotePath, String pattern);

	/**
	 * ��ѹGZ���͵�ѹ���ļ�
	 * @param localDir
	 * @param fileName
	 * @param toFileName
	 */
    public void uncompressionGzFile(String localDir, String fileName, String toFileName);
}