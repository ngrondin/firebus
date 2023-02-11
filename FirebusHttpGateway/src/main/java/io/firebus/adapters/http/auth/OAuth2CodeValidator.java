package io.firebus.adapters.http.auth;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.firebus.Firebus;
import io.firebus.adapters.http.HttpRequest;
import io.firebus.adapters.http.HttpResponse;
import io.firebus.adapters.http.handlers.AuthValidationHandler;
import io.firebus.data.DataException;
import io.firebus.data.DataMap;

public class OAuth2CodeValidator extends AuthValidationHandler
{
	protected String loginUrl;
	protected String tokenUrl;
	protected String clientId;
	protected String clientSecret;
	protected String redirectUrl;

	public OAuth2CodeValidator(Firebus f, DataMap c, CloseableHttpClient hc) 
	{
		super(f, c, hc);
		loginUrl = handlerConfig.getString("loginurl");
		tokenUrl = handlerConfig.getString("tokenurl");
		clientId = handlerConfig.getString("clientid");
		clientSecret = handlerConfig.getString("clientsecret");
		redirectUrl = handlerConfig.getString("redirecturl");
	}

	protected HttpResponse httpService(HttpRequest req) {
		HttpResponse resp = null;
    	if(tokenUrl != null && clientId != null && clientSecret != null)
    	{
    		try {
            	String code = req.getParameter("code");
            	//String contextPath = req.getPath();
            	//if(contextPath.equals(""))
            	//	contextPath = "/";
            	String redirectUrlResolved = redirectUrl != null ? redirectUrl : "${state}";
           		redirectUrlResolved = redirectUrlResolved.replace("${state}", req.getParameter("state") != null ? req.getParameter("state") : "");
            	
            	if(code != null && redirectUrlResolved != null)
            	{
            		int respStatus = -1;
            		DataMap respMap = null;
            		HttpPost httppost = new HttpPost(tokenUrl);
            		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            		params.add(new BasicNameValuePair("code", code));
            		params.add(new BasicNameValuePair("client_id", clientId));
            		params.add(new BasicNameValuePair("client_secret", clientSecret));
            		params.add(new BasicNameValuePair("redirect_uri", publicHost + path));
            		params.add(new BasicNameValuePair("grant_type", "authorization_code"));
            		httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            		CloseableHttpResponse response = httpClient.execute(httppost);
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

            		if(respStatus >= 200 && respStatus < 400)
            		{
                		if (respMap != null) 
                		{
                			DecodedJWT jwt = JWT.decode(respMap.getString("id_token"));
                			Claim usernameClaim = jwt.getClaim("email");
                			String username = usernameClaim.asString();
                			resp = new HttpResponse(303, "<html><title>Redirect</title><body>Loging in</body></html>");
                			_securityHandler.enrichAuthenticatedHttpResponse(username, resp);
                			resp.setHeader("location", redirectUrlResolved);		
                		}
                		else
                		{
                			resp = new HttpResponse(500, "<html><title>Error</title><body>Token is empty</body></html>");
                		}
            		}
            		else
            		{
                		if (respMap != null) 
                		{
                			resp = new HttpResponse(500, "<html><title>Error</title><body>Return code : " + respStatus + "<br>" + respMap.toString() + "</body></html>");
                		}
                		else
                		{
                			resp = new HttpResponse(500, "<html><title>Error</title><body>Return code : " + respStatus + "</body></html>");
                		}
            		}
            	}    			
    		} catch(Exception e) {
    			resp = new HttpResponse(500, "<html><title>Error</title><body>Problem authenticating</body></html>");    	
    		}
    	}
    	else
    	{
    		resp = new HttpResponse(500, "<html><title>Error</title><body>Authentication configuration missing</body></html>");
    	}
    	return resp;
	}	
	
	public String getLoginURL(String originalPath) {
		String url = loginUrl + "?client_id=" + clientId + "&response_type=code&scope=openid%20email&redirect_uri=" + publicHost + path + "&state=" + publicHost + originalPath + "&nonce=123";
		return url;
	}


}
