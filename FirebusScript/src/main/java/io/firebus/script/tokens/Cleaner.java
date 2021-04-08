package io.firebus.script.tokens;

public class Cleaner {

	public static void clean(TokenList list) {
		boolean inCommentBlock = false;
		boolean inCommentLine = false;
		for(int i = 0; i < list.size(); i++) {
			if(list.tokenIs(i, "/*") && !inCommentBlock && !inCommentLine) {
				inCommentBlock = true;
			} else if(list.tokenIs(i, "*/") && inCommentBlock) {
				inCommentBlock = false;
				list.remove(i--);
			} else if(list.tokenIs(i, "//") && !inCommentBlock && !inCommentLine) {
				inCommentLine = true;
			} else if(list.tokenIs(i, "\n") && inCommentLine) {
				inCommentLine = false;
				list.remove(i--);
			}
			
			if(inCommentBlock || inCommentLine) {
				list.remove(i--);
			}
		}
	}
}
