package it.gfranco.locktest.guarded;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RWGuardedArray extends GuardedArray {
	private final ReentrantReadWriteLock lock;

	public RWGuardedArray(int size, boolean fair) {
		super(size);
		this.lock = new ReentrantReadWriteLock(fair);
	}

	@Override
	protected Lock readLock() {
		return lock.readLock();
	}

	@Override
	protected Lock writeLock() {
		return lock.writeLock();
	}

}
