package se.sdmapeg.project.testapp;

import java.math.BigInteger;
import java.util.*;

public class PrimeFactorizationTask {
	private static final BigInteger NEGATIVEONE = BigInteger.ONE.negate();
	private static final BigInteger ZERO = BigInteger.ZERO;
	private static final BigInteger ONE = BigInteger.ONE;
	private static final BigInteger TWO = new BigInteger("2");
	private static final BigInteger THREE = new BigInteger("3");
	private static final BigInteger FOUR = new BigInteger("4");
	private static final BigInteger FIVE = new BigInteger("5");
	private static final BigInteger SIX = new BigInteger("6");
	private static final BigInteger SEVEN = new BigInteger("7");


	public static BigInteger trialDivision(BigInteger n) {
		if (n.equals(ONE)) {
			return ONE;
		}

		if (n.mod(TWO).equals(BigInteger.ZERO)) {
			return TWO;
		} else if (n.mod(THREE).equals(BigInteger.ZERO)) {
			return THREE;
		} else if (n.mod(FIVE).equals(BigInteger.ZERO)) {
			return FIVE;
		}

		BigInteger[] dif = {SIX, FOUR, TWO, FOUR, TWO, FOUR, SIX, TWO};
		BigInteger m = SEVEN;
		int i = 0;
		while (m.compareTo(n) != 1 && m.pow(2).compareTo(n) != 1) {
			if (n.mod(m).equals(ZERO)) {
				return m;
			}
			m = m.add(dif[i]);
			i = (i + 1)%8;
		}
		return n;
	}

	public static List<BigInteger> factor(long n) {
		return factor(new BigInteger(Long.toString(n)));
	}
	public static List<BigInteger> factor(BigInteger n) {
		if (n.abs().compareTo(ONE) != 1) {
			return new ArrayList<>();
		} else if (n.compareTo(ZERO) == -1) {
			n = n.negate();
		}
		List<BigInteger> factors = new ArrayList<>();
		while (!n.equals(ONE)) {
			BigInteger p = trialDivision(n);
			while (n.mod(p).equals(ZERO)) {
				factors.add(p);
				n = n.divide(p);
			}
		}
		Collections.sort(factors);
		return factors;
	}

	public static void main(String[] args) {
		long l = 56465657675L;
		System.out.println(l);
		System.out.println(factor(l));
	}
}
