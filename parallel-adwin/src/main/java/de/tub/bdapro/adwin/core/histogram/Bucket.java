package de.tub.bdapro.adwin.core.histogram;

/**
 * The basic bucket is used in the histogram to contain the basic informations over elements.
 */
public class Bucket {

        private final double total;
        private final double variance;
        private final int numElements;

        Bucket(double total, double variance, int numElements) {
            this.total = total;
            this.variance = variance;
            this.numElements = numElements;
        }

        public double getTotal() {
            return total;
        }

        public double getVariance() {
            return variance;
        }

        public int getNumElements() {
            return numElements;
        }

        public double getMean() {
            return total / numElements;
        }
    }