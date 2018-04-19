package app.util.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

public class FileHelper {

	/**
	 * ��ȡ�ı��ļ�����
	 * @param filePathAndName ������������·�����ļ���
	 * @param encoding �ı��ļ��򿪵ı��뷽ʽ
	 * @return �����ı��ļ�������
	 */
	public String readTxt(String filePathAndName, String encoding)
			throws IOException {
		encoding = encoding.trim();
		StringBuffer str = new StringBuffer("");
		String st = "";
		try {
			FileInputStream fs = new FileInputStream(filePathAndName);
			InputStreamReader isr;
			if (encoding.equals("")) {
				isr = new InputStreamReader(fs);
			} else {
				isr = new InputStreamReader(fs, encoding);
			}
			BufferedReader br = new BufferedReader(isr);
			try {
				String data = "";
				while ((data = br.readLine()) != null) {
					str.append(data + " ");
				}
			} catch (Exception e) {
				str.append(e.toString());
			}
			st = str.toString();
		} catch (IOException es) {
			st = "";
		}
		return st;
	}

	/**
	 * �½�Ŀ¼
	 * @param folderPath Ŀ¼
	 * @return ����Ŀ¼�������·��
	 */
	public String createFolder(String folderPath) {
		String txt = folderPath;
		try {
			java.io.File myFilePath = new java.io.File(txt);
			txt = folderPath;
			if (!myFilePath.exists()) {
				myFilePath.mkdir();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return txt;
	}

	/**
	 * �༶Ŀ¼����
	 * @param folderPath ׼��Ҫ�ڱ���Ŀ¼�´�����Ŀ¼��Ŀ¼·�� ���� c:myf
	 * @param paths ���޼�Ŀ¼����������Ŀ¼�Ե��������� ���� a|b|c
	 * @return ���ش����ļ����·�� ���� c:myfac
	 */
	public String createFolders(String folderPath, String paths) {
		String txts = folderPath;
		try {
			String txt;
			txts = folderPath;
			StringTokenizer st = new StringTokenizer(paths, "|");
			for (int i = 0; st.hasMoreTokens(); i++) {
				txt = st.nextToken().trim();
				if (txts.lastIndexOf("/") != -1) {
					txts = createFolder(txts + txt);
				} else {
					txts = createFolder(txts + txt + "/");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return txts;
	}

	/**
	 * �½��ļ�
	 * @param filePathAndName �ı��ļ���������·�����ļ���
	 * @param fileContent �ı��ļ�����
	 * @return
	 */
	public void createFile(String path,String fileName, String fileContent) {
		String filePath ="";
		try {
			 filePath = path+fileName;

			File myFilePath = new File(path,fileName);
			if (!myFilePath.exists()) {
				myFilePath.createNewFile();
			}
			FileWriter resultFile = new FileWriter(myFilePath);
			PrintWriter myFile = new PrintWriter(resultFile);
			String strContent = fileContent;
			myFile.println(strContent);
			myFile.close();
			resultFile.close();
		} catch (Exception e) {
			System.out.println(filePath);
			e.printStackTrace();
		}
	}

	/**
	 * ɾ���ļ�
	 * @param filePathAndName �ı��ļ���������·�����ļ���
	 * @return Boolean �ɹ�ɾ������true�����쳣����false
	 */
	public boolean delFile(String filePathAndName) {
		boolean bea = false;
		try {
			String filePath = filePathAndName;
			File myDelFile = new File(filePath);
			if (myDelFile.exists()) {
				myDelFile.delete();
				bea = true;
			} else {
				bea = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bea;
	}

	/**
	 * ɾ���ļ���
	 * @param folderPath �ļ�����������·��
	 * @return
	 */
	public void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // ɾ����������������
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete(); // ɾ�����ļ���
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ɾ��ָ���ļ����������ļ�
	 * @param path �ļ�����������·��
	 * @return
	 */
	public boolean delAllFile(String path) {
		boolean bea = false;
		File file = new File(path);
		if (!file.exists()) {
			return bea;
		}
		if (!file.isDirectory()) {
			return bea;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// ��ɾ���ļ���������ļ�
				delFolder(path + "/" + tempList[i]);// ��ɾ�����ļ���
				bea = true;
			}
		}
		return bea;
	}

	/**
	 * ���Ƶ����ļ�
	 * @param oldPathFile ׼�����Ƶ��ļ�Դ
	 * @param newPathFile �������¾���·�����ļ���
	 * @return
	 */
	public int copyFile(String oldPathFile, String newPathFile) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPathFile);
			if (oldfile.exists()) { // �ļ�����ʱ
				InputStream inStream = new FileInputStream(oldPathFile); // ����ԭ�ļ�
				FileOutputStream fs = new FileOutputStream(newPathFile);
				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // �ֽ��� �ļ���С
					// System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				fs.close();
				inStream.close();

			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	public static int copyFile(File oldfile, String newPathFile) {
		try {
			int bytesum = 0;
			int byteread = 0;
			if (oldfile.exists()) { // �ļ�����ʱ
				InputStream inStream = new FileInputStream(oldfile.getPath()); // ����ԭ�ļ�
				FileOutputStream fs = new FileOutputStream(newPathFile);
				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // �ֽ��� �ļ���С
					fs.write(buffer, 0, byteread);
				}
				fs.close();
				inStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	/**
	 * ���������ļ��е�����
	 * @param oldPath ׼��������Ŀ¼
	 * @param newPath ָ������·������Ŀ¼
	 * @return
	 */
	public void copyFolder(String oldPath, String newPath) {
		try {
			new File(newPath).mkdirs(); // ����ļ��в����� �������ļ���
			File a = new File(oldPath);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; i++) {
				if (oldPath.endsWith(File.separator)) {
					temp = new File(oldPath + file[i]);
				} else {
					temp = new File(oldPath + File.separator + file[i]);
				}
				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath
							+ "/" + (temp.getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {// ��������ļ���
					copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * �ƶ��ļ�
	 * @param oldPath
	 * @param newPath
	 * @return
	 */
	public void moveFile(String oldPath, String newPath) {
		copyFile(oldPath, newPath);
		delFile(oldPath);
	}

	/**
	 * �ƶ�Ŀ¼
	 * @param oldPath
	 * @param newPath
	 * @return
	 */
	public void moveFolder(String oldPath, String newPath) {
		copyFolder(oldPath, newPath);
		delFolder(oldPath);
	}

	/**
	 * �ϴ��ļ�����Ҫ�������ļ�
	 * @param upload_file_name
	 * @return
	 */
	public static String getToFileName(String upload_file_name) {
		int idx = upload_file_name.lastIndexOf(".");
		String to_file_name = upload_file_name.substring(0, idx)
				+ DateHelper.getToday("yyyyMMddHHmmss") + "."
				+ upload_file_name.substring(idx + 1);
		return to_file_name;
	}

    /**
     * ���ݲ�ͬ�Ĳ���ϵͳ�õ���Ӧ�ļ�·��
     * @param dirPath �ļ�·��
     */
    public static String getPathBySeparator(String dirPath) {
        String separatorPath = "";
        // ��windowsϵͳ
        if (File.separator.equals("/")) {
            separatorPath=dirPath.replaceAll("\\\\", "/");
        }
        else {// windowsϵͳ
            separatorPath=dirPath.replaceAll("/", "\\\\");
        }

        return separatorPath;
    }

    /**
     * ���ݲ�ͬ�Ĳ���ϵͳ�õ���Ӧ�ļ�·��
     * @param dirPath �ļ�·��
     */
    public static String getFilePath(File file) {
        String separatorPath = "";
        // windowsϵͳ
        if (null != file && !File.separator.equals("/")) {
            separatorPath = file.getPath().replaceAll("\\\\", "/");
        }

        return separatorPath;
    }

    /**
     * ��ȡ��UUID���ɵ��µ��ļ����ƺͱ��
     * @param file_name �ļ�����(����׺)
     */
	public Map<String, String> getNewFileInfoByUUID(String file_name) {
		Map<String, String> newFileInfoMap = new HashMap<String, String>();
        String new_file_id = UUID.randomUUID().toString();
		String new_file_name = new_file_id + "." + file_name.substring(file_name.lastIndexOf(".") + 1);
		newFileInfoMap.put("new_file_id", new_file_id);
		newFileInfoMap.put("new_file_name", new_file_name);
		return newFileInfoMap;
	}

	/**
     * ��ȡ�ļ���׺����������.��
     * @param fileFileName �ļ���
     */
    public static String getFileSuffix(String fileFileName) {
		String fileSuffix = "";
		if(fileFileName.lastIndexOf(".") != -1) {
			fileSuffix = fileFileName.substring(fileFileName.lastIndexOf(".") + 1);
		}
    	return fileSuffix;
    }
}