package de.tub.bdapro.adwin.benchmark;

import java.util.Random;

/**
 * The {@link DataGenerator} generates a stream of events.
 * According to its parameters it generates different types of concept drifts.
 * For example:
 * ABRUPT, INCREMENTAL, GRADUAL, OUTLIER, CONSTANT
 */
public class DataGenerator {
    private int constant;
    private int remainingConstant;
    private double currentVal;
    private Type type;
    private int changeCounter;
    private int changeCounterMax;
    private VarianceType varianceType;
    private Random randomGenerator;


    /**
     * @param constant The number of elements between a concept drift
     * @param type The type of concept drift
     * @param lengthOfConceptDrift The length of the concept drift in nr. of elements
     * @param varianceType We can generate constant values or values from an BERNOULLI distribution to model some variance in the data stream.
     */
    public DataGenerator(int constant, Type type, int lengthOfConceptDrift, VarianceType varianceType) {
        this.constant = constant;
        this.remainingConstant = constant;
        this.currentVal = 0;
        this.type = type;
        this.changeCounter = 0;
        this.changeCounterMax = lengthOfConceptDrift;
        this.varianceType = varianceType;
        this.randomGenerator = new Random();
    }

    public double getNext() {
        if (this.remainingConstant > 0) {
            this.remainingConstant--;
            return getValue(this.currentVal);
        }
        return getNextChanged();
    }

    private double getNextChanged() {
        if (this.type==Type.ABRUPT) {
            return getNextChangedAbrupt();
        } else if (this.type==Type.INCREMENTAL) {
            return getNextChangedIncremental();
        } else if (this.type==Type.GRADUAL) {
            return getNextChangedGradual();
        } else if (this.type==Type.OUTLIER) {
            return getNextChangedOutlier();
        } else {
            return getNextChangedConstant();
        }
    }

    private double getNextChangedAbrupt() {
        this.remainingConstant = this.constant;
        this.currentVal = this.currentVal == 0 ? 1 : 0;
        return getValue(this.currentVal);
    }

    private double getNextChangedIncremental() {
        if (changeCounter <= changeCounterMax) {
            changeCounter++;
            int mult = this.currentVal == 0 ? 1 : -1;
            return getValue(this.currentVal + mult * (((double) changeCounter)/changeCounterMax));
        }
        this.currentVal = this.currentVal == 0 ? 1 : 0;
        this.remainingConstant = this.constant;
        return getValue(this.currentVal);
    }

    private double getNextChangedGradual() {
        if (changeCounter <= changeCounterMax) {
            changeCounter++;
            double threshold = (((double) changeCounter)/changeCounterMax);
            double newVal = this.currentVal == 0 ? 1 : 0;
            return getValue(this.randomGenerator.nextDouble() > threshold ? this.currentVal : newVal);
        }
        this.currentVal = this.currentVal == 0 ? 1 : 0;
        this.remainingConstant = this.constant;
        return getValue(this.currentVal);
    }

    private double getNextChangedOutlier() {
        this.remainingConstant = this.constant;
        return getValue(this.currentVal == 0 ? 1 : 0);
    }

    private double getNextChangedConstant() {
        this.remainingConstant = this.constant;
        return getValue(this.currentVal);
    }

    private double getValue(double val) {
        if (this.varianceType==VarianceType.NOVARIANCE) {
            return val;
        } else {
            double threshold = 0.1 + 0.8*val;
            return this.randomGenerator.nextDouble() > threshold ? 0 : 1;
        }
    }

    public enum Type{
        ABRUPT, INCREMENTAL, GRADUAL, OUTLIER, CONSTANT
    }

    public enum VarianceType{
        NOVARIANCE, BERNOULLI;
    }
}
