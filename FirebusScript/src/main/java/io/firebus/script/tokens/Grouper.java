package io.firebus.script.tokens;

import java.util.ArrayList;
import java.util.List;

public class Grouper {

	public static void group(TokenList list, String m1, String m2) {
		List<Integer> starts = new ArrayList<Integer>();
		for(int i = 0; i < list.size(); i++) {
			Token token = list.get(i);
			if(token.is(m1)) {
				starts.add(i);
			} else if(token.is(m2)) {
				int start = starts.get(starts.size() - 1);
				starts.remove(starts.size() - 1);
				TokenList sublist = list.slice(start, i);
				TokenGroup group = new TokenGroup(m1 + m2, sublist.slice(1, sublist.size() - 2));
				list.add(start, group);
				i = start;
			}
		}		
	}
}
