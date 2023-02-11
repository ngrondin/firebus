package io.firebus.adapters.http.auth;

import java.security.MessageDigest;
import java.util.Base64;

import org.apache.http.impl.client.CloseableHttpClient;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.adapters.http.HttpRequest;
import io.firebus.adapters.http.HttpResponse;
import io.firebus.adapters.http.handlers.AuthValidationHandler;
import io.firebus.data.DataMap;

public class UserPassValidator extends AuthValidationHandler
{
	protected String loginUrl;
	protected String dataService;
	protected String collection;
	protected String userKey;
	protected String passwordKey;
	protected String hashType;
	protected String redirectUrl;
	protected String cookieName;

	public UserPassValidator(Firebus f, DataMap c, CloseableHttpClient hc) 
	{
		super(f, c, hc);
		loginUrl = handlerConfig.getString("loginurl");
		dataService = handlerConfig.getString("dataservice");
		collection = handlerConfig.containsKey("collection") ? handlerConfig.getString("collection") : "user";
		userKey = handlerConfig.containsKey("userkey") ? handlerConfig.getString("userkey") : "username";
		passwordKey = handlerConfig.containsKey("passwordkey") ? handlerConfig.getString("passwordkey") : "passwordhash";
		hashType = handlerConfig.containsKey("hash") ? handlerConfig.getString("hash") : "SHA-256";
		redirectUrl = handlerConfig.getString("redirecturl");
		cookieName = handlerConfig.containsKey("cookie") ? handlerConfig.getString("cookie") : "token";
	}
    
	public String getLoginURL(String originalPath) {
		String url = loginUrl + "?redirect=" + publicHost + path + "&state=" + publicHost + originalPath;
		return url;
	}

	protected HttpResponse httpService(HttpRequest req) {
		HttpResponse resp = null;
    	//String contextPath = req.getPath();
    	//if(contextPath.equals(""))
    	//	contextPath = "/";

    	String username = req.getParameter("username");
    	String password = req.getParameter("password");
    	String redirectUrlResolved = redirectUrl != null ? redirectUrl : "${state}";
   		redirectUrlResolved = redirectUrlResolved.replace("${state}", req.getParameter("state") != null ? req.getParameter("state") : "");

    	if(firebus != null)
    	{
    		if(username != null && password != null)
    		{
    			try
    			{
	    			DataMap fbReq = new DataMap();
	    			fbReq.put("object", collection);
	    			fbReq.put("filter", new DataMap(userKey, username));
	    			Payload r = firebus.requestService(dataService, new Payload(fbReq.toString()));
	    			DataMap fbResp = new DataMap(r.getString());
	    			if(fbResp != null && fbResp.getList("result") != null)
	    			{
	    				if(fbResp.getList("result").size() > 0)
	    				{
		    				DataMap userConfig = fbResp.getList("result").getObject(0);
		    				String savedPassHash = userConfig.getString(passwordKey);
		    				MessageDigest digest = MessageDigest.getInstance(hashType);
		    				byte[] encodedhash = digest.digest(password.getBytes());
		    				String receivedPassHash = Base64.getEncoder().encodeToString(encodedhash);
		    				if(receivedPassHash.equals(savedPassHash)) 
		    				{
		    					resp = new HttpResponse(200, "<html><head><title>Redirect</title></head><meta http-equiv=\"refresh\" content=\"0; url = '" + redirectUrlResolved + "'\"><body>Loging in</body></html>");
		    					_securityHandler.enrichAuthenticatedHttpResponse(username, resp);
		    				}
		    				else
		    				{
		    					resp = new HttpResponse(401, "<html><title>Error</title><body>Unauthorized</body></html>");
		    				}
	    				}
	    				else
	    				{
	    					resp = new HttpResponse(401, "<html><title>Error</title><body>Unauthorized</body></html>");
	    				}
	    			}
	    			else
	    			{
	    				resp = new HttpResponse(500, "<html><title>Error</title><body>Data service not found</body></html>");
	    			}
	    		}
    			catch (Exception e) 
    			{
    				resp = new HttpResponse(500, "<html><title>Error</title><body>" + e.getMessage() + "</body></html>");
				}
    		}
    		else
    		{
    			resp = new HttpResponse(400, "<html><title>Error</title><body>Missing username of password</body></html>");
    		}
    	}
    	else
    	{
    		resp = new HttpResponse(500, "<html><title>Error</title><body>Firebus not configured on the handler</body></html>");
    	}
    	return resp;
	}	

}
