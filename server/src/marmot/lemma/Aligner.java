// Copyright 2014 Thomas Müller
// This file is part of MarMoT, which is licensed under GPLv3.

package marmot.lemma;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import marmot.util.LevenshteinLattice;
import marmot.util.LineIterator;
import marmot.util.StringUtils;

public class Aligner {

	public static void main(String[] args) {

		String path = "/marmot/test/trn.txt";

		Aligner segmenter = new Aligner();

		int[] current_copy_cost = new int[0];
		int[] new_copy_cost = new int[0];

		while (true) {

			InputStream input_stream = segmenter.getClass()
					.getResourceAsStream(path);
			LineIterator iterator = new LineIterator(input_stream);
			while (iterator.hasNext()) {

				List<String> line = iterator.next();

				if (!line.isEmpty()) {

					String form = StringUtils.normalize(line.get(1), true);

					if (form.length() > current_copy_cost.length) {
						current_copy_cost = Arrays.copyOf(current_copy_cost,
								form.length());
						new_copy_cost = Arrays.copyOf(new_copy_cost,
								form.length());
					}

					String lemma = StringUtils.normalize(line.get(2), true);

					if (!form.equals(lemma)) {

						assert current_copy_cost != null;
						LevenshteinLattice lattice = new ItLevenshteinLattice(
								form, lemma, current_copy_cost);

						String list = lattice.searchOperationSequence();

						for (int i = 0; i < list.length(); i++) {
							if (list.charAt(i) == 'C') {
								new_copy_cost[i] += 1;
							}
						}
					}
				}
			}

			System.out.println(Arrays.toString(current_copy_cost));
			System.out.println(Arrays.toString(new_copy_cost));
			System.out.println();
			
			if (Arrays.equals(current_copy_cost, new_copy_cost)) {
				break;
			}
			

			current_copy_cost = new_copy_cost;
			new_copy_cost = new int[current_copy_cost.length];
		}
	}
}
