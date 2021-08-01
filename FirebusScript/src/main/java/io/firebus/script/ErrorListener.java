package io.firebus.script;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

public class ErrorListener implements ANTLRErrorListener {
	protected List<String> errors;
	
	public ErrorListener() {
		errors = new ArrayList<String>();
	}
	
	public int getErrorCount() {
		return errors.size();
	}
	
	public List<String> getErrors() {
		return errors;
	}

	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
		errors.add(line + ":" + charPositionInLine + " " + msg);
	}

	public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
	}

	public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
	}

	public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
	}

}
