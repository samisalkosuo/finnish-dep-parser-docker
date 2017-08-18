// Copyright 2014 Thomas Müller
// This file is part of MarMoT, which is licensed under GPLv3.

package marmot.lemma;

public abstract class AbstractLemmatizerModel {

	char[] alphabet_;
	
	char pad_symbol_;
	
	public AbstractLemmatizerModel (char[] alphabet, char pad_symbol) {
		alphabet_ = alphabet;
		pad_symbol_ = pad_symbol;		
	}	
	
	public char getPadSymbol() {
		return pad_symbol_;
	}

	public char[] getAlphabet() {
		return alphabet_;
	}

	public abstract double getScore(String input, int prev_index, int index,
			char prev_p, char p);

}
