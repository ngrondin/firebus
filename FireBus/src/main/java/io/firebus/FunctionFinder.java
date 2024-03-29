package io.firebus;

import java.util.ArrayList;
import java.util.List;

import io.firebus.data.DataMap;
import io.firebus.information.FunctionInformation;
import io.firebus.information.NodeInformation;
import io.firebus.logging.Logger;

public class FunctionFinder {
	protected static int subTimeout = 500;
	protected NodeCore nodeCore;
	protected String functionName;
	protected List<FunctionInformation> list;
	protected int tryPointer;
	protected int waitBeforeBroadcast = 0;
	
	public FunctionFinder(NodeCore nc, String n) 
	{
		nodeCore = nc;
		functionName = n;
		refreshList();
	}
	
	protected void refreshList()
	{
		list = new ArrayList<FunctionInformation>();
		tryPointer = 0;
		int c = nodeCore.getDirectory().getNodeCount();
		for(int i = 0; i < c; i++) {
			NodeInformation ni = nodeCore.getDirectory().getNode(i);
			FunctionInformation fi = ni.getFunctionInformation(functionName);
			if(fi != null) {
				boolean inserted = false;
				for(int j = 0; j < list.size(); j++) {
					if(list.get(j).getCombinedRating() < fi.getCombinedRating()) {
						list.add(j, fi);
						inserted = true;
						break;
					}
				}
				if(!inserted)
					list.add(fi);
			}
		}
	}
	
	protected void findMore()
	{
		Logger.fine("fb.funcfinder.broadcasting", null);
		try{ Thread.sleep(waitBeforeBroadcast);} catch(Exception e) {}
		Message findMsg = new Message(0, nodeCore.getNodeId(), Message.MSGTYPE_GETFUNCTIONINFORMATION, functionName, null);
		nodeCore.getCorrelationManager().sendAndWait(findMsg, subTimeout);
		waitBeforeBroadcast += 1000;
		refreshList();
	}
	
	public FunctionInformation findNext() 
	{
		FunctionInformation fi = null;
		if(tryPointer >= list.size()) 
			findMore();
		
		if(tryPointer < list.size())
		{
			fi = list.get(tryPointer);
			tryPointer++;
		} else {
			Logger.warning("fb.funcfinder.notfound", new DataMap("function", functionName, "listlen", list.size(), "pointer", tryPointer));
		}
		return fi;
	}

}
