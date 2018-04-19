package app.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ���������ļ�������
 */
public class PropertyHelper {
	/**
	 * ��ȡ�����ļ��еļ�ֵ��
	 * @return
	 */
	public static Properties getPropertiesFileInfo() {
		Properties properties = new Properties();
		try {
			InputStream is = PropertyHelper.class.getResourceAsStream(Constants.PROPERTIES_FILE);
			properties.load(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}

	/**
	 * ���ݼ���ȡ�����ļ��еļ�ֵ
	 * @param key
	 * @return
	 */
	public static String getPropertiesFileValue(String key){
		Properties properties = new Properties();
		try {
			InputStream is = PropertyHelper.class.getResourceAsStream(Constants.PROPERTIES_FILE);
			properties.load(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties.getProperty(key);
	}

	/**
	 * ��ȡ�����ļ��еļ�ֵ
	 * @param fileName
	 * @param key
	 * @return
	 */
	public static String getPropertiesFileValue(String fileName, String key) {
		Properties properties = new Properties();
		try {
		    FileInputStream is = new FileInputStream(new File(fileName));
			properties.load(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties.getProperty(key);
	}
}