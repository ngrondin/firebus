package io.firebus.adapters.http.idm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;

import io.firebus.Firebus;
import io.firebus.adapters.http.FirebusHttpException;
import io.firebus.adapters.http.HttpGateway;
import io.firebus.adapters.http.IDMHandler;
import io.firebus.data.DataException;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;

public class OAuth2IDM extends IDMHandler
{
	protected String basePath;
	protected String loginUrl;
	protected String tokenUrl;
	protected String invalidateUrl;
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
		invalidateUrl = handlerConfig.getString("invalidateurl");
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
    	DataMap respMap = this.callTokenUrl(getCodeURL(req), "authorization_code", code);
		String accessToken = respMap.getString("access_token");
		String refreshToken = respMap.getString("refresh_token");
		long expiry = (new Date()).getTime() + (respMap.getNumber("expires_in").longValue() * 1000);
		_securityHandler.sendAuthResponse(req, resp, accessToken, expiry, refreshToken, basePath + "/refresh", state);
    }
    
    public void refreshService(HttpServletRequest req, HttpServletResponse resp) throws Exception {
    	String refreshToken = _securityHandler.extractRefreshToken(req);
    	if(refreshToken != null) {
    		try {
    			String state = req.getParameter("state");
		    	String action = req.getParameter("action");
		    	if(action == null || (action != null && action.equals("refresh"))) {
	    			DataMap respMap = callTokenUrl(getCodeURL(req), "refresh_token", refreshToken);
	    			String accessToken = respMap.getString("access_token");
	    			String newRefreshToken = respMap.getString("refresh_token");
	    			long expiry = (new Date()).getTime() + (respMap.getNumber("expires_in").longValue() * 1000);
	    			_securityHandler.sendRefreshResponse(req, resp, accessToken, expiry, newRefreshToken, basePath + "/refresh", state); 	
		    	} else if(action.equals("invalidate")) {
		        	callInvalidateUrl(refreshToken);
		    		_securityHandler.enrichLogoutResponse(req, resp);
		    		resp.sendRedirect("/logout");
		    	}
    		} catch(Exception e) {
    			Logger.severe("fb.http.oauth2.refresh", e);
    			resp.sendRedirect("/logout");
    		}    		
    	} else {
    		resp.sendRedirect("/logout");
    	}
    }
    
	public String getLoginURL(HttpServletRequest req, String originalPath) {
		String originalUrl = getHostUrl(req) + originalPath;
		long nonce = (int)(Math.random() * 1000000);
		String url = loginUrl + "?client_id=" + clientId + "&response_type=code&scope=" + scope + "&redirect_uri=" + getCodeURL(req) + "&state=" + originalUrl + "&nonce=" + nonce;
		return url;
	}

	public String getCodeURL(HttpServletRequest req) {
		return getHostUrl(req) + basePath + "/code";
	}
	
	public String getRefreshPath(HttpServletRequest req, String originalPath) {
		String url = basePath + "/refresh";
		if(originalPath != null) url = url + "?state=" + originalPath;
		return url;
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
	
	protected DataMap callTokenUrl(String redirect_uri, String grant_type, String grant_value) throws Exception {
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("client_id", clientId));
		params.add(new BasicNameValuePair("client_secret", clientSecret));
		params.add(new BasicNameValuePair("redirect_uri", redirect_uri));
		params.add(new BasicNameValuePair("grant_type", grant_type));
		if(grant_type.equals("refresh_token")) {
			params.add(new BasicNameValuePair("refresh_token", grant_value));			
		} else if(grant_type.equals("authorization_code")) {
			params.add(new BasicNameValuePair("code", grant_value));			
		} 
		HttpPost post = new HttpPost(tokenUrl);
		post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		return call(post);
    }	
	
	protected DataMap callInvalidateUrl(String refreshToken) throws Exception {
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("client_id", clientId));
		params.add(new BasicNameValuePair("client_secret", clientSecret));
		params.add(new BasicNameValuePair("redirect_uri", refreshToken));
		params.add(new BasicNameValuePair("grant_type", "refresh_token"));
		HttpPost post = new HttpPost(invalidateUrl);
		post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		return call(post);
    }	
	    
	protected DataMap callKeysUrl() throws Exception {
		HttpGet httpget = new HttpGet(keysUrl);
		return call(httpget);
    }	
	
	protected DataMap call(HttpUriRequest request) throws Exception {
		DataMap respMap = null;
		int respStatus;
		CloseableHttpResponse response = httpGateway.getHttpClient().execute(request);
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
				throw new FirebusHttpException("Data is empty", respStatus, null);
			}
		} else {
			throw new FirebusHttpException("Error executing http call", respStatus, null);
		}
	}
}
