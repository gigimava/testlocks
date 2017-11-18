package it.gfranco.locktest;

import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import it.gfranco.locktest.guarded.GuardedArray;
import it.gfranco.locktest.locked.ReadLockedArray;
import it.gfranco.locktest.locked.WriteLockedArray;

public class WellBehavedLockTester extends LockTester {
	public WellBehavedLockTester(int iterations, int accessesPerIteration, long randomSeed, float probabiltyRead, GuardedArray guardedarray) {
		super(iterations, accessesPerIteration, randomSeed, probabiltyRead, guardedarray);
	}

	@Override
	protected int doReader() {
		// Predetermine which locations we're going to read
		int[] an = IntStream.range(0, accessesPerIteration)
			.map(iAccess -> random.nextInt(guardedarray.size()))
			.toArray();
		try (ReadLockedArray lan = guardedarray.acquireRead()) {
			int sum = 0;
			for (int in = 0; in < an.length; ++in) {
				sum += lan.get(an[in]);
			}
			return sum;
		}
	}
	
	@Override
	protected void doWriter() {
		// Predetermine which locations we're going to write and what we're going to write
		class ArrayOp {
			private final int[] an;
			
			public ArrayOp(int size, IntUnaryOperator fnLocation, IntUnaryOperator fnValue) {
				this.an = new int[size * 2];
				for (int i = 0; i < size; ++i) {
					an[i * 2] = fnLocation.applyAsInt(i);
					an[i * 2 + 1] = fnValue.applyAsInt(i);
				}
			}
			
			int getLocation(int i) {
				return an[i * 2];
			}
			int getValue(int i) {
				return an[i * 2 + 1];
			}
		}
		ArrayOp aop = new ArrayOp(accessesPerIteration, in -> random.nextInt(guardedarray.size()), in -> random.nextInt(1000));
		try (WriteLockedArray lan = guardedarray.acquireWrite()) {
			for (int in = 0; in < accessesPerIteration; ++in) {
				lan.set(aop.getLocation(in), aop.getValue(in));
			}
		}
	}
}
