package it.gfranco.locktest;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import it.gfranco.locktest.guarded.*;
import it.gfranco.locktest.locked.WriteLockedArray;

public class LockTestMain {
	private static interface GuardedArrayFactory extends IntFunction<GuardedArray> { }
	@FunctionalInterface
	private static interface LockTesterFactory {
		LockTester makeLockTester(int iterations, int accessesPerIteration, long randomSeed, float probabilityRead, GuardedArray guardedarray);
	};
	
	private final Random rand = new Random(0xF1FA2017);
	int iterations = 100000;
	int accessesPerIterationMin = 10;
	int accessesPerIterationMax = 3000;
	int accessesPerIterationMultiplier = 2;
	float probabilityReadMin = 0.8f;
	float probabilityReadMax = 0.8f;
	float probabilityReadStep = 0.05f;
	int arraySize = 10000;
	int threadsMin = 1;
	int threadsMax = 256;
	int threadsMultiplier = 2;
	
	private static <T> T getNoExcept(Future<T> future) {
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void runTest(GuardedArrayFactory gafactory, LockTesterFactory ltfactory) throws InterruptedException {
		System.out.println("Threads\tAccesses per iteration\tRead probability (theoretical)\tRead probability (real)\tRuntime (ns)");
		ExecutorService pool = Executors.newFixedThreadPool(threadsMax);
		float nProbabilitySteps = (int)((probabilityReadMax - probabilityReadMin) / probabilityReadStep);
		for (int nThreads = threadsMin; nThreads <= threadsMax; nThreads *= threadsMultiplier) {
			for (int nAccesses = accessesPerIterationMin; nAccesses <= accessesPerIterationMax; nAccesses *= accessesPerIterationMultiplier) {
				for (int iStep = 0; iStep <= nProbabilitySteps; ++iStep) {
					float probabilityRead = probabilityReadMin + probabilityReadStep * iStep;
					GuardedArray guardedarray = gafactory.apply(arraySize);
					try (WriteLockedArray arr = guardedarray.acquireWrite()) {
						for (int i = 0; i < arr.size(); ++i) {
							arr.set(i, i);
						}
					}
					
					long nStart = System.nanoTime();
					int nAccessessBecauseJavaIsStupid = nAccesses;
					int nThreadsBecauseJavaIsStupid = nThreads;
					List<Future<Integer>> lstfuture = pool.invokeAll(
							IntStream.range(0, nThreads)
							.mapToObj(iThread -> ltfactory.makeLockTester(
									iterations / nThreadsBecauseJavaIsStupid,
									nAccessessBecauseJavaIsStupid,
									rand.nextLong(),
									probabilityRead,
									guardedarray
							))
							.collect(Collectors.toList())
					);
					long nEnd = System.nanoTime();
					
					System.out.format("%10d\t%10d\t%10f\t%10f\t%10d\n",
							nThreads,
							nAccesses,
							probabilityRead,
							lstfuture.stream().mapToLong(future -> getNoExcept(future)).sum() / (double)iterations,
							nEnd - nStart);
				}
				System.gc(); // JVM will probably ignore it, but I know it'd be better to do it here.
			}
		}
		pool.shutdown();
	}
	
	//private static IntFunction<GuardedArray> gafactory = size -> new MutexGuardedArray(size, false);
	//private static LockTester makeLockTester(int iterations, int accessesPerIteration, long randomSeed, float probabilityRead, GuardedArray guardedarray) {
	//	return new IllBehavedLockTester(iterations, accessesPerIteration, randomSeed, probabilityRead, guardedarray);
	//}
	
	private static void warmup(GuardedArrayFactory gafactory, LockTesterFactory ltfactory) throws InterruptedException {	
		LockTestMain ltm = new LockTestMain();
		ltm.accessesPerIterationMin = 1;
		ltm.accessesPerIterationMax = 8;
		ltm.iterations = 100;
		ltm.threadsMin = 1;
		ltm.threadsMax = 1;
		ltm.runTest(gafactory, ltfactory);
	}
	
	private static <T, U> void forEachPair(T[] at, U[] au, BiConsumer<T, U> fn) {
		if (at.length != au.length) {
			throw new IllegalArgumentException();
		}
		for (int i = 0; i < at.length; ++i) {
			fn.accept(at[i], au[i]);
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		Class<?>[] aclassLockTester = {
				IllBehavedLockTester.class,
				WellBehavedLockTester.class
		};
		LockTesterFactory[] altfactory = {
				(a, b, c, d, e) -> new IllBehavedLockTester(a, b, c, d, e),
				(a, b, c, d, e) -> new WellBehavedLockTester(a, b, c, d, e)
			};
		Class<?>[] aclassGuardedArray = {
				MutexGuardedArray.class,
				RWGuardedArray.class
		};
		GuardedArrayFactory[] agafactory = {
				size -> new MutexGuardedArray(size, false),
				size -> new RWGuardedArray(size, false)
		};
		
		System.out.println("Warmup");
		for (LockTesterFactory ltfactory : altfactory) {
			for (GuardedArrayFactory gafactory : agafactory) {
				warmup(gafactory, ltfactory);
			}
		}
		
		System.out.print("\nActual test");
		forEachPair(aclassLockTester, altfactory, (classLockTester, ltfactory) -> {
			forEachPair(aclassGuardedArray, agafactory, (classGuardedArray, gafactory) -> {
				System.out.println();
				System.out.append("LockTester: ").append(classLockTester.getSimpleName())
					.append("\nGuardedArray: ").append(classGuardedArray.getSimpleName())
					.println();
				try {
					new LockTestMain().runTest(gafactory, ltfactory);
				} catch (InterruptedException e1) {
					throw new RuntimeException(e1);
				}
			});
		});
	}
}
