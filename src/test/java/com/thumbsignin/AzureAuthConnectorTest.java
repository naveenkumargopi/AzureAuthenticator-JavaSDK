package com.thumbsignin;

import org.json.JSONObject;
import org.junit.Test;

import com.thumbsignin.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AzureAuthConnectorTest {
	
	private static final String AZURE_TENANT_ID = "c5bd07ef-f708-4577-84ce-e0e1faca9b8f";
	
	private static final String AZURE_CLIENT_ID = "30408b60-ccdc-4533-852a-220e75a6633f";
	
	private static final String AZURE_CLIENT_SECRET = "WNbKiL0xj8PJkAk+LkdtQuUfhYjCNUFFJ94d1H2vHqw=";
	
	private static final String AZURE_USER_NAME = "demo@ak1976hotmail.onmicrosoft.com";
	
	private static final String AZURE_USER_ID = "57d47a0b-b834-4906-b60b-2bd177f6369e";
	
	private static final String ACCESS_TOKEN = "access_token";
	
	private static final String ERROR = "error";
	
	private static final String INVALID_GRANT = "invalid_grant";
	
	private AzureAuthConnector azureAuthConnector = new AzureAuthConnector();
	
	
	@Test
	public void testAzureUserAuthenticationPositiveFlow() {
		
		log.info("Start Time.");
		String response = azureAuthConnector.authenticateUserInAzure(AZURE_TENANT_ID, AZURE_USER_NAME, Util.decode("UHJhbWF0aUAxMjM="), AZURE_CLIENT_ID, AZURE_CLIENT_SECRET);
		log.info("Response: {}", response);
		JSONObject responseJson = new JSONObject(response);
		assert (response != null);
		assert (responseJson.has(ACCESS_TOKEN));
		assert (responseJson.get("upn").equals(AZURE_USER_NAME));

	}
	
	@Test
	public void testAzureUserAuthenticationNegativeFlow() {
		
		log.info("Start Time.");
		String response = azureAuthConnector.authenticateUserInAzure(AZURE_TENANT_ID, AZURE_USER_NAME, "invalid", AZURE_CLIENT_ID, AZURE_CLIENT_SECRET);
		log.info("Response: {}", response);
		JSONObject responseJson = new JSONObject(response);
		assert (response != null);
		assert (responseJson.has(ERROR));
		assert (responseJson.get(ERROR).equals(INVALID_GRANT));
		
	}
	
	@Test
	public void testGetUserNameById() {
		
		log.info("Getting access_token by client credentials..");
		String accessToken = azureAuthConnector.acquireAccessTokenByClientCredentials(AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET);
		log.info("Start Time.");
		String response = azureAuthConnector.getUserNameByIdFromGraph(AZURE_USER_ID, AZURE_TENANT_ID, accessToken);
		log.info("Response: {}", response);
		assert (accessToken != null);
		assert (response != null);
		
	}
	
	@Test
	public void testUserMembershipInfo() {
		
		log.info("Getting access_token by client credentials..");
		String accessToken = azureAuthConnector.acquireAccessTokenByClientCredentials(AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET);
		log.info("Start Time.");
		Object response = azureAuthConnector.getUserMembershipInfoFromGraph(AZURE_USER_ID, AZURE_TENANT_ID, accessToken);
		log.info("Response: {}", response);
		assert (accessToken != null);
		assert (response != null);
		
	}
	
}
