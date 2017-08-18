// Copyright 2014 Thomas Müller
// This file is part of MarMoT, which is licensed under GPLv3.

package marmot.lemma;

public class WeightedLemmatizerModel extends AbstractLemmatizerModel {

	private String output_;

	public WeightedLemmatizerModel(char[] alphabet, char pad_symbol,
			String output) {
		super(alphabet, pad_symbol);
		output_ = output;
	}

	@Override
	public double getScore(String input, int prev_index, int index,
			char prev_p, char p) {

		if (prev_p == pad_symbol_) {
			if (p != output_.charAt(0)) {
				return Double.NEGATIVE_INFINITY;
			}

		} else if (p == pad_symbol_) {

			if (prev_p != output_.charAt(output_.length() - 1)) {
				return Double.NEGATIVE_INFINITY;
			}

		} else {

			boolean found_bigram = false;

			for (int i = 0; i < output_.length(); i++) {

				if (output_.charAt(i) == prev_p) {

					if (i + 1 < output_.length() && output_.charAt(i + 1) == p) {

						found_bigram = true;
						break;

					}

				}

			}

			if (!found_bigram) {
				return Double.NEGATIVE_INFINITY;
			}

		}

		return 1;
	}

}
