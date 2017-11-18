package it.gfranco.locktest.guarded;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MutexGuardedArray extends GuardedArray {
	private final ReentrantLock lock;
	
	public MutexGuardedArray(int size, boolean fair) {
		super(size);
		this.lock = new ReentrantLock(fair);
	}
	
	@Override
	protected Lock writeLock() {
		return lock;
	}

	@Override
	protected Lock readLock() {
		return lock;
	}
}
