package io.firebus.adapters.http.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.firebus.Payload;
import io.firebus.adapters.http.SecurityHandler;
import io.firebus.utils.DataMap;

public class JWTCookie extends SecurityHandler {
	protected String cookieName;
	protected String fbMetadataName;
	protected String jwtSecret;
	protected String jwtIssuer;
	protected long timeout;

	public JWTCookie(DataMap c) {
		super(c);
		cookieName = config.getString("cookie");
		fbMetadataName = config.getString("fbmetaname");
		jwtSecret = config.getString("jwtsecret");
		jwtIssuer = config.getString("jwtissuer");
		if(config.containsKey("timeout")) {
			timeout = config.getNumber("timeout").longValue();
		} else {
			timeout = 3600000;
		}
	}

	public boolean checkHttpRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String token = getToken(req);
		if(token != null) {
			DecodedJWT jwt = JWT.decode(token);
			String issuer = jwt.getIssuer();
			long expiresAt = jwt.getExpiresAt().getTime();
			long now = System.currentTimeMillis();
			if(expiresAt > now && issuer.equals(jwtIssuer)) {
				if(expiresAt < (now + (timeout / 2)))
					setToken(jwt.getClaim("email").asString(), resp);
				return true;
			}
		}
		unauthenticated(req, resp);
		return false;
	}

	public void enrichFirebusRequest(HttpServletRequest req, Payload payload) {
		String token = getToken(req);
		payload.metadata.put(fbMetadataName, token);
	}

	public void enrichAuthResponse(String username, HttpServletResponse resp) {
		setToken(username, resp);
	}

	protected String getToken(HttpServletRequest req)
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
	
	protected void setToken(String username, HttpServletResponse resp)
	{
	    Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
	    String token = JWT.create()
	    		.withIssuer(jwtIssuer)
	    		.withClaim("email", username)
	    		.withExpiresAt(new Date((new Date()).getTime() + timeout))
	    		.sign(algorithm);

		if(cookieName != null)
		{
			Cookie cookie = new Cookie(cookieName, token);
			cookie.setPath("/");
			cookie.setMaxAge((int)(timeout / 1000));
			resp.addCookie(cookie);
		}		
	}
}
