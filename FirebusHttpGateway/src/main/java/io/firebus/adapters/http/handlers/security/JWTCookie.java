package io.firebus.adapters.http.handlers.security;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.firebus.Payload;
import io.firebus.adapters.http.HttpRequest;
import io.firebus.adapters.http.HttpResponse;
import io.firebus.adapters.http.handlers.SecurityHandler;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;

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
	protected SimpleDateFormat sdf;

	public JWTCookie(DataMap c, CloseableHttpClient hc) {
		super(c, hc);
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
		sdf = new SimpleDateFormat("EEE, DD-MMM-YYYY HH:MM:SS Z");
	}

	public boolean checkAndEnrichHttpRequest(HttpRequest req) {
		String token = getTokenFromRequest(req);
		if(token != null) {
			DecodedJWT jwt = JWT.decode(token);
			String issuer = jwt.getIssuer();
			String email = jwt.getClaim("email").asString();
			long expiresAt = jwt.getExpiresAt().getTime();
			long now = System.currentTimeMillis();
			if(expiresAt > now && issuer.equals(jwtIssuer)) {
				req.setSecurityData("token", token);
				req.setSecurityData("email", email);
				req.setSecurityData("expiry", expiresAt);
				//if(expiresAt < (now + (timeout / 2)))
				//	setTokenOnResponse(jwt.getClaim("email").asString(), resp);
				return true;
			}
		}
		//unauthenticated(req, resp);
		return false;
	}

	public void enrichFirebusRequest(HttpRequest req, Payload payload) {
		String token = getTokenFromRequest(req);
		payload.metadata.put(fbMetadataName, token);
	}

	public void enrichAuthenticatedHttpResponse(String username, HttpResponse resp) {
		setTokenOnResponse(username, resp);
	}

	protected String getTokenFromRequest(HttpRequest req)
	{
		String token = null;
		if(cookieName != null)
		{
			List<String> cookies = req.getHeader("Cookie");
			if(cookies != null) {
				for(String cookieStr : cookies) {
					String[] parts = cookieStr.split(";");
					for(String part : parts) {
						String[] subparts = part.split("=");
						if(subparts[0].equals(cookieName)) {
							token = subparts[1];
						}
					}				
				}
			}
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
				HttpPost httppost = new HttpPost(idmUrl);
	    		httppost.setHeader("Content-Type", "application/json");
	    		DataMap req = new DataMap();
	    		req.put("client_id", idmClientId);
	    		req.put("client_secret", idmClientSecret);
	    		req.put("user", username);
	    		httppost.setEntity(new StringEntity(req.toString(), "UTF-8"));
	    		CloseableHttpResponse response = httpClient.execute(httppost);
	    		try {
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
	    		} finally {
	    			response.close();
	    		}
			} catch(Exception e) { } 
		}
		
	    String token = tokenBuilder.sign(algorithm);
	    return token;
	}
	
	protected void setTokenOnResponse(String username, HttpResponse resp)
	{
		if(cookieName != null)
		{
			String token = generateToken(username);
			Date expiry = new Date((new Date()).getTime() + timeout);
			setCookieOnResponse(resp, cookieName, token, expiry, cookieDomain, "/");
		}		
	}

	public void enrichLogoutResponse(HttpResponse resp) {
		setCookieOnResponse(resp, cookieName, "", new Date(), cookieDomain, "/");
	}
	
	public void setCookieOnResponse(HttpResponse resp, String name, String value, Date expiry, String domain, String path ) {
		String cookieStr = name + "=" + value;
		cookieStr += "; expires=" + sdf.format(expiry);
		if(cookieDomain != null) {
			cookieStr += "; domain=" + domain;
		}
		cookieStr += "; path=/";
		resp.setHeader("Set-Cookie", cookieStr);
	}


}
