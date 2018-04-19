package app.util.ftp;

import java.util.List;

/**
 * FTP文件传输接口类
 * @author daixf
 * @date 2016-07-01
 */
public interface FtpHelper {
    /**
     * 连接FTP服务器
     * @param serverIp FTP服务器的IP地址
     * @param port FTP服务器端口号
     * @param user 登录FTP服务器的用户名
     * @param password 登录FTP服务器的用户名的口令
     * @param path 访问FTP服务器的文件路径
     */
    public boolean connectServer(String serverIp, int port, String user, String password, String path);

    /**
     * 连接FTP服务器
     * @param host 主机
     * @param port 端口
     * @param username 用户名
     * @param password 密码
     * @param path 访问FTP服务器的文件路径
     * @return
     */
    public boolean connectServer(String serverIp, int port, String user, String password, String path, String encoding);

    /**
     * 断开与FTP服务器的链接
     */
    public void disconnectServer();

    /**
     * 检查FTP是否连接
     * @return
     */
    public boolean isConnected();

    /**
     * 检查文件夹是否已存在
     * @param remotePath ftp目录，以“/”为后缀
     * @param folderName 文件夹名称
     * @return 若该文件夹已存在，则返回true；若不存在，返回false
     */
    public boolean checkFolderExists(String remotePath, String folderName);

    /**
     * 在FTP服务器上创建文件夹
     * @param remotePath ftp目录，以“/”为后缀
     * @param folderName 文件夹名称
     * @return 若该文件夹已存在，则返回true；若不存在，创建成功则返回true，失败则返回false
     */
    public boolean createFtpFolder(String remotePath, String folderName);

    /**
     * 上传文件
     * @param localFilePath 本地文件全路径（包含文件名）
     * @param remotePath    服务器远程目录
     * @param newFileName   上传后文件名称
     */
    public boolean uploadFile(String localFilePath, String remotePath, String newFileName);

    /**
     * 下载文件
     * @param remoteFileName 待下载文件名称
     * @param remotePath 	  待下载文件所在的远程路径
     * @param localPath 	  本地存放路径
     */
    public boolean downloadFile(String remoteFileName, String remotePath, String localPath);

    /**
     * 下载文件
     * @param remoteFileName 待下载文件名称
     * @param remotePath 	  待下载文件所在的远程路径
     * @param newFileName    下载到本地的新文件的名称，若为空，则取原文件名
     * @param localPath 	  本地存放路径
     */
    public boolean downloadFile(String remoteFileName, String remotePath, String newFileName, String localPath);

    /**
     * 删除文件
     * @param remotePath 文件所在目录
     * @param fileName 	   文件名称
     */
    public boolean deleteFile(String remotePath, String fileName);

    /**
     * 删除文件
     * @param fileName 	   文件名称
     */
    public boolean deleteFile(String fileName);

    /**
     * 取得某个远程目录下的所有文件列表的信息
     * @param remotePath
     * @return
     */
    public List<String> getFileList(String remotePath);

    /**
     * 取得SFTP服务器某个目录下的符合条件的所有文件名
     * @param remotePath FTP服务器的路径
     * @param pattern    用于匹配文件名的正则表达式
     * @return List<String> 包含文件的名称fileName
     */
	public List<String> getFileList(String remotePath, String pattern);

	/**
	 * 解压GZ类型的压缩文件
	 * @param localDir
	 * @param fileName
	 * @param toFileName
	 */
    public void uncompressionGzFile(String localDir, String fileName, String toFileName);
}