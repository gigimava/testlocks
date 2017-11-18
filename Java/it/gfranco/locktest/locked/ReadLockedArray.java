package it.gfranco.locktest.locked;

import java.util.concurrent.locks.Lock;

public class ReadLockedArray implements AutoCloseable {
	protected final int[] an;
	private final Lock lock;
	
	public ReadLockedArray(int[] an, Lock lock) {
		this.an = an;
		this.lock = lock;
		lock.lock();
	}
	
	public void close() {
		lock.unlock();		
	}
	
	public int get(int n) {
		return an[n];
	}
	
	public int size() {
		return an.length;
	}
}
