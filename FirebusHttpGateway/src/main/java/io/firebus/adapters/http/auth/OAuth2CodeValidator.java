package io.firebus.adapters.http.auth;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.firebus.Firebus;
import io.firebus.adapters.http.AuthValidationHandler;
import io.firebus.adapters.http.HttpGateway;
import io.firebus.utils.DataException;
import io.firebus.utils.DataMap;

public class OAuth2CodeValidator extends AuthValidationHandler
{
	protected String loginUrl;
	protected String tokenUrl;
	protected String clientId;
	protected String clientSecret;
	protected String redirectUrl;

	public OAuth2CodeValidator(HttpGateway gw, Firebus f, DataMap c) 
	{
		super(gw, f, c);
		loginUrl = handlerConfig.getString("loginurl");
		tokenUrl = handlerConfig.getString("tokenurl");
		clientId = handlerConfig.getString("clientid");
		clientSecret = handlerConfig.getString("clientsecret");
		redirectUrl = handlerConfig.getString("redirecturl");
	}

    protected void httpService(HttpServletRequest req, HttpServletResponse resp)  throws ServletException, IOException 
    {
    	if(tokenUrl != null && clientId != null && clientSecret != null)
    	{
        	String code = req.getParameter("code");
        	String contextPath = req.getContextPath();
        	if(contextPath.equals(""))
        		contextPath = "/";
        	String redirectUrlResolved = redirectUrl != null ? redirectUrl : "${state}";
       		redirectUrlResolved = redirectUrlResolved.replace("${state}", req.getParameter("state") != null ? req.getParameter("state") : "");
        	
        	if(code != null && redirectUrlResolved != null)
        	{
        		DataMap respMap = null;
        		HttpClient httpclient = httpGateway.getHttpClient();
        		HttpPost httppost = new HttpPost(tokenUrl);
        		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        		params.add(new BasicNameValuePair("code", code));
        		params.add(new BasicNameValuePair("client_id", clientId));
        		params.add(new BasicNameValuePair("client_secret", clientSecret));
        		params.add(new BasicNameValuePair("redirect_uri", publicHost + path));
        		params.add(new BasicNameValuePair("grant_type", "authorization_code"));
        		httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        		HttpResponse response = httpclient.execute(httppost);
        		int respStatus = response.getStatusLine().getStatusCode(); 
        		HttpEntity entity = response.getEntity();
        		if (entity != null) 
        		{
        			InputStream is = entity.getContent();
        			try { respMap = new DataMap(is); }
        			catch(DataException e) {}
        		}
        		if(respStatus >= 200 && respStatus < 400)
        		{
            		if (respMap != null) 
            		{
            			DecodedJWT jwt = JWT.decode(respMap.getString("id_token"));
            			Claim usernameClaim = jwt.getClaim("email");
            			String username = usernameClaim.asString();
            			_securityHandler.enrichAuthResponse(username, resp);
            			resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
            			resp.setHeader("location", redirectUrlResolved);		
            	        PrintWriter writer = resp.getWriter();
            	        writer.println("<html><title>Redirect</title><body>Loging in</body></html>");
            		}
            		else
            		{
            			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            	        PrintWriter writer = resp.getWriter();
            	        writer.println("<html><title>Error</title><body>Token is empty</body></html>");
            		}
        		}
        		else
        		{
            		if (respMap != null) 
            		{
            			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            	        PrintWriter writer = resp.getWriter();
            	        writer.println("<html><title>Error</title><body>Return code : " + respStatus + "<br>" + respMap.toString() + "</body></html>");
            		}
            		else
            		{
            			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            	        PrintWriter writer = resp.getWriter();
            	        writer.println("<html><title>Error</title><body>Return code : " + respStatus + "</body></html>");
            		}
        		}
        	}
    	}
    	else
    	{
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        PrintWriter writer = resp.getWriter();
	        writer.println("<html><title>Error</title><body>Authentication configuration missing</body></html>");
    	}
    }

	public String getLoginURL(String originalPath) {
		String url = loginUrl + "?client_id=" + clientId + "&response_type=code&scope=openid%20email&redirect_uri=" + publicHost + path + "&state=" + publicHost + originalPath + "&nonce=123";
		return url;
	}	
}
