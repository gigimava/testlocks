package it.gfranco.locktest.locked;

import java.util.concurrent.locks.Lock;

public class WriteLockedArray extends ReadLockedArray {
	public WriteLockedArray(int[] an, Lock lock) {
		super(an, lock);
	}

	public void set(int i, int val) {
		an[i] = val;
	}
}
