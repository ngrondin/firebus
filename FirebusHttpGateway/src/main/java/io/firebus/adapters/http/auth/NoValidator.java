package io.firebus.adapters.http.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.firebus.Firebus;
import io.firebus.adapters.http.AuthValidationHandler;
import io.firebus.utils.DataMap;

public class NoValidator extends AuthValidationHandler {
	protected String loginUrl;
	
	public NoValidator(DataMap c, Firebus fb) {
		super(c, fb);
		loginUrl = handlerConfig.getString("loginurl");
	}
	

	public String getLoginURL(String originalPath) {
		String url = loginUrl + "?redirect=" + publicHost + originalPath;
		return url;
	}

	protected void httpService(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
	}

}
