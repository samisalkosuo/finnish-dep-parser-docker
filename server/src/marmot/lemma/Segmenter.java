// Copyright 2014 Thomas Müller
// This file is part of MarMoT, which is licensed under GPLv3.

package marmot.lemma;

import java.io.InputStream;
import java.util.List;

import marmot.util.LevenshteinLattice;
import marmot.util.LineIterator;
import marmot.util.StringUtils;

public class Segmenter {

	public static void main(String[] args) {

		String path = "/marmot/test/trn.txt";

		Segmenter segmenter = new Segmenter();

		InputStream input_stream = segmenter.getClass().getResourceAsStream(
				path);
		LineIterator iterator = new LineIterator(input_stream);

		while (iterator.hasNext()) {
			List<String> line = iterator.next();

			if (!line.isEmpty()) {

				String form = StringUtils.normalize(line.get(1), true);
				String lemma = StringUtils.normalize(line.get(2), true);
				
				String tag = line.get(4);
				
				
				if (! (tag.startsWith("N") || tag.startsWith("V"))) {
					continue;
				}

				if (!form.equals(lemma)) {

					LevenshteinLattice lattice = new LevenshteinLattice(form,
							lemma);

					List<List<Character>> list = lattice
							.searchOperationSequences(true);

					assert list.size() > 0;

					for (List<Character> seq : list) {

						System.err.print(form + " " + lemma + " " + seq);

						StringBuilder form_string = new StringBuilder("");
						StringBuilder lemma_string = new StringBuilder("");

						int form_index = 0;
						int lemma_index = 0;

						for (char c : seq) {

							switch (c) {

							case 'C':
							case 'R':

								if (form_string.length() > 0
										&& lemma_string.length() > 0) {

									System.err.print(" " + form_string + ","
											+ lemma_string);
									form_string.setLength(0);
									lemma_string.setLength(0);

								}

								form_string.append(form.charAt(form_index));
								form_index++;

								lemma_string.append(lemma.charAt(lemma_index));
								lemma_index++;

								break;

							case 'I':

								lemma_string.append(lemma.charAt(lemma_index));
								lemma_index++;

								break;

							case 'D':

								form_string.append(form.charAt(form_index));
								form_index++;

							}

						}

						form_string.append('$');
						lemma_string.append('$');
						System.err.println(" " + form_string + ","
								+ lemma_string);

					}

				}

			}

		}

	}

}
