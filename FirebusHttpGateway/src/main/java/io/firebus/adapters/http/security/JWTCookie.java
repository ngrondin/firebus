package io.firebus.adapters.http.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.firebus.Payload;
import io.firebus.adapters.http.HttpGateway;
import io.firebus.adapters.http.IDMHandler;
import io.firebus.adapters.http.SecurityHandler;
import io.firebus.adapters.http.idm.OAuth2IDM;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;
import io.firebus.utils.jwt.JWTValidator;

public class JWTCookie extends SecurityHandler {
	protected String accessTokenCookieName;
	protected String refreshTokenCookieName;
	protected String fbMetadataName;
	protected JWTValidator jwtValidator;
	protected boolean secureCookies;
	
	public JWTCookie(HttpGateway gw, DataMap c) {
		super(gw, c);
		accessTokenCookieName = config.getString("accesstokencookie");
		refreshTokenCookieName = config.getString("refreshtokencookie");
		fbMetadataName = config.getString("fbmetaname");
		jwtValidator = new JWTValidator();
		secureCookies = this.httpGateway.getPublicHost().startsWith("https");
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
				if(jwtValidator.tryValidate(jwt)) {
					return true;
				}  else {
					sendNeedToRefreshResponse(req, resp, jwt.getIssuer());
					return false;						
				}
			}
		}
		sendUnauthenticatedResponse(req, resp);
		return false;	

	}

	public String extractRefreshToken(HttpServletRequest req) throws ServletException, IOException {
		return getCookie(req, refreshTokenCookieName);
	}

	public void enrichFirebusRequest(HttpServletRequest req, Payload payload) {
		String token = getCookie(req, accessTokenCookieName);
		payload.metadata.put(fbMetadataName, token);
	}

	public void enrichAuthResponse(HttpServletRequest req, HttpServletResponse resp, String accessToken, long expiry, String refreshToken, String refreshPath, String state) throws ServletException, IOException {
		setCookies(resp, accessToken, refreshToken, refreshPath);
		sendRedirectScript(resp, accessToken, expiry, refreshToken, refreshPath, state);
		Logger.info("fb.http.sec.jwtcooke.login", new DataMap());
	}

	public void enrichRefreshResponse(HttpServletRequest req, HttpServletResponse resp, String accessToken, long expiry, String refreshToken, String refreshPath, String state) throws ServletException, IOException {
		setCookies(resp, accessToken, refreshToken, refreshPath);
		if(acceptsFirst(req, "text/html")) {
			sendRedirectScript(resp, accessToken, expiry, refreshToken, refreshPath, state);		
		} else if(acceptsFirst(req, "application/json")) {
			sendJsonData(resp, accessToken, expiry, refreshToken, refreshPath, state);	
		}		
		Logger.info("fb.http.sec.jwtcooke.refresh", new DataMap());	
	}

	public void enrichLogoutResponse(HttpServletRequest req, HttpServletResponse resp) {
		Cookie cookie = new Cookie(accessTokenCookieName, "");
		cookie.setPath("/");
		cookie.setMaxAge(0);
		resp.addCookie(cookie);	
		for(IDMHandler idm: idmHandlers) {
			Cookie refreshCookie = new Cookie(refreshTokenCookieName, "");
			refreshCookie.setPath(idm.getRefreshUrl(null));
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
	
	protected void setCookies(HttpServletResponse resp, String accessToken, String refreshToken, String refreshPath)
	{
		setCookie(resp, accessTokenCookieName, accessToken, "/");
		setCookie(resp, refreshTokenCookieName, refreshToken, refreshPath);
	}
	
	protected void setCookie(HttpServletResponse resp, String name, String value, String path) {
		String str = name + "=" + value + "; HttpOnly; Path=" + path + "; SameSite=Lax; Max-Age=15724800; Secure=" + secureCookies;
		resp.addHeader("Set-Cookie", str);
	}
	
	protected void sendRedirectScript(HttpServletResponse resp, String accessToken, long expiry, String refreshToken, String refreshPath, String state) throws IOException {
		resp.setContentType("text/html");
		PrintWriter writer = resp.getWriter();
		writer.println("<html><body><script>");
		writer.println("localStorage.setItem('access_token', '" + accessToken + "')");
		writer.println("localStorage.setItem('expires_at', '" + expiry + "')");
		writer.println("localStorage.setItem('refresh_token', '" + refreshToken + "')");
		writer.println("localStorage.setItem('refresh_path', '" + refreshPath + "')");
		writer.println("window.location='" + state + "';");
		writer.println("</script></body></html>");
	}
	
	protected void sendJsonData(HttpServletResponse resp, String accessToken, long expiry, String refreshToken, String refreshPath, String state) throws IOException {
		resp.setContentType("application/json");
		PrintWriter writer = resp.getWriter();
		writer.println("{");	
		writer.println(" \"access_token\":\"" + accessToken + "\",");	
		writer.println(" \"expires_at\":\"" + expiry + "\",");	
		writer.println(" \"refresh_token\":\"" + refreshToken + "\",");	
		writer.println(" \"refresh_path\":\"" + refreshPath + "\"");	
		writer.println("}");	
	}
	
	
}
