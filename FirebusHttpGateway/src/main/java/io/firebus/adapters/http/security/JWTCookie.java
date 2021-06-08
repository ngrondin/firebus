package io.firebus.adapters.http.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.firebus.Payload;
import io.firebus.adapters.http.HttpGateway;
import io.firebus.adapters.http.SecurityHandler;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

public class JWTCookie extends SecurityHandler {
	protected String cookieName;
	protected String cookieDomain;
	protected String fbMetadataName;
	protected String jwtSecret;
	protected String jwtIssuer;
	protected String idmUrl;
	protected String idmClientId;
	protected String idmClientSecret;	
	protected long timeout;

	public JWTCookie(HttpGateway gw, DataMap c) {
		super(gw, c);
		cookieName = config.getString("cookie");
		cookieDomain = config.getString("cookiedomain");
		fbMetadataName = config.getString("fbmetaname");
		jwtSecret = config.getString("jwtsecret");
		jwtIssuer = config.getString("jwtissuer");
		idmUrl = config.getString("idmurl");
		idmClientId = config.getString("idmclientid");
		idmClientSecret = config.getString("idmclientsecret");
		if(config.containsKey("timeout")) {
			timeout = config.getNumber("timeout").longValue();
		} else {
			timeout = 3600000;
		}
		if(cookieDomain != null && cookieDomain.equals(""))
			cookieDomain = null;
	}

	public boolean checkHttpRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String token = getTokenFromRequest(req);
		if(token != null) {
			DecodedJWT jwt = JWT.decode(token);
			String issuer = jwt.getIssuer();
			long expiresAt = jwt.getExpiresAt().getTime();
			long now = System.currentTimeMillis();
			if(expiresAt > now && issuer.equals(jwtIssuer)) {
				if(expiresAt < (now + (timeout / 2)))
					setTokenOnResponse(jwt.getClaim("email").asString(), resp);
				return true;
			}
		}
		unauthenticated(req, resp);
		return false;
	}

	public void enrichFirebusRequest(HttpServletRequest req, Payload payload) {
		String token = getTokenFromRequest(req);
		payload.metadata.put(fbMetadataName, token);
	}

	public void enrichAuthResponse(String username, HttpServletResponse resp) {
		setTokenOnResponse(username, resp);
	}

	protected String getTokenFromRequest(HttpServletRequest req)
	{
		String token = null;
		if(cookieName != null)
		{
			Cookie[] cookies = req.getCookies();
			if(cookies != null)
				for (int i = 0; i < cookies.length; i++) 
					if(cookies[i].getName().equals(cookieName))
						token = cookies[i].getValue();
		}
		return token;
	}
	
	protected String generateToken(String username) 
	{
	    Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
	    JWTCreator.Builder tokenBuilder = JWT.create()
	    		.withIssuer(jwtIssuer)
	    		.withClaim("email", username)
	    		.withExpiresAt(new Date((new Date()).getTime() + timeout));
	    
		if(idmUrl != null && idmClientId != null && idmClientSecret != null) {
			try {
	    		HttpClient httpclient = httpGateway.getHttpClient();
	    		HttpPost httppost = new HttpPost(idmUrl);
	    		httppost.setHeader("Content-Type", "application/json");
	    		DataMap req = new DataMap();
	    		req.put("client_id", idmClientId);
	    		req.put("client_secret", idmClientSecret);
	    		req.put("user", username);
	    		httppost.setEntity(new StringEntity(req.toString(), "UTF-8"));
	    		HttpResponse response = httpclient.execute(httppost);
	    		int respStatus = response.getStatusLine().getStatusCode(); 
	    		DataMap respMap = null;
	    		if(respStatus == 200) {
	        		HttpEntity entity = response.getEntity();
	        		if (entity != null) 
	        		{
	        			respMap = new DataMap(entity.getContent()); 
	        			DataList roles = respMap.getList("roles");
	        			if(roles != null && roles.size() > 0) {
	        				String[] rolesArray = new String[roles.size()];
	        				for(int i = 0; i < roles.size(); i++) 
	        					rolesArray[i] = roles.getString(i);
	    					tokenBuilder.withArrayClaim("roles", rolesArray);
	        			}
	        			DataList domains = respMap.getList("domains");
	        			if(domains != null && domains.size() > 0) {
	        				String[] domainArray = new String[domains.size()];
	        				for(int i = 0; i < domains.size(); i++) 
	        					domainArray[i] = domains.getString(i);
	    					tokenBuilder.withArrayClaim("domains", domainArray);
	        			}
	        		}
	    			
	    		}
			} catch(Exception e) { }
		}
		
	    String token = tokenBuilder.sign(algorithm);
	    return token;
	}
	
	protected void setTokenOnResponse(String username, HttpServletResponse resp)
	{
		if(cookieName != null)
		{
			Cookie cookie = new Cookie(cookieName, generateToken(username));
			cookie.setPath("/");
			cookie.setMaxAge((int)(timeout / 1000));
			if(cookieDomain != null) 
				cookie.setDomain(cookieDomain);
			resp.addCookie(cookie);
		}		
	}

	public void enrichLogoutResponse(HttpServletResponse resp) {
		Cookie cookie = new Cookie(cookieName, "");
		cookie.setPath("/");
		cookie.setMaxAge(0);
		if(cookieDomain != null) 
			cookie.setDomain(cookieDomain);
		resp.addCookie(cookie);
		
	}
}
