package io.firebus.adapters.http.auth;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import io.firebus.Firebus;
import io.firebus.utils.DataException;
import io.firebus.utils.DataMap;

public class OAuth2CodeValidator extends AuthValidationHandler
{
	private static final long serialVersionUID = 1L;
	protected String tokenUrl;
	protected String clientId;
	protected String clientSecret;
	protected String thisUrl;
	protected String redirectUrl;
	protected String cookieName;

	public OAuth2CodeValidator(DataMap c, Firebus fb) 
	{
		super(c, fb);
		tokenUrl = handlerConfig.getString("tokenurl");
		clientId = handlerConfig.getString("clientid");
		clientSecret = handlerConfig.getString("clientsecret");
		thisUrl = handlerConfig.getString("thisurl");
		redirectUrl = handlerConfig.getString("redirecturl");
		cookieName = handlerConfig.getString("cookie");
	}

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)  throws ServletException, IOException 
    {
    	if(tokenUrl != null && clientId != null && clientSecret != null)
    	{
        	String code = req.getParameter("code");
        	String contextPath = req.getContextPath();
        	if(contextPath.equals(""))
        		contextPath = "/";
        	String thisUrlResolved = thisUrl != null ? thisUrl : req.getRequestURL().toString();
        	String redirectUrlResolved = redirectUrl != null ? redirectUrl : "${state}";
       		redirectUrlResolved = redirectUrlResolved.replace("${state}", req.getParameter("state") != null ? req.getParameter("state") : "");
        	
        	if(code != null && redirectUrlResolved != null)
        	{
        		DataMap respMap = null;
        		HttpClient httpclient = HttpClients.createDefault();
        		HttpPost httppost = new HttpPost(tokenUrl);
        		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        		params.add(new BasicNameValuePair("code", code));
        		params.add(new BasicNameValuePair("client_id", clientId));
        		params.add(new BasicNameValuePair("client_secret", clientSecret));
        		params.add(new BasicNameValuePair("redirect_uri", thisUrlResolved));
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
            			if(cookieName != null)
            			{
                			Cookie cookie = new Cookie(cookieName, respMap.getString("id_token"));
                			cookie.setPath(contextPath);
                			cookie.setMaxAge(3600);
                			resp.addCookie(cookie);
            			}
            			resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
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
}
