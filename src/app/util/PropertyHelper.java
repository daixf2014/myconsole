package app.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 属性配置文件帮助类
 */
public class PropertyHelper {
	/**
	 * 获取配置文件中的键值对
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
	 * 根据键获取配置文件中的键值
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
	 * 获取配置文件中的键值
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