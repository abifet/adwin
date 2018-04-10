package de.tub.bdapro.adwin.core;

import de.tub.bdapro.adwin.core.histogram.Bucket;
import de.tub.bdapro.adwin.core.histogram.Histogram;

/**
 * This is the foundation of our reimplementation of the core ADWIN algorithm.
 * It basically contains the checkHistogramForCut which receives a {@link Histogram} and detects concept drifts on it.
 * It is implemented by {@link SequentialADWINImpl} or {@link HalfCutCheckThreadExecutorADWINImpl}
 */
public abstract class ADWIN {
    private final double delta;
    private final int minKeepSize;
    private final int minCutSize;

    public ADWIN(double delta) {
        this.delta = delta;
        this.minKeepSize = 7;
        this.minCutSize = 7;
    }

    /**
     * Detects concept drifts on a given {@link Histogram}
     * @param histogram
     * @param iterable
     * @param numCutPointsToCheck
     * @return true if concept drift was found
     */
    public boolean checkHistogramForCut(Histogram histogram, Iterable<Bucket> iterable, int numCutPointsToCheck) {
        double keepTotal = histogram.getTotal();
        double keepVariance = histogram.getTotal();
        int keepSize = histogram.getNumElements();

        double cutTotal = 0;
        double cutVariance = 0;
        int cutSize = 0;

        double bucketTotal, bucketVariance, bucketSize, bucketMean;
        int cutPointsChecked = 0;
        for (Bucket bucket : iterable) {
            cutPointsChecked++;
            bucketTotal = bucket.getTotal();
            bucketVariance = bucket.getVariance();
            bucketSize = bucket.getNumElements();
            bucketMean = bucket.getMean();

            keepTotal -= bucketTotal;
            keepVariance -= bucketVariance + keepSize * bucketSize * Math.pow(keepTotal / keepSize - bucketMean, 2) / (keepSize + bucketSize);
            keepSize -= bucketSize;

            cutTotal += bucketTotal;
            if (cutSize > 0)
                cutVariance += bucketVariance + cutSize * bucketSize * Math.pow(cutTotal / cutSize - bucketMean, 2) / (cutSize + bucketSize);
            cutSize += bucketSize;

            if (keepSize >= minKeepSize && cutSize >= minCutSize && isCutPoint(histogram, keepTotal, keepVariance, keepSize, cutTotal, cutVariance, cutSize)) {
                return true;
            } else if (keepSize < minKeepSize) {
                return false;
            } else if (cutPointsChecked == numCutPointsToCheck) {
                return false;
            }
        }
        return false;
    }

    private boolean isCutPoint(Histogram histogram, double keepTotal, double keepVariance, int keepSize, double cutTotal, double cutVariance, int cutSize) {
        double absMeanDifference = Math.abs(keepTotal / keepSize - cutTotal / cutSize);
        double dd = Math.log(2.0 * Math.log(histogram.getNumElements()) / delta);
        double m = 1.0 / (keepSize - minKeepSize + 3) + 1.0 / (cutSize - minCutSize + 3);
        double epsilon = Math.sqrt(2.0 * m * (histogram.getVariance() / histogram.getNumElements()) * dd) + 2.0 / 3.0 * dd * m;
        return absMeanDifference > epsilon;
    }

    public abstract boolean execute(Histogram histogram) throws Exception;
    public abstract void terminate();
}
