package io.firebus.adapters.http.auth;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.adapters.http.AuthValidationHandler;
import io.firebus.utils.DataMap;

public class UserPassValidator extends AuthValidationHandler
{
	protected String dataService;
	protected String collection;
	protected String userKey;
	protected String passwordKey;
	protected String hashType;
	protected String jwtSecret;
	protected String redirectUrl;
	protected String cookieName;

	public UserPassValidator(DataMap c, Firebus fb) 
	{
		super(c, fb);
		dataService = handlerConfig.getString("dataservice");
		collection = handlerConfig.containsKey("collection") ? handlerConfig.getString("collection") : "user";
		userKey = handlerConfig.containsKey("userkey") ? handlerConfig.getString("userkey") : "username";
		passwordKey = handlerConfig.containsKey("passwordkey") ? handlerConfig.getString("passwordkey") : "passwordhash";
		hashType = handlerConfig.containsKey("hash") ? handlerConfig.getString("hash") : "SHA-256";
		jwtSecret = handlerConfig.getString("jwtsecret");
		redirectUrl = handlerConfig.getString("redirecturl");
		cookieName = handlerConfig.containsKey("cookie") ? handlerConfig.getString("cookie") : "token";
	}

    protected void httpService(String tokenStr, HttpServletRequest req, HttpServletResponse resp)  throws ServletException, IOException 
    {
    	String contextPath = req.getContextPath();
    	if(contextPath.equals(""))
    		contextPath = "/";

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
		            			long expiry = 28800000;
		    				    Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
		    				    String token = JWT.create()
		    				    		.withIssuer("io.firebus.http")
		    				    		.withClaim("email", username)
		    				    		.withExpiresAt(new Date((new Date()).getTime() + expiry))
		    				    		.sign(algorithm);
	                			Cookie cookie = new Cookie(cookieName, token);
	                			cookie.setPath(contextPath);
	                			cookie.setMaxAge(3600);
	                			resp.addCookie(cookie);
		            			resp.setStatus(HttpServletResponse.SC_OK);
		            	        PrintWriter writer = resp.getWriter();
		            	        writer.println("<html><head><title>Redirect</title></head><meta http-equiv=\"refresh\" content=\"0; url = '" + redirectUrlResolved + "'\"><body>Loging in</body></html>");
	
		    				}
		    				else
		    				{
			    				resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			    		        PrintWriter writer = resp.getWriter();
			    		        writer.println("<html><title>Error</title><body>Unauthorized</body></html>");
		    				}
	    				}
	    				else
	    				{
		    				resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		    		        PrintWriter writer = resp.getWriter();
		    		        writer.println("<html><title>Error</title><body>Unauthorized</body></html>");
	    				}
	    			}
	    			else
	    			{
	    				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    		        PrintWriter writer = resp.getWriter();
	    		        writer.println("<html><title>Error</title><body>Data service not found</body></html>");
	    			}
	    		}
    			catch (Exception e) 
    			{
    				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    		        PrintWriter writer = resp.getWriter();
    		        writer.println("<html><title>Error</title><body>" + e.getMessage() + "</body></html>");
				}
    		}
    		else
    		{
    			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	        PrintWriter writer = resp.getWriter();
    	        writer.println("<html><title>Error</title><body>Missing username of password</body></html>");
    		}
    	}
    	else
    	{
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        PrintWriter writer = resp.getWriter();
	        writer.println("<html><title>Error</title><body>Firebus not configured on the handler</body></html>");
    	}
    }	
}
