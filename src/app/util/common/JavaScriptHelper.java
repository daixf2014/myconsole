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
	 * ����js�ļ��е�ĳһ��������������
	 * @param code ��Ҫ���������
	 * @param JsURL js�ļ���url
	 * @param functionName ���õ�js�ļ��еĺ�����
	 * @return newCode ��js�ļ��еĺ����������µ�����
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
			inputStreamReader = getInputContent("GET", null, conn);// ��ȡjs����
		} catch (ConnectException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		engine.eval(inputStreamReader);
		if (engine instanceof Invocable) {
			Invocable invoke = (Invocable) engine;
			newCode = (String) invoke.invokeFunction(functionName, code);// ���ö�Ӧ������������1������:��Ҫ������ַ�������
		}
		inputStreamReader.close();
		return newCode;
	}

	/**
	 * ͨ������ȡ��������
	 */
	private static InputStreamReader getInputContent(String requestMethod, String outputStr, HttpURLConnection conn)
			throws ProtocolException, IOException, UnsupportedEncodingException { // ����װ��http���󷽷�����Ҫ���õķ���
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		// ��������ʽ��GET/POST��
		conn.setRequestMethod(requestMethod);
		// ��outputStr��Ϊnullʱ�������д����
		if (null != outputStr) {
			OutputStream outputStream = conn.getOutputStream();
			// ע������ʽ
			outputStream.write(outputStr.getBytes("UTF-8"));
			outputStream.close();
		}
		// ����������ȡ��������
		InputStream inputStream = conn.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
		return inputStreamReader;
	}
}
