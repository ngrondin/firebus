package io.firebus.interfaces;

import io.firebus.information.ServiceInformation;

public interface InformationRequestor 
{
	public void informationRequestCallback(ServiceInformation si);

	public void informationRequestTimeout();
}
