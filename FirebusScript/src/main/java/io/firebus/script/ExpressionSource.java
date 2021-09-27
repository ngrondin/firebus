package io.firebus.script;

public class ExpressionSource extends Source {
	protected boolean isFixed;
	protected Object fixedValue;
	
	public ExpressionSource(String n, String b) {
		super(n, preProcessBody(b));
		processBody(b);
	}
	
	private static String preProcessBody(String b) {
		if(b != null && b.startsWith("{") && b.endsWith("}"))
			b = "(" + b + ")";	
		return b;
	}

	private void processBody(String b) {
		if(b == null) {
			isFixed = true;
			fixedValue = null;
		} else if(b.equals("null")) {
			isFixed = true;
			fixedValue = null;
		} else if(b.equalsIgnoreCase("true")) {
			isFixed = true;
			fixedValue = true;
		} else if(b.equalsIgnoreCase("false")) {
			isFixed = true;
			fixedValue = false;
		} else {
			boolean isOnlyDigits = true;
			int i = 0;
			while(i < b.length() && isOnlyDigits == true) {
				char c = b.charAt(i);
				if(!((((int)c) >= 48 && ((int)c) <= 57) || c == '.')) 
					isOnlyDigits = false;
				i++;
			}
			if(isOnlyDigits) {
				if(b.contains(".")) {
					isFixed = true;
					fixedValue = Double.parseDouble(b);
				} else {
					isFixed = true;
					long l = Long.parseLong(b);	
					if(l <= 2147483647 && l >= -2147483648) 
						fixedValue = (int)l;
					else
						fixedValue = l;
				}
			}
		}
	}
	
	public boolean isFixedValue() {
		return isFixed;
	}
	
	public Object getFixedValue() {
		return fixedValue;
	}
}
