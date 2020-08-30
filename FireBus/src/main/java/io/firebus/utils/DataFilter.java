package io.firebus.utils;

import java.util.Iterator;

public class DataFilter {

	protected DataMap filter;
	
	public DataFilter(DataMap f) {
		filter = f;
	}
	
	public boolean apply(DataMap data) {
		return recursiveApply(filter, data);
	}
	
	protected boolean recursiveApply(DataMap filter, DataMap data) {
		boolean ret = true;
		Iterator<String> it = filter.keySet().iterator();
		while(it.hasNext()) {
			String key = it.next();
			DataEntity fVal = filter.get(key);
			if(key.equals("$or")) {
				if(fVal instanceof DataList) {
					DataList fValList = (DataList)fVal;
					boolean subRet = false;
					for(int i = 0; i < fValList.size(); i++) {
						DataEntity fSubVal = fValList.get(i);
						if(fSubVal instanceof DataMap) {
							if(recursiveApply((DataMap)fSubVal, data)) {
								subRet = true;
							}
						} 					
					}
					if(subRet == false)
						ret = false;
				} else {
					ret = false;
				}				
			} else if(key.equals("$and")) {
				if(fVal instanceof DataList) {
					DataList fValList = (DataList)fVal;
					for(int i = 0; i < fValList.size(); i++) {
						DataEntity fSubVal = fValList.get(i);
						if(fSubVal instanceof DataMap) {
							if(!recursiveApply((DataMap)fSubVal, data)) {
								ret = false;
							}
						} else {
							ret = false;
						}						
					}
				} else {
					ret = false;
				}				
			} else {
				DataEntity dVal = getDataValue(data, key);
				if(fVal instanceof DataMap) {
					DataMap fValObj = (DataMap)fVal;
					if(fValObj.containsKey("$in")) {
						DataEntity fSubVal = fValObj.get("$in");
						if(fSubVal instanceof DataList) {
							boolean subRet = false;
							DataList fSubValList = (DataList)fSubVal;
							for(int i = 0; i < fSubValList.size(); i++) {
								if(valuesAreEqual(fSubValList.get(i), dVal)) {
									subRet = true;
								}
							}
							if(subRet == false) 
								ret = false;
						} else {
							ret = false;
						}
					} else if(fValObj.containsKey("$nin")) {
						DataEntity fSubVal = fValObj.get("$nin");
						if(fSubVal instanceof DataList) {
							DataList fSubValList = (DataList)fSubVal;
							for(int i = 0; i < fSubValList.size(); i++) {
								if(valuesAreEqual(fSubValList.get(i), dVal)) {
									ret = false;
								}
							}
						} else {
							ret = false;
						}					
					} else if(fValObj.containsKey("$eq")) {
						DataEntity fSubVal = fValObj.get("$eq");
						if(!valuesAreEqual(fSubVal, dVal)) {
							ret = false;
						} 
					} else if(fValObj.containsKey("$ne")) {
						DataEntity fSubVal = fValObj.get("$ne");
						if(valuesAreEqual(fSubVal, dVal)) {
							ret = false;
						} 
					} else if(fValObj.containsKey("$gt")) {
						DataEntity fSubVal = fValObj.get("$gt");
						if(fSubVal instanceof DataLiteral) {
							if(((DataLiteral)fSubVal).getNumber().doubleValue() >= ((DataLiteral)dVal).getNumber().doubleValue()) {
								ret = false;
							} 
						} else {
							ret = false;
						}
					} else if(fValObj.containsKey("$lt")) {
						DataEntity fSubVal = fValObj.get("$lt");
						if(fSubVal instanceof DataLiteral) {
							if(((DataLiteral)fSubVal).getNumber().doubleValue() <= ((DataLiteral)dVal).getNumber().doubleValue()) {
								ret = false;
							} 
						} else {
							ret = false;
						}
					} 
				} else if(fVal instanceof DataLiteral) {
					if(!valuesAreEqual(fVal, dVal)) {
						ret = false;
					}						
				}				
			}

		}
		
		return ret;
	}
	
	
	protected DataEntity getDataValue(DataEntity d, String key) {
		DataEntity ret = null;
		String root = key;
		String rest = null;
		if(key.indexOf(".") > -1) {
			root = key.substring(0, key.indexOf("."));
			rest = key.substring(key.indexOf(".") + 1);
		}
		if(d instanceof DataMap) {
			DataEntity rootEntity = ((DataMap)d).get(root); 
			if(rest == null) {
				return rootEntity;
			} else {
				return getDataValue(rootEntity, rest);
			}			
		} else if(d instanceof DataList) {
			DataList rootList = (DataList)d;
			DataList list = new DataList();
			for(int i = 0; i < rootList.size(); i++) {
				if(rest == null) {
					if(rootList.get(i) instanceof DataMap) {
						list.add(((DataMap)rootList.get(i)).get(root));
					} 
				} else {
					list.add(getDataValue(rootList.get(i), rest));
				}
			}
			return list;
		} else if(d instanceof DataLiteral) {
			if(rest == null) {
				return d;
			} else {
				return null;
			}
		}
		return ret;
	}
	
	protected boolean valuesAreEqual(DataEntity val1, DataEntity val2) {
		if(val1 instanceof DataLiteral && val2 instanceof DataLiteral) {
			if(((DataLiteral)val1).equals((DataLiteral)val2))
				return true;
		} else if(val1 instanceof DataList && val2 instanceof DataLiteral) {
			return literalIsInList((DataLiteral)val2, (DataList)val1);
		} else if(val1 instanceof DataLiteral && val2 instanceof DataList) {
			return literalIsInList((DataLiteral)val1, (DataList)val2);
		} else if(val1 instanceof DataMap && val2 instanceof DataMap) {
			return ((DataMap)val1).equals((DataMap)val2);
		}
		return false;
	}
	
	protected boolean literalIsInList(DataLiteral lit, DataList list) {
		for(int i = 0; i < list.size(); i++) {
			if(((DataLiteral)list.get(i)).equals(lit))
				return true;
		}
		return false;
	}

}
