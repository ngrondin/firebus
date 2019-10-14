package com.nic.firebus.interfaces;

import com.nic.firebus.information.ServiceInformation;

public interface InformationRequestor 
{
	public void informationRequestCallback(ServiceInformation si);

	public void informationRequestTimeout();
}
