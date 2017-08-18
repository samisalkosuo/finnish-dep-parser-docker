// Copyright 2014 Thomas Müller
// This file is part of MarMoT, which is licensed under GPLv3.

package marmot.lemma;

import java.util.Arrays;

public class ViterbiChartDecoder {

	AbstractLemmatizerModel model_;
	String input;
	char[] pad_alphabet_;

	double[][] prob_chart_;
	int[][] index_backtrace_chart_;
	int[][] char_backtrace_chart_;

	private int max_substring_length_ = 3;

	public ViterbiChartDecoder(AbstractLemmatizerModel model, String input) {
		model_ = model;
		this.input = input;
		pad_alphabet_ = new char[1];
		pad_alphabet_[0] = model.getPadSymbol();
		
		fillChart();
	}
	
	public void fillChart() {

		prob_chart_ = new double[input.length() + 1][];
		index_backtrace_chart_ = new int[input.length() + 1][];
		char_backtrace_chart_ = new int[input.length() + 1][];

		for (int index = 0; index < input.length() + 1; index++) {

			char[] alphabet = getAlphabet(index);

			prob_chart_[index] = new double[alphabet.length];
			index_backtrace_chart_[index] = new int[alphabet.length];
			char_backtrace_chart_[index] = new int[alphabet.length];
			Arrays.fill(prob_chart_[index], Double.NEGATIVE_INFINITY);

			for (int p_index = 0; p_index < alphabet.length; p_index++) {

				char p = alphabet[p_index];

				int prev_start_index = index - max_substring_length_;

				for (int prev_index = Math.max(-1, prev_start_index); prev_index < index; prev_index++) {

					char[] prev_alphabet = getAlphabet(prev_index);

					for (int prev_p_index = 0; prev_p_index < prev_alphabet.length; prev_p_index++) {

						char prev_p = prev_alphabet[prev_p_index];

						double score = model_.getScore(input, prev_index,
								index, prev_p, p);

						if (prev_index > 0) {
							score += prob_chart_[prev_index][prev_p_index];
						}

						if (score > prob_chart_[index][p_index]) {

							prob_chart_[index][p_index] = score;
							index_backtrace_chart_[index][p_index] = prev_index;
							char_backtrace_chart_[index][p_index] = prev_p_index;

						}

					}

				}

			}
		}
	}

	public String firstBestDecode() {
		StringBuilder sb = new StringBuilder();

		int prev_index = input.length();
		int prev_p_index = 0;

		char[] alphabet = model_.getAlphabet();

		while (true) {

			int new_index = index_backtrace_chart_[prev_index][prev_p_index];
			int new_p_index = char_backtrace_chart_[prev_index][prev_p_index];

			assert (new_index < prev_index);
			assert (new_p_index < alphabet.length);

			if (new_index < 0) {
				break;
			}

			sb.append(alphabet[new_p_index]);

			prev_index = new_index;
			prev_p_index = new_p_index;
		}

		sb.reverse();
		return sb.toString();
	}

	private char[] getAlphabet(int j) {

		if (j >= 0 && j < input.length()) {
			return model_.getAlphabet();
		}

		return pad_alphabet_;
	}

}
