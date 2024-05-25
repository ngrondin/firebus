package io.firebus.utils.jwt;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.firebus.data.DataList;
import io.firebus.data.DataMap;

public class JWTValidator {
	List<JWTValidatorEntry> entries;

	public JWTValidator() {
		entries = new ArrayList<JWTValidatorEntry>();
	}
	
	public void tryAddSharedSecret(String issuer, String secret) {
		try {
			addSharedSecret(issuer, secret);
		} catch(Exception e) {}
	}
	
	public void addSharedSecret(String issuer, String secret) throws JWTValidatorException {
		entries.add(new JWTValidatorEntry(issuer, null, "HS256", secret));
		entries.add(new JWTValidatorEntry(issuer, null, "HS384", secret));
		entries.add(new JWTValidatorEntry(issuer, null, "HS384", secret));
	}
	
	
	public void tryAddJWK(String issuer, DataMap jwkMap) {
		try {
			addJWK(issuer, jwkMap);
		} catch(Exception e) {}
	}
		
	public void addJWK(String issuer, DataMap jwkMap) throws Exception {
		DataList list = jwkMap.getList("keys");
		for(int i = 0; i < list.size(); i++) {
			DataMap keyMap = list.getObject(i);
			String alg = keyMap.getString("alg");
			String kid = keyMap.getString("kid");
			BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(keyMap.getString("n")));
			BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(keyMap.getString("e")));
			String algInstance = alg.startsWith("RS") ? "RSA" : alg.startsWith("ES") ? "ECDSA" : null;
			if(algInstance != null) {
			    PublicKey pubKey = KeyFactory.getInstance(algInstance).generatePublic(new RSAPublicKeySpec(modulus, exponent));
			    entries.add(new JWTValidatorEntry(issuer, kid, alg, pubKey));
			}
		}
	}
	
	public void clearAll() {
		entries.clear();
	}
	
	public DecodedJWT tryDecode(String token) {
		try {
			return decode(token);
		} catch(Exception e) {
			return null;
		}
	}
	
	public DecodedJWT decode(String token) throws JWTValidatorException {
		try {
			return JWT.decode(token);
		} catch(Exception e) {
			throw new JWTValidatorException("Error decoding token", e);
		}
	}	

	
	public void decodeAndValidate(String token) throws JWTValidatorException {
		validate(decode(token));
	}
	
	public boolean tryValidate(String token)  {
		try {
			validate(decode(token));
			return true;
		} catch(Exception e) {
			return false;
		}
	}

	public boolean tryValidate(DecodedJWT jwt) {
		try {
			validate(jwt);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	public void validate(DecodedJWT jwt) throws JWTValidatorException {
		if(jwt != null) {
			String issuer = jwt.getIssuer();
			String alg = jwt.getAlgorithm();
			String kid = jwt.getKeyId();
			for(JWTValidatorEntry entry: entries) {
				if(entry.matches(issuer, kid, alg)) {
					try {
						entry.validate(jwt);
						return;
					} catch(Exception e) {
						throw new JWTValidatorException("Invalid token", e);
					}
				}
			}
		}
		throw new JWTValidatorException("No validator for token");
	}
	
	
}
