package it.gfranco.locktest;

import it.gfranco.locktest.guarded.GuardedArray;
import it.gfranco.locktest.locked.ReadLockedArray;
import it.gfranco.locktest.locked.WriteLockedArray;

public class DummyTester extends LockTester {

	public DummyTester(int iterations, int accessesPerIteration, long randomSeed, float probabilityRead, GuardedArray guardedarray) {
		super(iterations, accessesPerIteration, randomSeed, probabilityRead, guardedarray);
	}

	@Override
	protected int doReader() {
		try (ReadLockedArray lan = guardedarray.acquireRead()){
			Thread.sleep(1);
			return 0;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void doWriter() {
		try (WriteLockedArray lan = guardedarray.acquireWrite()){
			Thread.sleep(100);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
