package io.firebus.adapters.http.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.interfaces.DecodedJWT;

import io.firebus.Payload;
import io.firebus.adapters.http.HttpGateway;
import io.firebus.adapters.http.IDMHandler;
import io.firebus.adapters.http.SecurityHandler;
import io.firebus.adapters.http.Utils;
import io.firebus.adapters.http.idm.OAuth2IDM;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.firebus.utils.jwt.JWTValidator;
import io.firebus.utils.jwt.JWTValidatorException;

public class JWTCookie extends SecurityHandler {
	protected String accessTokenCookieName;
	protected String refreshTokenCookieName;
	protected String fbMetadataName;
	protected JWTValidator jwtValidator;
	
	public JWTCookie(HttpGateway gw, DataMap c) {
		super(gw, c);
		accessTokenCookieName = config.getString("accesstokencookie");
		refreshTokenCookieName = config.getString("refreshtokencookie");
		fbMetadataName = config.getString("fbmetaname");
		jwtValidator = new JWTValidator();
	}
	
	public void addIDMHandler(IDMHandler avh) {
		super.addIDMHandler(avh);
		if(avh instanceof OAuth2IDM) {
			OAuth2IDM idm = (OAuth2IDM) avh;
			try { 
				String issuer = idm.getUri();
				String sharedSecret = idm.getJWTSecret();
				DataMap jwk = idm.getJWKData();
				if(sharedSecret != null) jwtValidator.addSharedSecret(issuer, sharedSecret);
				if(jwk != null) jwtValidator.addJWK(issuer, jwk);
			} catch(Exception e) {
				Logger.severe("fb.http.sec.jwtcookie.addidm", e);
			}
		}
	}

	public boolean checkHttpRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String accessToken = getCookie(req, accessTokenCookieName);
		if(accessToken != null) {
			DecodedJWT jwt = jwtValidator.tryDecode(accessToken);
			if(jwt != null) {
				try {
					jwtValidator.validate(jwt);
					return true;
				} catch(JWTValidatorException e) {
					Logger.security("fb.http.sec.jwtcooke.check", new DataMap("expired", e.expired, "badsig", e.badSignature, "badalg", e.badAlgorithm));
					if(e.expired)
						sendNeedToRefreshResponse(req, resp, jwt.getIssuer());
					else 
						sendUnauthenticatedResponse(req, resp);
					return false;							
				}
			} else {
				Logger.security("fb.http.sec.jwtcooke.check", "Token can't be decoded", null);
				sendUnauthenticatedResponse(req, resp);
				return false;				
			}
		} else {
			Logger.security("fb.http.sec.jwtcooke.check", "No token provided", null);
			sendUnauthenticatedResponse(req, resp);
			return false;
		}
	}

	public String extractRefreshToken(HttpServletRequest req) throws ServletException, IOException {
		return getCookie(req, refreshTokenCookieName);
	}

	public void enrichFirebusRequest(HttpServletRequest req, Payload payload) {
		String token = getCookie(req, accessTokenCookieName);
		payload.metadata.put(fbMetadataName, token);
	}

	public void sendAuthResponse(HttpServletRequest req, HttpServletResponse resp, String accessToken, long expiry, String refreshToken, String refreshPath, String state) throws ServletException, IOException {
		setCookies(req, resp, accessToken, refreshToken, refreshPath);
		resp.sendRedirect(state);
		Logger.info("fb.http.sec.jwtcooke.login", new DataMap());
	}

	public void sendRefreshResponse(HttpServletRequest req, HttpServletResponse resp, String accessToken, long expiry, String refreshToken, String refreshPath, String state) throws ServletException, IOException {
		setCookies(req, resp, accessToken, refreshToken, refreshPath);
		if(Utils.acceptsFirst(req, "text/html")) {
			resp.sendRedirect(state);
		} else if(Utils.acceptsFirst(req, "application/json")) {
			DataMap data = new DataMap("access_token", accessToken, "expires_at", expiry, "refresh_path", refreshPath);
			PrintWriter writer = resp.getWriter();
			writer.println(data.toString(true));				
		}		
		Logger.info("fb.http.sec.jwtcooke.refresh", new DataMap());	
	}
	
	public DataMap getCheckData(HttpServletRequest req) throws ServletException, IOException {
		DataMap data = null;
		String token = getCookie(req, accessTokenCookieName);
		if(token != null) {
			data = new DataMap();
			data.put("access_token", token);
			DecodedJWT jwt = jwtValidator.tryDecode(token);
			Date expires = jwt.getExpiresAt();
			data.put("expires_at", expires.getTime());
			for(IDMHandler idm: this.idmHandlers) { 
				if(idm.getUri().equals(jwt.getIssuer()))
					data.put("refresh_path", idm.getRefreshPath(req, null));
			}
		} 
		return data;
	}
	
	public void logout(HttpServletRequest req) throws ServletException, IOException {
		
	}
	

	public void enrichLogoutResponse(HttpServletRequest req, HttpServletResponse resp) {
		Cookie cookie = new Cookie(accessTokenCookieName, "");
		cookie.setPath("/");
		cookie.setMaxAge(0);
		resp.addCookie(cookie);	
		for(IDMHandler idm: idmHandlers) {
			Cookie refreshCookie = new Cookie(refreshTokenCookieName, "");
			refreshCookie.setPath(idm.getRefreshPath(req, null));
			refreshCookie.setMaxAge(0);
			resp.addCookie(refreshCookie);			
		}
	}
	


	protected String getCookie(HttpServletRequest req, String cookieName) {
		if(cookieName != null) {
			Cookie[] cookies = req.getCookies();
			if(cookies != null)
				for (int i = 0; i < cookies.length; i++) 
					if(cookies[i].getName().equals(cookieName))
						return cookies[i].getValue();
		}
		return null;
	}	
	
	protected void setCookies(HttpServletRequest req, HttpServletResponse resp, String accessToken, String refreshToken, String refreshPath)
	{
		setCookie(req, resp, accessTokenCookieName, accessToken, "/");
		setCookie(req, resp, refreshTokenCookieName, refreshToken, refreshPath);
	}
	
	protected void setCookie(HttpServletRequest req, HttpServletResponse resp, String name, String value, String path) {
		boolean secureCookies = req.getScheme().equals("https");
		String str = name + "=" + value + "; HttpOnly; Path=" + path + "; SameSite=Lax; Max-Age=15724800; Secure=" + secureCookies;
		resp.addHeader("Set-Cookie", str);
	}

}
