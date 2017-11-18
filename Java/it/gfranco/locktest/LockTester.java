package it.gfranco.locktest;

import java.util.Random;
import java.util.concurrent.Callable;

import it.gfranco.locktest.guarded.GuardedArray;

public abstract class LockTester implements Callable<Integer> {
	private final int iterations;
	protected final int accessesPerIteration;
	protected final Random random;
	private final float probabilityRead;
	protected final GuardedArray guardedarray;
	
	public LockTester(int iterations, int accessesPerIteration, long randomSeed, float probabilityRead, GuardedArray guardedarray) {
		this.iterations = iterations;
		this.accessesPerIteration = accessesPerIteration;
		this.random = new Random(randomSeed);
		this.probabilityRead = probabilityRead;
		this.guardedarray = guardedarray;
	}
	
	protected abstract int doReader();
	protected abstract void doWriter();
	
	@Override
	public final Integer call() {
		int nReads = 0;
		for (int i = 0; i < iterations; ++i) {
			if (random.nextDouble() < probabilityRead) {
				doReader();
				++nReads;
			} else {
				doWriter();
			}
		}
		return nReads;
	}
}
