package it.gfranco.locktest;

import it.gfranco.locktest.guarded.GuardedArray;
import it.gfranco.locktest.locked.ReadLockedArray;
import it.gfranco.locktest.locked.WriteLockedArray;

// Lots of computations could be done out of the critical section,
// but we are ill-behaved, so it's ok.
public class IllBehavedLockTester extends LockTester {	
	public IllBehavedLockTester(int iterations, int accessesPerIteration, long randomSeed, float probabiltyRead, GuardedArray guardedarray) {
		super(iterations, accessesPerIteration, randomSeed, probabiltyRead, guardedarray);
	}

	@Override
	protected int doReader() {
		try (ReadLockedArray lan = guardedarray.acquireRead()) {
			int sum = 0;
			for (int iAccess = 0; iAccess < accessesPerIteration; ++iAccess) {
				sum += lan.get(random.nextInt(lan.size()));
			}
			return sum;
		}
	}
	
	@Override
	protected void doWriter() {
		try (WriteLockedArray lan = guardedarray.acquireWrite()) {
			for (int iAccess = 0; iAccess < accessesPerIteration; ++iAccess) {
				lan.set(random.nextInt(lan.size()), random.nextInt(1000));
			}
		}
	}
}
