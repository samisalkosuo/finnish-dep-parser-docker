package marmot.lemma;

import marmot.util.LevenshteinLattice;

public class ItLevenshteinLattice extends LevenshteinLattice {

	private int[] copy_gain_;
	
	public ItLevenshteinLattice(String input, String output, int[] copy_cost) {
		
		super(input, output, 0, 0, 0);
		
		copy_gain_ = copy_cost;
		
		assert copy_cost != null;
		
		
	}
	
	@Override
	protected int getCopyCost(int input_index) {
		int cost;

		assert copy_gain_ != null;
		
		if (copy_gain_ == null || input_index >= copy_gain_.length) {
			cost = -1;
		} else {
			cost = - (copy_gain_[input_index] + 1);
		}
		
		
		
		//System.err.println("getCopyCost: " + input_index + " " + cost + " " + Arrays.toString(copy_gain_));		
		return cost;
	}

}
