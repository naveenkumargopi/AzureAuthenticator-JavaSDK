package com.thumbsignin;

import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONObject;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.thumbsignin.domain.Membership;
import com.thumbsignin.domain.User;
import com.thumbsignin.util.HttpClientHelper;
import com.thumbsignin.util.JSONHelper;
import com.thumbsignin.util.Util;

import lombok.extern.slf4j.Slf4j;

/**
 * This class provides the methods to connect with Azure Active Directory and perform user specific operations such as 
 * authentication, retrieving user attributes etc.
 * 
 * @author Naveen Kumar Gopi
 * 
 */
@Slf4j
public class AzureAuthConnector {
	
	private static final String MICROSOFT_AUTHORITY_URL = "https://login.microsoftonline.com/";
	
	private static final String AZURE_AD_GRAPH_RESOURCE_ENDPOINT = "https://graph.windows.net";
	
	private static final String USER_NOT_FOUND_IN_AZURE_AD = "userRemovedFromAzureAD";
	
	private static final String ACCESS_TOKEN = "access_token";
	
	private static final String ID_TOKEN = "id_token";
	
	/*
	 * Authenticating user credentials in Azure AD via Resource Owner Password Credentials Grant flow
	 */
	public String authenticateUserInAzure(String azureTenantId, String userName, String userPwd, String azureClientId, String clientSecret) {
		
		String azureUserAuthenticationResponse = "";
		String authenticateURL = String.format("%s%s/oauth2/token", MICROSOFT_AUTHORITY_URL,azureTenantId);
		Map<String,String> postParamsMap = new LinkedHashMap<>();
		postParamsMap.put("resource", AZURE_AD_GRAPH_RESOURCE_ENDPOINT);
		postParamsMap.put("client_id", azureClientId);
		postParamsMap.put("client_secret", clientSecret);
		postParamsMap.put("username", userName);
		postParamsMap.put("password", userPwd);
		postParamsMap.put("grant_type", "password");
		postParamsMap.put("scope", "openid");
		
		try {
			byte[] postDataInBytes = HttpClientHelper.getPostDataAsBytes(postParamsMap);
			
			URL url = new URL(authenticateURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();			
			conn.setRequestMethod(HttpClientHelper.POST);
			conn.setRequestProperty(HttpClientHelper.CONTENT_TYPE, HttpClientHelper.URL_ENCODED_CONTENT_TYPE);
	        conn.setRequestProperty(HttpClientHelper.CONTENT_LENGTH, String.valueOf(postDataInBytes.length));
			conn.setRequestProperty(HttpClientHelper.ACCEPT, HttpClientHelper.APPLICATION_JSON);
			conn.setDoOutput(true);
			conn.getOutputStream().write(postDataInBytes);
			
			azureUserAuthenticationResponse = HttpClientHelper.getResponseStringFromConn(conn);
            
		} catch (Exception e) {
			log.error("Error occured while validating the user credentials: {}", e.getMessage());
			return Arrays.toString(e.getStackTrace());
		}
		
		JSONObject azureUserAuthenticationResponseJson = new JSONObject(azureUserAuthenticationResponse);
		if (azureUserAuthenticationResponseJson.has(ID_TOKEN)) {
			JSONObject authResponse = Util.getJwtPayloadData(azureUserAuthenticationResponseJson.getString(ID_TOKEN));
			authResponse.put(ACCESS_TOKEN, azureUserAuthenticationResponseJson.getString(ACCESS_TOKEN));
			return authResponse.toString();
		}
		return azureUserAuthenticationResponse;
		
	}
	
	/*
	 * To acquire the access token on behalf of the application (Client Credentials Grant Flow)
	 */
	public String acquireAccessTokenByClientCredentials(String azureTenantId, String azureClientId, String clientSecret) {
    	String accessToken = "";
    	try {
			ExecutorService service = Executors.newFixedThreadPool(1);
			AuthenticationContext context = new AuthenticationContext(MICROSOFT_AUTHORITY_URL+azureTenantId+"/", false, service);
			
			ClientCredential credential = new ClientCredential(azureClientId, clientSecret);
			Future<AuthenticationResult> future = context.acquireToken(AZURE_AD_GRAPH_RESOURCE_ENDPOINT, credential, null);
			AuthenticationResult result = future.get();
			accessToken = result.getAccessToken();
		} catch (Exception e) {
			log.error(e.getMessage());
		} 
    	return accessToken;
    }
	
	public String getDecodedJwtTokenPayload(String jwtToken) {		
		return Util.getJwtPayloadData(jwtToken).toString();
	}
	
	/*
	 * This will return the user name for a given userId from Azure AD
	 */
	public String getUserNameByIdFromGraph(String userId, String azureTenantId, String accessToken) {
    	User user = new User();
    	try
    	{
    		String urlStr = String.format("%s/%s/users/%s?api-version=1.6", AZURE_AD_GRAPH_RESOURCE_ENDPOINT, azureTenantId, userId);
    		
    		URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty(HttpClientHelper.AUTHORIZATION, accessToken);
            conn.setRequestProperty(HttpClientHelper.CONTENT_TYPE, HttpClientHelper.APPLICATION_JSON);
            conn.setRequestProperty(HttpClientHelper.ACCEPT, "application/json;odata=minimalmetadata");
            
            String responseStr = HttpClientHelper.getResponseStringFromConn(conn, true);
            int responseCode = conn.getResponseCode();
            JSONObject response = HttpClientHelper.processGoodRespStr(responseCode, responseStr);
            JSONObject userJsonObj = JSONHelper.fetchDirectoryObjectJSONObject(response);
            JSONHelper.convertJSONObjectToDirectoryObject(userJsonObj, user);
    	}
    	catch (FileNotFoundException e) {
    		return USER_NOT_FOUND_IN_AZURE_AD;
    	}
    	catch (Exception e)
    	{
    		log.error(e.getMessage());
    	}
    	return user.getGivenName();
      }
	
	/*
	 * This will return the user membership information of a given userId from Azure AD
	 */
	public List<Membership> getUserMembershipInfoFromGraph(String userId, String azureTenantId, String accessToken) {
		List<Membership> listOfUserMemberships = new ArrayList<>();
    	try
    	{
    		String urlStr = String.format("%s/%s/users/%s/memberOf?api-version=1.6", AZURE_AD_GRAPH_RESOURCE_ENDPOINT, azureTenantId, userId);
    		
    		URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty(HttpClientHelper.AUTHORIZATION, accessToken);
            conn.setRequestProperty(HttpClientHelper.CONTENT_TYPE, HttpClientHelper.APPLICATION_JSON);
            conn.setRequestProperty(HttpClientHelper.ACCEPT, "application/json;odata=minimalmetadata");
            
            String responseStr = HttpClientHelper.getResponseStringFromConn(conn, true);
            int responseCode = conn.getResponseCode();
            JSONObject response = HttpClientHelper.processGoodRespStr(responseCode, responseStr);
            JSONArray userMemberships;
            
            userMemberships = JSONHelper.fetchDirectoryObjectJSONArray(response);
            
            Membership membership;
            for (int i = 0; i < userMemberships.length(); i++) {
                JSONObject userMembershipJSONObject = userMemberships.optJSONObject(i);
                membership = new Membership();
                JSONHelper.convertJSONObjectToDirectoryObject(userMembershipJSONObject, membership);
                listOfUserMemberships.add(membership);
            }
    	}
    	catch (FileNotFoundException e) {
    		return null;
    	}
    	catch (Exception e)
    	{
    		log.error(e.getMessage());
    	}
    	return listOfUserMemberships;
	}
	
}
