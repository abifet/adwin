package de.tub.bdapro.adwin;

import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Basic abstraction to manage the different threads.
 */
public class ThreadManager {

    private final ExecutorService executor;

    private static ThreadManager instance;


    private ThreadManager(){
        this.executor = Executors.newCachedThreadPool();
    }

    public static ThreadManager getInstance(){
        if(instance==null)
            instance = new ThreadManager();
        return instance;
    }

    public void shutdown() {
        this.executor.shutdownNow();
        instance = null;
    }

    /**
     * Adds a new task to the executor.
     * It returns a future, which will deliver the return value of the job if the job is done.
     * The job can also be canceled.
     * @param callable
     * @param <T>
     * @return Future<T>
     */
    public <T> CompletableFuture<T> addTask(Supplier<T> callable){
         return CompletableFuture.supplyAsync(callable, executor);
    }

    public CompletableFuture<Void> addTask(Runnable callable){
        return CompletableFuture.runAsync(callable, executor);
    }
}
