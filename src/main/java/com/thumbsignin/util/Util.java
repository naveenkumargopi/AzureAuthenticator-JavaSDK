package com.thumbsignin.util;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

public class Util {

	public static JSONObject getJwtPayloadData(String jwtToken) {
		
		//Decode JWT
        String[] jwtTokenSplitString = jwtToken.split("\\.");
        String base64EncodedBody = jwtTokenSplitString[1];

        String jwtPayloadData = decode(base64EncodedBody);
        return new JSONObject(jwtPayloadData);
		
	}
	
	public static String decode(String encodedStr) {
		
		Base64 base64Url = new Base64(true);
		return new String(base64Url.decode(encodedStr));
		
	}
	
}
