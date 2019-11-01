package com.nic.firebus.adapters.http;

import java.io.File;

import javax.servlet.http.HttpServlet;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;

public class HttpGateway implements ServiceProvider {

	protected Firebus firebus;
	protected DataMap config;
	
	public HttpGateway(DataMap c, Firebus f) {
		config = c;
		firebus = f;
		
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir("temp");
        
        int port = config.containsKey("port") ? Integer.parseInt(config.getString("port")) : 80; 
        tomcat.setPort(port);
         
        String contextPath = "/";
        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext(contextPath, docBase);

        DataList list = config.getList("paths");
        for(int i = 0; i < list.size(); i++)
        {
        	DataMap pathConfig = list.getObject(i);
            String name = pathConfig.getString("name");
            String urlPattern = pathConfig.getString("url");
            tomcat.addServlet(urlPattern, name, new ServiceHandler(pathConfig));      
            context.addServletMappingDecoded(urlPattern, name);
        }
         
        try {
			tomcat.start();
		} catch (LifecycleException e) {
			e.printStackTrace();
		}
        tomcat.getServer().await();		
	}
	
	public Payload service(Payload payload) throws FunctionErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	public ServiceInformation getServiceInformation() {
		// TODO Auto-generated method stub
		return null;
	}

}
