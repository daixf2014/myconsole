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
 * FTP服务器文件上传下载工具类
 * @author daixf
 * @date 2016-07-04
 */
public class FtpUtil implements FtpHelper {
    public final Logger logger = Logger.getLogger(this.getClass());
    public FTPClient ftpClient;

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
     * 连接FTP服务器
     * @param serverIp FTP服务器的IP地址
     * @param intPort FTP服务器端口号
     * @param user 登录FTP服务器的用户名
     * @param password 登录FTP服务器的用户名的口令
     * @param path 访问FTP服务器的文件路径
     * @param encoding 字符集编码
     * @throws IOException
     */
    public boolean connectServer(String serverIp, int port, String user, String password,
            String path, String encoding) {
        boolean isConn = false;
        ftpClient = new FTPClient();
        this.ftpClient.setControlEncoding(encoding);    //设置字符集
        this.ftpClient.setDataTimeout(30 * 1000);		//设置数据传输超时时间
        this.ftpClient.setConnectTimeout(30 * 1000);	//设置连接超时时间为20秒
        this.ftpClient.setBufferSize(1024 * 2);

        try {
            if(port > 0) {// ftp端口号
                this.ftpClient.connect(serverIp, port);
            }
            else {//使用默认的端口号
                this.ftpClient.connect(serverIp);
            }

            isConn = this.ftpClient.login(user, password);
            int reply = this.ftpClient.getReplyCode();  // FTP服务器连接回答
            if(!FTPReply.isPositiveCompletion(reply)) {// 判断请求连接的返回码是否为 230
                this.ftpClient.disconnect();
                logger.error("登录FTP服务失败！");
                return isConn;
            }
            if(isConn) {
                logger.info("恭喜" + user + "成功登陆FTP服务器！");
                this.ftpClient.enterLocalPassiveMode(); //被动连接模式（一定要配置）
                //设置二进制传输
            	this.ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            	this.ftpClient.setFileTransferMode(FTPClient.STREAM_TRANSFER_MODE);
                if(path.length() != 0) {// path是ftp服务下主目录的子目录
                	this.ftpClient.changeWorkingDirectory(path);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("登录FTP服务失败！" + e.getMessage());
        }
        return isConn;
    }

    /**
     * 断开与ftp服务器的链接
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
     * 检查FTP是否连接
     */
    public boolean isConnected() {
        if(this.ftpClient != null) {
            return this.ftpClient.isConnected();
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
			this.ftpClient.changeWorkingDirectory(remotePath+folderName);
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
    			this.ftpClient.makeDirectory(remotePath+folderName);
			} catch (Exception e) {
				return false;
			}
    	}

		return true;
	}

    /**
     * 上传Ftp文件
     * @param localFilePath 本地文件全路径（包含文件名）
     * @param remotePath    服务器远程目录
     * @param newFileName   上传后文件名称
     */
    public boolean uploadFile(String localFilePath, String remotePath, String newFileName) {
        java.io.File localFile = new java.io.File(localFilePath);
        return this.uploadFile(localFile, remotePath, newFileName);
    }

    /**
     * 上传Ftp文件
     * @param localFile 本地文件
     * @param String newFileName 上传到FTP服务器上的新文件名
     * @param remotePath 上传服务器路径 - 应该以“/”结束
     * @return boolean
     */
    public boolean uploadFile(File localFile, String remotePath, String newFileName) {
        BufferedInputStream inStream = null;
        String localFileName = localFile.getName();
        boolean uploadResult = false;
        try {
            inStream = new BufferedInputStream(new FileInputStream(localFile));
            if("".equals(newFileName)) {//新增的文件名称没有传，默认与上传的本地文件名相同
                newFileName = localFileName;
            }
            //logger.info(localFileName + "文件开始上传...");
            uploadResult = this.uploadFile(inStream, remotePath, newFileName);
            if(uploadResult) {
                return uploadResult;
            }
        }
        catch (FileNotFoundException e) {
        	logger.error(localFileName + "文件未找到...");
            e.printStackTrace();
        }
        return uploadResult;
    }

    /**
     * 上传Ftp文件
     * @param newFileName 上传到FTP服务器上的新文件名
     * @param inStream 本地上传文件的输入流
     * @param remotePath 上传服务器路径 - 应该以“/”结束
     * @return boolean
     */
    public boolean uploadFile(InputStream inStream, String remotePath, String newFileName) {
        boolean uploadResult = false;
        try {
            if(!"".equals(remotePath)) {//判断是否需要指定上传文件的路径
                this.ftpClient.changeWorkingDirectory(remotePath);  //改变工作路径
            }
            uploadResult = this.ftpClient.storeFile(newFileName, inStream);  //上传文件
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
        BufferedOutputStream outStream = null;
        boolean downloadResult = false;
        if("".equals(newFileName)) {//判断下载文件是否重新起名字，若没有默认为ftp下载的文件名相同
            newFileName = remoteFileName;
        }

        //文件路径
        String newFilePath = !"".equals(localPath) ? localPath + newFileName : newFileName;

        //判断存放本地文件目录是否存在，不存在时创建文件夹
        File dirFile = new File(newFilePath.substring(0, newFilePath.lastIndexOf("/")));
        if(!dirFile.exists()) {
        	dirFile.mkdirs();
        }

        try {
            if(!"".equals(remotePath)) {//判断ftp服务器路径是否调整
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
     * 下载多个文件
     * @param list 待下载的文件list集合
     * @param remoteDownLoadPath remoteFileName 所在的路径
     * @param localDir 下载到当地那个路径下
     */
    public void downloadFiles(List<String> list, String remoteDownLoadPath, String localDir) {
        if(list != null && list.size() > 0) {
            for(String fileName : list) {
                downloadFile(fileName, remoteDownLoadPath, "", localDir);
            }
        }
    }

    /**
     * 删除文件
     * @param remotePath 文件所在目录
     * @param fileName 	   文件名称
     */
    public boolean deleteFile(String remotePath, String fileName) {
    	boolean result = false;
        try {
        	if(ftpClient.changeWorkingDirectory(remotePath)) {
        		result = this.ftpClient.deleteFile(fileName);
        	}
        }
        catch (Exception e) {
        	logger.debug("文件删除失败:【" + fileName + "】");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 删除远程FTP服务器上的文件
     * @param 文件名称
     */
    public boolean deleteFile(String fileName) {
    	boolean result = false;
    	try {
    		result = this.ftpClient.deleteFile(fileName);
		}
    	catch (IOException e) {
    		logger.error("删除远程FTP服务器上的"+fileName+"文件失败！");
			e.printStackTrace();
		}
    	return result;
    }

    /**
     * 取得FTP服务器中某个目录下的所有文件列表的信息
     * @param remotePath FTP服务器的路径
     * @return List 包含文件的名称fileName 和 文件创建时间 createTime String类型的
     */
    public List<String> getFileList(String remotePath) {
        List<String> fileList = new ArrayList<String>();
        try {
            FTPListParseEngine engine = this.ftpClient.initiateListParsing(remotePath);
            FTPFile[] files = engine.getFiles();
            logger.debug("files.length ===> "+files.length);
            for(int i=0; i<files.length; i++) {
                if(files[i].isFile()) {//判断是否为文件
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
     * 取得FTP服务器某个目录下的符合条件的所有文件名
     * @param remotePath FTP服务器的路径
     * @param pattern 用于匹配文件名的正则表达式
     * @return List<String> 包含文件的名称fileName
     */
    public List<String> getFileList(String remotePath, String pattern) {
        List<String> nameList = new ArrayList<String>();
        try {
            FTPFile[] files = ftpClient.listFiles(remotePath, new MyFtpFileFilter(pattern));
            for(int i=0; i<files.length; i++) {
            	if(files[i].isFile()) {//判断是否为文件
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
     * 取得FTP服务器某个目录下的符合条件的所有文件名
     * @param remotePath FTP服务器的路径
     * @param pattern 用于匹配文件名的正则表达式
     * @return List<String> 包含文件的名称fileName
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

    /**
     * 解压ZIP文件
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
                logger.debug("【" + file.getName() + "】解压成功");
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
     * 在FTP服务器端生成XML文件
     * @author 戴晓飞 2013-09-17
     * @throws java.lang.Exception
     * @param document Document对象
     */
    public void upload(Document document) throws Exception {
        OutputStream os = null;
        try {
            String xmlFileName = "log" + DateHelper.getToday("yyyyMMddhhmmss") + ".xml";
            // 开始把Document映射到文件
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transFormer = transFactory.newTransformer();
            // 设置输出结果
            DOMSource domSource = new DOMSource(document);
            // 设置输出流
            os = ftpClient.storeFileStream("log/" + xmlFileName);
            StreamResult xmlResult = new StreamResult(os);
            // 输出xml文件
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
     * 上传文件夹
     * @param localDirectory 当地文件夹
     * @param remoteDirectoryPath Ftp 服务器路径 以目录"/"结束
     */
    public boolean uploadDirectory(String localDirectory, String remoteDirectoryPath) {
        File src = new File(localDirectory);
        try {
            remoteDirectoryPath = remoteDirectoryPath + src.getName() + "/";
            this.ftpClient.makeDirectory(remoteDirectoryPath);
        }
        catch (IOException e) {
            e.printStackTrace();
            logger.info(remoteDirectoryPath + "目录创建失败！");
        }
        File[] allFile = src.listFiles();

        // 循环遍历本件夹中的文件，单个文件上传
        for(int currentFile = 0; currentFile < allFile.length; currentFile++) {
            if(!allFile[currentFile].isDirectory()) {//判断本地文件是否是文件夹
                String srcName = allFile[currentFile].getPath().toString();
                uploadFile(new File(srcName), remoteDirectoryPath, ""); //上传文件
            }
        }

        for(int currentFile = 0; currentFile < allFile.length; currentFile++) {
            if(allFile[currentFile].isDirectory()) {
                // 递归调用，本件夹中包含的文件夹
                uploadDirectory(allFile[currentFile].getPath().toString(), remoteDirectoryPath);
            }
        }
        return true;
    }

    /**
     * 下载文件夹
     * @param localDirectoryPath本地地址
     * @param remoteDirectory 远程文件夹
     */
    public boolean downLoadDirectory(String localDirectoryPath, String remoteDirectory) {
        try {
            String fileName = new File(remoteDirectory).getName();//远程目录文件夹名称
            localDirectoryPath = localDirectoryPath + fileName + "/";
            FTPFile[] allFile = this.ftpClient.listFiles(remoteDirectory);
            if(allFile.length > 0) { //如果有文件
                new File(localDirectoryPath).mkdirs();
                // 遍历FTP服务器上的文件，单个下载到本地
                for(int currentFile = 0; currentFile < allFile.length; currentFile++) {
                    logger.debug("开始下载文件:【" + allFile[currentFile].getName() + "】");
                    if(!allFile[currentFile].isDirectory()) {//如果是文件，则下载
                        downloadFile(allFile[currentFile].getName(), remoteDirectory, localDirectoryPath);

                    }
                    else { //如果是文件夹，则先下载文件夹，再下载文件
                        String remoteDirectoryPath = remoteDirectory + "/" + allFile[currentFile].getName();
                        downLoadDirectory(localDirectoryPath, remoteDirectoryPath); //递归调用
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

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    /**
     * FTP文件过滤
     * @author 戴晓飞 2013-09-17
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