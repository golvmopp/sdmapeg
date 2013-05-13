package se.sdmapeg.server.test;

import java.util.Iterator;

/**
 *
 * @author niclas
 */
public final class PairIterator<L, R> implements Iterator<PairIterator.Pair<L, R>> {
	private final Iterator<L> leftIterator;
	private final Iterator<R> rightIterator;

	public PairIterator(Iterator<L> leftIterator,
						Iterator<R> rightIterator) {
		this.leftIterator = leftIterator;
		this.rightIterator = rightIterator;
	}

	@Override
	public boolean hasNext() {
		return leftIterator.hasNext() && rightIterator.hasNext();
	}

	@Override
	public Pair<L, R> next() {
		return new PairIterator.Pair<>(leftIterator.next(), rightIterator.next());
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public static <L, R> Iterable<PairIterator.Pair<L, R>> iterable(final Iterable<L> leftIterable,
																	final Iterable<R> rightIterable) {
		return new Iterable<PairIterator.Pair<L, R>>() {
			@Override
			public Iterator<Pair<L, R>> iterator() {
				return new PairIterator<>(leftIterable.iterator(),
										  rightIterable.iterator());
			}
		};
	}

	public static final class Pair<L, R> {
		private final L left;
		private final R right;

		public Pair(L left, R right) {
			this.right = right;
			this.left = left;
		}

		public L getLeft() {
			return left;
		}

		public R getRight() {
			return right;
		}
	}
	
}
