package io.firebus.utils.jwt;

import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;

public class JWTValidatorEntry {
	protected String issuer;
	protected String keyId;
	protected String algoName;
	protected String sharedSecret;
	protected PublicKey publicKey;
	protected Algorithm algorithm;
	protected JWTVerifier verifier;
	
	public JWTValidatorEntry(String is, String id, String a, String s) throws JWTValidatorException {
		issuer = is;
		keyId = id;
		algoName = a;
		sharedSecret = s;
		setup();
	}
	
	public JWTValidatorEntry(String is, String id, String a, PublicKey k) throws JWTValidatorException {
		issuer = is;
		keyId = id;
		algoName = a;
		publicKey = k;
		if(algoName.startsWith("RS") && !(k instanceof RSAPublicKey)) throw new JWTValidatorException("Algorithm and Key type mismatch");
		if(algoName.startsWith("ES") && !(k instanceof ECPublicKey)) throw new JWTValidatorException("Algorithm and Key type mismatch");
		setup();
	}
	
	protected void setup() {
		switch(algoName) {
		case "HS256": algorithm = Algorithm.HMAC256(sharedSecret);
			break;
		case "HS384": algorithm = Algorithm.HMAC256(sharedSecret);
			break;
		case "HS512": algorithm = Algorithm.HMAC256(sharedSecret);
			break;
		case "RS256": algorithm = Algorithm.RSA256((RSAPublicKey)publicKey, null);
			break;
		case "RS384": algorithm = Algorithm.RSA384((RSAPublicKey)publicKey, null);
			break;
		case "RS512": algorithm = Algorithm.RSA512((RSAPublicKey)publicKey, null);
			break;
		case "ES256": algorithm = Algorithm.ECDSA256((ECPublicKey)publicKey, null);
			break;
		case "ES384": algorithm = Algorithm.ECDSA384((ECPublicKey)publicKey, null);
			break;
		case "ES512": algorithm = Algorithm.ECDSA512((ECPublicKey)publicKey, null);
			break;
		}
		
		Verification builder = JWT.require(algorithm);
		if(issuer != null) builder.withIssuer(issuer);
		if(keyId != null) builder.withClaim("kid", keyId);
		verifier = builder.build();
	}
	
	public boolean matches(String is, String id, String a) {
		boolean match = true;
		if(is != null && issuer != null && !is.equals(issuer)) match = false;
		if(id != null && keyId != null && !id.equals(keyId)) match = false;
		if(a != null && algoName != null && !a.equals(algoName)) match = false;
		return match;
	}
	
	public DecodedJWT validate(DecodedJWT jwt) throws Exception {		
		return verifier.verify(jwt);
	}


}
