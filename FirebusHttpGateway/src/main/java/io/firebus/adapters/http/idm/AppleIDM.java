package io.firebus.adapters.http.idm;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.ECDSAKeyProvider;

import io.firebus.Firebus;
import io.firebus.adapters.http.FirebusHttpException;
import io.firebus.adapters.http.HttpGateway;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;

//TODO: Can all be generalised into the oAuth2CodeValidator
public class AppleIDM extends OAuth2IDM {
	protected String keyId;
	protected String privateKey;

	public AppleIDM(HttpGateway gw, Firebus f, DataMap c) {
		super(gw, f, c);
		keyId = handlerConfig.getString("keyid");
		privateKey = handlerConfig.getString("privatekey");
	}
	
	protected String getClientSecret(String clientId) {
		String clientSecret = null;
		try {
			long expiry = 28800000;
			byte[] pkcs8EncodedKey = Base64.getDecoder().decode(privateKey);
			KeyFactory factory = KeyFactory.getInstance("EC");
			final ECPrivateKey privateKey = (ECPrivateKey)factory.generatePrivate(new PKCS8EncodedKeySpec(pkcs8EncodedKey));
		    	    
		    Algorithm algorithm = Algorithm.ECDSA256(new ECDSAKeyProvider() {
				public ECPublicKey getPublicKeyById(String keyId) { return null; }
				public ECPrivateKey getPrivateKey() {return privateKey;}
				public String getPrivateKeyId() {return keyId;}
			});
		    clientSecret = JWT.create()
		    		.withIssuer("826685XAPC")
		    		.withIssuedAt(new Date())
		    		.withAudience("https://appleid.apple.com")
		    		.withSubject(clientId)
		    		.withExpiresAt(new Date((new Date()).getTime() + expiry))
		    		.sign(algorithm);
		} catch(Exception e) {
			Logger.severe("fb.auth.applevalidator.gensecret", e);
		}	
		return clientSecret;
	}
	
	
    public void codeService(HttpServletRequest req, HttpServletResponse resp) throws Exception {
    	InputStream is = req.getInputStream();
		StringBuilder sb = new StringBuilder();
		byte[] bytes = new byte[1024];
		int len = 0;
		while((len = is.read(bytes)) > -1) {
			sb.append(new String(bytes, 0, len));
		}
		String result = java.net.URLDecoder.decode(sb.toString(), StandardCharsets.UTF_8.name());
		String[] parts = result.split("&");
		String state = null;
		String code = null;
		for(int i = 0; i < parts.length; i++) {
			if(parts[i].startsWith("state=")) {
				state = parts[i].substring(6);
			} else if(parts[i].startsWith("code=")) {
				code = parts[i].substring(5);
			}  			
		}
    	if(code == null) throw new FirebusHttpException("Missing code in authorization code request", 400, null);
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("code", code));
		params.add(new BasicNameValuePair("client_id", clientId));
		params.add(new BasicNameValuePair("client_secret", getClientSecret(clientId)));
		params.add(new BasicNameValuePair("redirect_uri", getCodeURL()));
		params.add(new BasicNameValuePair("grant_type", "authorization_code"));    		
		DataMap respMap = callTokenUrl(params);
		String accessToken = respMap.getString("access_token");
		String refreshToken = respMap.getString("refresh_token");
		long expiry = (new Date()).getTime() + (respMap.getNumber("expires_in").longValue() * 1000);
		_securityHandler.enrichAuthResponse(req, resp, accessToken, expiry, refreshToken, getRefreshUrl(null), state);
    }
    
    public void refreshService(HttpServletRequest req, HttpServletResponse resp) throws Exception {
    	String refreshToken = _securityHandler.extractRefreshToken(req);
    	if(refreshToken == null) throw new FirebusHttpException("Missing refresh token in refresh request", 400, null);
    	String state = req.getParameter("state");
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("refresh_token", refreshToken));
		params.add(new BasicNameValuePair("client_id", clientId));
		params.add(new BasicNameValuePair("client_secret", getClientSecret(clientId)));
		params.add(new BasicNameValuePair("redirect_uri", getCodeURL()));
		params.add(new BasicNameValuePair("grant_type", "refresh_token"));
		DataMap respMap = callTokenUrl(params);
		String accessToken = respMap.getString("access_token");
		String newRefreshToken = respMap.getString("refresh_token");
		long expiry = (new Date()).getTime() + (respMap.getNumber("expires_in").longValue() * 1000);
		_securityHandler.enrichRefreshResponse(req, resp, accessToken, expiry, newRefreshToken, getRefreshUrl(null), state); 	
    }
    

	public String getLoginURL(String originalPath) {
		String originalUrl = httpGateway.getPublicHost() + originalPath;
		long nonce = (int)(Math.random() * 1000000);
		String url = loginUrl + "?client_id=" + clientId + "&response_type=code&response_mode=form_post&scope=name%20email&redirect_uri=" + getCodeURL() + "&state=" + originalUrl + "&nonce=" + nonce;
		return url;
	}	

}
