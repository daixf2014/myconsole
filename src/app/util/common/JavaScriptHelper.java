package app.util.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class JavaScriptHelper {

	/**
	 * 调用js文件中的某一函数，处理数据
	 * @param code 需要处理的数据
	 * @param JsURL js文件的url
	 * @param functionName 调用的js文件中的函数名
	 * @return newCode 被js文件中的函数处理后的新的数据
	 */
	public static String processDataViaJs(String code, String JsURL, String functionName) throws Exception {
		ScriptEngineManager manager = new ScriptEngineManager();
		String newCode = "";
		InputStreamReader inputStreamReader = null;
		ScriptEngine engine = manager.getEngineByName("javascript");
		try {
			URL url = new URL(JsURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Content-Type", "text/html");
			inputStreamReader = getInputContent("GET", null, conn);// 获取js内容
		} catch (ConnectException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		engine.eval(inputStreamReader);
		if (engine instanceof Invocable) {
			Invocable invoke = (Invocable) engine;
			newCode = (String) invoke.invokeFunction(functionName, code);// 调用对应方法，并传入1个参数:需要处理的字符串符串
		}
		inputStreamReader.close();
		return newCode;
	}

	/**
	 * 通过流获取返回内容
	 */
	private static InputStreamReader getInputContent(String requestMethod, String outputStr, HttpURLConnection conn)
			throws ProtocolException, IOException, UnsupportedEncodingException { // （封装的http请求方法）需要调用的方法
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		// 设置请求方式（GET/POST）
		conn.setRequestMethod(requestMethod);
		// 当outputStr不为null时向输出流写数据
		if (null != outputStr) {
			OutputStream outputStream = conn.getOutputStream();
			// 注意编码格式
			outputStream.write(outputStr.getBytes("UTF-8"));
			outputStream.close();
		}
		// 从输入流读取返回内容
		InputStream inputStream = conn.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
		return inputStreamReader;
	}
}
