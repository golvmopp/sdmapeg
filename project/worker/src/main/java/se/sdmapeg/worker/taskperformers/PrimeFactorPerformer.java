package se.sdmapeg.worker.taskperformers;

import java.util.ArrayList;

public class PrimeFactorPerformer {
	public static ArrayList<Integer> findPrimeFactors(int product) {
		ArrayList<Integer> factors = new ArrayList<>();
		for (int i = product-1; i>2; i--) {
			if (product%i==0) {
				factors.add(i);
			}
		}
		return factors;
	}
}
