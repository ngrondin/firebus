package io.firebus;

import java.util.logging.Logger;

import io.firebus.information.NodeInformation;

public class FunctionFinder {
	private static Logger logger = Logger.getLogger("io.firebus");
	protected static int subTimeout = 500;
	
	public static NodeInformation findFunction(NodeCore nodeCore, String functionName) 
	{
		NodeInformation ni = nodeCore.getDirectory().findFunction(functionName);
		if(ni == null)
		{
			logger.fine("Broadcasting Service Information Request Message");
			Message findMsg = new Message(0, nodeCore.getNodeId(), Message.MSGTYPE_GETFUNCTIONINFORMATION, functionName, null);
			Message respMsg = nodeCore.getCorrelationManager().sendAndWait(findMsg, subTimeout);
			if(respMsg != null)
			{
				ni = nodeCore.getDirectory().getNodeById(respMsg.getOriginatorId());
			}
		}
		return ni;
	}

}
