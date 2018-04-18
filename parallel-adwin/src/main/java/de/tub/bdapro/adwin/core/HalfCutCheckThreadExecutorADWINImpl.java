package de.tub.bdapro.adwin.core;


import de.tub.bdapro.adwin.core.histogram.Histogram;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This implementation of ADWIN contains the implementation of the HalfCutCheck approach.
 * It uses a ExecuterService to create new Threads.
 */
public class HalfCutCheckThreadExecutorADWINImpl extends ADWIN {


    private final ExecutorService executor = Executors.newCachedThreadPool();


    public HalfCutCheckThreadExecutorADWINImpl(double delta) {
        super(delta);
    }

    @Override
    public boolean execute(Histogram histogram) throws  Exception {

        boolean tryToFindCut = true;
        boolean cutFound = false;
        while (tryToFindCut) {
            tryToFindCut = false;
            // create forward thread
            final CompletableFuture<Boolean> checkForwardHalf = CompletableFuture.supplyAsync( () ->
                    (checkHistogramForCut(histogram, histogram.forwardBucketIterable(), histogram.getNumBuckets()/2)), executor);

            // use main thread as backward thread
            final boolean checkBackwardHalf = checkHistogramForCut(histogram, histogram.reverseBucketIterable(), (histogram.getNumBuckets()-1)/2);

            if (checkBackwardHalf || checkForwardHalf.get() ) {
                histogram.removeBuckets(1);
                tryToFindCut = true;
                cutFound = true;
            }
        }
        return cutFound;
    }

    public void terminate() { this.executor.shutdownNow(); }
}
