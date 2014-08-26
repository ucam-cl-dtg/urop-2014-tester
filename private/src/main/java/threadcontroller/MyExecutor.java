package threadcontroller;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MyExecutor {
	private static final BlockingQueue<Runnable> waitingQueue = new LinkedBlockingQueue<>();
	
	public static ExecutorService newFixedThreadPool(int nThreads) {
        System.out.println(nThreads);
	    return new ThreadPoolExecutor(nThreads, nThreads,
	                                  0L, TimeUnit.MILLISECONDS,
	                                  waitingQueue);
	}
	
	public static BlockingQueue<Runnable> getWaitingQueue() {
		return waitingQueue;
	}
}

