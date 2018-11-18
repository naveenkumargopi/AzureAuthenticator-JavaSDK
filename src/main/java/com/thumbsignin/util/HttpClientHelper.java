package com.thumbsignin.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is Helper class for all RestClient class.
 * 
 */
public class HttpClientHelper {
	
	public static final String UTF8 = "UTF-8";
	
	public static final String GET = "GET";
	
	public static final String POST = "POST";
	
	public static final String ACCEPT = "Accept";
	
	public static final String CONTENT_TYPE = "Content-Type";
	
	public static final String CONTENT_LENGTH = "Content-Length";
	
	public static final String APPLICATION_JSON = "application/json";
	
	public static final String URL_ENCODED_CONTENT_TYPE = "application/x-www-form-urlencoded";
	
	public static final String AUTHORIZATION = "Authorization";
    
    public static byte[] getPostDataAsBytes(Map<String,String> postParamsMap) throws UnsupportedEncodingException {
    	
    	//This will convert the map into this format: "param1=value1&param2=value2&param3=value3"
    	StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,String> param : postParamsMap.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), UTF8));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), UTF8));
        }
        return postData.toString().getBytes(UTF8);
        
    }
    
    public static String getResponseStringFromConn(HttpURLConnection conn) throws IOException {
    	
    	//Use conn.getInputStream() for codes between 200 and 299. For any other response code use conn.getErrorStream().
    	BufferedReader reader = null;
    	reader = new BufferedReader(new InputStreamReader(conn.getResponseCode() / 100 == 2 ? conn.getInputStream() : conn.getErrorStream()));
    	StringBuffer stringBuffer = new StringBuffer();
        String line = "";
        while ((line = reader.readLine()) != null) {
            stringBuffer.append(line);
        }
        return stringBuffer.toString();
    	
    }

    public static String getResponseStringFromConn(HttpURLConnection conn, boolean isSuccess) throws IOException {

        BufferedReader reader = null;
        if (isSuccess) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuffer stringBuffer = new StringBuffer();
        String line = "";
        while ((line = reader.readLine()) != null) {
            stringBuffer.append(line);
        }

        return stringBuffer.toString();
    }

    public static String getResponseStringFromConn(HttpURLConnection conn, String payLoad) throws IOException {

        // Send the http message payload to the server.
        if (payLoad != null) {
            conn.setDoOutput(true);
            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
            osw.write(payLoad);
            osw.flush();
            osw.close();
        }

        // Get the message response from the server.
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = "";
        StringBuffer stringBuffer = new StringBuffer();
        while ((line = br.readLine()) != null) {
            stringBuffer.append(line);
        }

        br.close();

        return stringBuffer.toString();
    }

    public static byte[] getByteArrayFromConn(HttpURLConnection conn, boolean isSuccess) throws IOException {

        InputStream is = conn.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int bytesRead = 0;

        while ((bytesRead = is.read(buff, 0, buff.length)) != -1) {
            baos.write(buff, 0, bytesRead);
        }

        byte[] bytes = baos.toByteArray();
        baos.close();
        return bytes;
    }

    /**
     * for bad response, whose responseCode is not 200 level
     * 
     * @param responseCode
     * @param errorCode
     * @param errorMsg
     * @return
     * @throws JSONException
     */
    public static JSONObject processResponse(int responseCode, String errorCode, String errorMsg) throws JSONException {
        JSONObject response = new JSONObject();
        response.put("responseCode", responseCode);
        response.put("errorCode", errorCode);
        response.put("errorMsg", errorMsg);

        return response;
    }

    /**
     * for bad response, whose responseCode is not 200 level
     * 
     * @param responseCode
     * @param errorCode
     * @param errorMsg
     * @return
     * @throws JSONException
     */
    public static JSONObject processGoodRespStr(int responseCode, String goodRespStr) throws JSONException {
        JSONObject response = new JSONObject();
        response.put("responseCode", responseCode);
        if (goodRespStr.equalsIgnoreCase("")) {
            response.put("responseMsg", "");
        } else {
            response.put("responseMsg", new JSONObject(goodRespStr));
        }

        return response;
    }

    /**
     * for good response
     * 
     * @param responseCode
     * @param responseMsg
     * @return
     * @throws JSONException
     */
    public static JSONObject processBadRespStr(int responseCode, String responseMsg) throws JSONException {

        JSONObject response = new JSONObject();
        response.put("responseCode", responseCode);
        if (responseMsg.equalsIgnoreCase("")) { // good response is empty string
            response.put("responseMsg", "");
        } else { // bad response is json string
            JSONObject errorObject = new JSONObject(responseMsg).optJSONObject("odata.error");

            String errorCode = errorObject.optString("code");
            String errorMsg = errorObject.optJSONObject("message").optString("value");
            response.put("responseCode", responseCode);
            response.put("errorCode", errorCode);
            response.put("errorMsg", errorMsg);
        }

        return response;
    }

}
