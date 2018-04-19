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
 * SFTP服务器文件上传下载工具类
 * @author daixf
 * @date 2016-07-01
 */
public class SftpUtil implements FtpHelper {
    public final Logger logger = Logger.getLogger(this.getClass());
    private ChannelSftp sftpClient;

    /**
     * 连接FTP服务器
     * @param serverIp FTP服务器的IP地址
     * @param port FTP服务器端口号
     * @param user 登录FTP服务器的用户名
     * @param password 登录FTP服务器的用户名的口令
     * @param path 访问FTP服务器的文件路径
     */
    public boolean connectServer(String serverIp, int port, String user, String password,
            String path) {
        return this.connectServer(serverIp, port, user, password, path, "GBK");
    }

    /**
     * 连接sftp服务器
     * @param host 主机
     * @param port 端口
     * @param username 用户名
     * @param password 密码
     * @param path 访问FTP服务器的文件路径
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
                //连接服务器，采用默认端口
                sftpSession = jsch.getSession(user, serverIp);
            } else {
                //采用指定的端口连接服务器
                sftpSession = jsch.getSession(user, serverIp, port);
            }

            //如果服务器连接不上
            if(sftpSession == null) {
                logger.debug("SFTP连接失败！");
                return false;
            }

            //设置登陆主机的密码
            sftpSession.setPassword(password);//设置密码
            //设置第一次登陆的时候提示，可选值：(ask | yes | no)
            sftpSession.setConfig("StrictHostKeyChecking", "no");
            //设置登陆超时时间
            sftpSession.connect(30000);
            //创建sftp通信通道
            channel = (Channel) sftpSession.openChannel("sftp");
            channel.connect(10000);
            sftpClient = (ChannelSftp) channel;
            sftpClient.setFilenameEncoding(encoding);
            //进入服务器指定的文件夹
            sftpClient.cd(path);
            logger.debug("SFTP连接成功！");
            isConn = true;

        } catch (Exception e) {
        	isConn = false;
            logger.debug("SFTP连接失败！");
        }
        return isConn;
    }

    /**
     * 断开与sftp服务器的链接
     */
    public void disconnectServer() {
        try {
            if(this.sftpClient != null) {
            	this.sftpClient.getSession().disconnect();
                this.sftpClient.disconnect();
                this.sftpClient.exit();
                logger.debug("sftp已断开连接！！");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查sftp是否连接
     */
    public boolean isConnected() {
        if(this.sftpClient != null) {
            return this.sftpClient.isConnected();
        }
        return false;
    }

    /**
     * 检查文件夹是否已存在
     * @param remotePath ftp目录，以“/”为后缀
     * @param folderName 文件夹名称
     * @return 若该文件夹已存在，则返回true；若不存在，返回false
     */
    public boolean checkFolderExists(String remotePath, String folderName) {
    	try {
			sftpClient.cd(remotePath+folderName);
			return true;
		}
		catch (Exception ex) {
			logger.debug("文件夹不存在！");
		}
		return false;
    }

    /**
     * 在FTP服务器上创建文件夹
     * @param remotePath ftp目录，以“/”为后缀
     * @param folderName 文件夹名称
     * @return 若该文件夹已存在，则返回true；若不存在，创建成功则返回true，失败则返回false
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
     * 上传文件
     * @param localFilePath 本地文件全路径（包含文件名）
     * @param remotePath    服务器远程目录
     * @param newFileName   上传后文件名称
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
        	logger.debug("文件【"+newFileName+"】上传失败！"+e.getMessage());
        }
        return uploadResult;
    }

    /**
     * 下载文件
     * @param remoteFileName 待下载文件名称
     * @param remotePath 	  待下载文件所在的远程路径
     * @param localPath 	  本地存放路径
     */
    public boolean downloadFile(String remoteFileName, String remotePath, String localPath) {
        return this.downloadFile(remoteFileName, remotePath, "", localPath);
    }

    /**
     * 下载文件
     * @param remoteFileName 待下载文件名称
     * @param remotePath 	  待下载文件所在的远程路径
     * @param newFileName    下载到本地的新文件的名称，若为空，则取原文件名
     * @param localPath 	  本地存放路径
     */
    public boolean downloadFile(String remoteFileName, String remotePath, String newFileName, String localPath) {
        boolean downloadResult = false;
        try {
            //判断下载文件是否重新起名字，若没有默认为sftp下载的文件名相同
            if("".equals(newFileName)) {
                newFileName = remoteFileName;
            }

            //文件路径
            String newFilePath = !"".equals(localPath) ? localPath + newFileName : newFileName;

            //判断存放本地文件目录是否存在，不存在时创建文件夹
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
            logger.error(remoteFileName + "下载失败！");
            e.printStackTrace();
        }
        return downloadResult;
    }

    /**
     * 删除文件
     * @param remotePath 文件所在目录
     * @param fileName 	   文件名称
     */
    public boolean deleteFile(String remotePath, String fileName) {
    	boolean result = false;
        try {
            sftpClient.cd(remotePath);
            sftpClient.rm(fileName);
            result = true;
        }
        catch (Exception e) {
        	logger.debug("文件删除失败:【" + fileName + "】");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 删除文件
     * @param fileName 	   文件名称
     */
    public boolean deleteFile(String fileName) {
    	boolean result = false;
        try {
            sftpClient.rm(fileName);
            result = true;
        }
        catch (Exception e) {
        	logger.debug("文件删除失败:【" + fileName + "】");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 取得SFTP服务器中某个目录下的所有文件列表的信息
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
     * 取得SFTP服务器某个目录下的符合条件的所有文件名
     * @param remotePath FTP服务器的路径
     * @param pattern    用于匹配文件名的正则表达式
     * @return List<String> 包含文件的名称fileName
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
     * FTP文件过滤
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
     * 解压GZ类型的压缩文件
     * @param localDir
     * @param fileName
     * @param toFileName
     */
    public void uncompressionGzFile(String localDir, String fileName, String toFileName) {
        try {
            // 本地的待解压的文件
            String gzFileName = localDir + fileName;
            GZIPInputStream gzi = new GZIPInputStream(new FileInputStream(gzFileName));
            if("".equals(toFileName)) {
                toFileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
            // 解压后数据文件名称
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