package de.tub.bdapro.adwin.core;


import de.tub.bdapro.adwin.core.histogram.Bucket;
import de.tub.bdapro.adwin.core.histogram.Histogram;

/**
 * This is the serial implementation of ADWIN.
 * It basically executes a full cut detection in the main thread.
 */
public class SequentialADWINImpl extends ADWIN {

    public SequentialADWINImpl(double delta) {
        super(delta);
    }


    @Override
    public boolean execute(Histogram histogram) {

        boolean tryToFindCut = true;
        boolean cutFound = false;
        while (tryToFindCut) {
            tryToFindCut = false;
            if (checkHistogramForCut(histogram, histogram.reverseBucketIterable(), histogram.getNumBuckets() - 1)) {
                histogram.removeBuckets(1);
                tryToFindCut = true;
                cutFound = true;
            }
        }
        return cutFound;
    }


    @Override
    public void terminate() {}

}
