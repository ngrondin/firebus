package io.firebus.adapters.http.idm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import io.firebus.Firebus;
import io.firebus.adapters.http.FirebusHttpException;
import io.firebus.adapters.http.HttpGateway;
import io.firebus.adapters.http.IDMHandler;
import io.firebus.data.DataException;
import io.firebus.data.DataMap;

public class OAuth2IDM extends IDMHandler
{
	protected String basePath;
	protected String loginUrl;
	protected String tokenUrl;
	protected String keysUrl;
	protected String clientId;
	protected String clientSecret;
	protected String scope;
	protected String jwtSecret;

	public OAuth2IDM(HttpGateway gw, Firebus f, DataMap c) 
	{
		super(gw, f, c);
		basePath = path.endsWith("/*") ? path.substring(0, path.length() - 2) : path;
		loginUrl = handlerConfig.getString("loginurl");
		tokenUrl = handlerConfig.getString("tokenurl");
		keysUrl = handlerConfig.getString("keysurl");
		clientId = handlerConfig.getString("clientid");
		clientSecret = handlerConfig.getString("clientsecret");
		scope = handlerConfig.getString("scope");
		jwtSecret = handlerConfig.getString("jwtsecret");
	}

    public void inboundService(HttpServletRequest req, HttpServletResponse resp)  throws Exception {
    	if(tokenUrl == null || clientId == null || clientSecret == null) throw new FirebusHttpException("Authentication configuration missing", 500, null);
		String shortPath = getShortPath(req);
    	if(shortPath.equals("/code")) {
    		codeService(req, resp);
    	} else if(shortPath.equals("/refresh")) {
    		refreshService(req, resp);
    	}
    }
    
    public void codeService(HttpServletRequest req, HttpServletResponse resp) throws Exception {
    	String code = req.getParameter("code");
    	if(code == null) throw new FirebusHttpException("Missing code in authorization code request", 400, null);
    	String state = req.getParameter("state");
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("code", code));
		params.add(new BasicNameValuePair("client_id", clientId));
		params.add(new BasicNameValuePair("client_secret", clientSecret));
		params.add(new BasicNameValuePair("redirect_uri", geCodeURL()));
		params.add(new BasicNameValuePair("grant_type", "authorization_code"));
		DataMap respMap = callTokenUrl(params);
		String accessToken = respMap.getString("access_token");
		String refreshToken = respMap.getString("refresh_token");
		long expiry = respMap.getNumber("expires_in").longValue();
		_securityHandler.enrichAuthResponse(req, resp, accessToken, refreshToken, expiry, state);
    }
    
    public void refreshService(HttpServletRequest req, HttpServletResponse resp) throws Exception {
    	String refreshToken = _securityHandler.extractRefreshToken(req);
    	if(refreshToken == null) throw new FirebusHttpException("Missing refresh token in refresh request", 400, null);
    	String state = req.getParameter("state");
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("refresh_token", refreshToken));
		params.add(new BasicNameValuePair("client_id", clientId));
		params.add(new BasicNameValuePair("client_secret", clientSecret));
		params.add(new BasicNameValuePair("redirect_uri", geCodeURL()));
		params.add(new BasicNameValuePair("grant_type", "refresh_token"));
		try {
			DataMap respMap = callTokenUrl(params);
			String accessToken = respMap.getString("access_token");
			String newRefreshToken = respMap.getString("refresh_token");
			long expiry = respMap.getNumber("expiry").longValue();
			_securityHandler.enrichRefreshResponse(req, resp, accessToken, newRefreshToken, expiry, state); 	
		} catch(Exception e) {
			resp.sendRedirect("/logout");
		}
    }
    

	public String getLoginURL(String originalPath) {
		String originalUrl = httpGateway.getPublicHost() + originalPath;
		long nonce = (int)(Math.random() * 1000000);
		String url = loginUrl + "?client_id=" + clientId + "&response_type=code&scope=" + scope + "&redirect_uri=" + geCodeURL() + "&state=" + originalUrl + "&nonce=" + nonce;
		return url;
	}

	public String geCodeURL() {
		return httpGateway.getPublicHost() + basePath + "/code";
	}
	
	public String geRefereshURL(String originalPath) {
		String originalUrl = httpGateway.getPublicHost() + originalPath;
		return httpGateway.getPublicHost() + basePath + "/refresh?state=" + originalUrl;
	}
	
	public String getJWTSecret() {
		return jwtSecret;
	}
	
	public DataMap getJWKData() throws Exception {
		if(keysUrl != null) {
			return callKeysUrl();
		}
		return null;
	}
	
	protected DataMap callTokenUrl(List<NameValuePair> params) throws Exception {
   		int respStatus = -1;
		DataMap respMap = null;
		HttpPost httppost = new HttpPost(tokenUrl);
   		httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		CloseableHttpResponse response = httpGateway.getHttpClient().execute(httppost);
		try {
    		respStatus = response.getStatusLine().getStatusCode(); 
    		HttpEntity entity = response.getEntity();
    		if (entity != null) 
    		{
    			InputStream is = entity.getContent();
    			try { respMap = new DataMap(is); }
    			catch(DataException e) {}
    		}        		
		} finally {
			response.close();
		}
		
		if(respStatus >= 200 && respStatus < 400) {
    		if (respMap != null) {
    			return respMap;
    		} else {
    			throw new FirebusHttpException("Token is empty", respStatus, null);
    		}
		} else {
			throw new FirebusHttpException("Error retreiving idm tokens", respStatus, null);
		}
    }
	    
	protected DataMap callKeysUrl() throws Exception {
		DataMap respMap = null;
		int respStatus;
		HttpGet httpget = new HttpGet(keysUrl);
		CloseableHttpResponse response = httpGateway.getHttpClient().execute(httpget);
		try {
    		respStatus = response.getStatusLine().getStatusCode(); 
    		HttpEntity entity = response.getEntity();
    		if (entity != null)  {
    			InputStream is = entity.getContent();
    			try { respMap = new DataMap(is); }
    			catch(DataException e) {}
    		}        		
		} finally {
			response.close();
		}
		
		if(respStatus >= 200 && respStatus < 400) {
    		if (respMap != null) {
    			return respMap;
    		} else {
    			throw new FirebusHttpException("Keys response is empty", respStatus, null);
    		}
		} else {
			throw new FirebusHttpException("Error retreiving idm keys", respStatus, null);
		}
    }	
}
