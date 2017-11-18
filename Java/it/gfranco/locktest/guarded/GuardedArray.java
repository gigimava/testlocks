package it.gfranco.locktest.guarded;

import java.util.concurrent.locks.Lock;

import it.gfranco.locktest.locked.ReadLockedArray;
import it.gfranco.locktest.locked.WriteLockedArray;

public abstract class GuardedArray {
	private final int[] an;
	
	public GuardedArray(int size) {
		this.an = new int[size];
	}
	
	protected abstract Lock readLock();
	public ReadLockedArray acquireRead() {
		return new ReadLockedArray(an, readLock());
	}
	
	protected abstract Lock writeLock();
	public WriteLockedArray acquireWrite() {
		return new WriteLockedArray(an, writeLock());
	}
	
	public int size() {
		return an.length;
	}
}
