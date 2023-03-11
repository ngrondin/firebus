package io.firebus.adapters.http.auth;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
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
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.ECDSAKeyProvider;

import io.firebus.Firebus;
import io.firebus.adapters.http.AuthValidationHandler;
import io.firebus.adapters.http.HttpGateway;
import io.firebus.data.DataException;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;

//TODO: Can all be generalised into the oAuth2CodeValidator
public class AppleValidator extends AuthValidationHandler {
	protected String loginUrl;
	protected String tokenUrl;
	protected String clientId;
	//protected String clientSecret;
	protected String keyId;
	protected String privateKey;
	protected String redirectUrl;

	public AppleValidator(HttpGateway gw, Firebus f, DataMap c) {
		super(gw, f, c);
		loginUrl = handlerConfig.getString("loginurl");
		tokenUrl = handlerConfig.getString("tokenurl");
		clientId = handlerConfig.getString("clientid");
		keyId = handlerConfig.getString("keyid");
		privateKey = handlerConfig.getString("privatekey");
		//clientSecret = getClientSecret(clientId);
		redirectUrl = handlerConfig.getString("redirecturl");
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
	
	
    protected void httpService(HttpServletRequest req, HttpServletResponse resp)  throws ServletException, IOException 
    {
    	if(tokenUrl != null && clientId != null)
    	{
    		try {
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
	    		if(code != null) {
	            	String redirectUrlResolved = redirectUrl != null ? redirectUrl : "${state}";
	           		redirectUrlResolved = redirectUrlResolved.replace("${state}", state != null ? state : "");

	        		DataMap respMap = null;
	        		HttpClient httpclient = httpGateway.getHttpClient();
	        		HttpPost httppost = new HttpPost(tokenUrl);
	        		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
	        		params.add(new BasicNameValuePair("code", code));
	        		params.add(new BasicNameValuePair("client_id", clientId));
	        		params.add(new BasicNameValuePair("client_secret", getClientSecret(clientId)));
	        		params.add(new BasicNameValuePair("redirect_uri", publicHost + path));
	        		params.add(new BasicNameValuePair("grant_type", "authorization_code"));
	        		httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
	        		httppost.setHeader("content-type", "application/x-www-form-urlencoded");
	        		HttpResponse response = httpclient.execute(httppost);
	        		int respStatus = response.getStatusLine().getStatusCode(); 
	        		HttpEntity entity = response.getEntity();
	        		if (entity != null) 
	        		{
	        			is = entity.getContent();
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
	        		}	    		} else {
	    			
	    		}
    		} catch(Exception e) {
    			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    	        PrintWriter writer = resp.getWriter();
    	        writer.println("<html><title>Error</title><body>Problem authenticating</body></html>");
    			
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
		String url = loginUrl + "?client_id=" + clientId + "&response_type=code&response_mode=form_post&scope=name%20email&redirect_uri=" + publicHost + path + "&state=" + publicHost + originalPath + "&nonce=123";
		return url;
	}	

}
