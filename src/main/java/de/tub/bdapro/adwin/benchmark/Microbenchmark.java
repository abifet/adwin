package de.tub.bdapro.adwin.benchmark;


import de.tub.bdapro.adwin.ADWINInterface;
import de.tub.bdapro.adwin.ADWINWrapper;
import de.tub.bdapro.adwin.ADWINWrapperOriginal;
import de.tub.bdapro.adwin.SnapshotThreadExecuterADWINWrapper;
import de.tub.bdapro.adwin.core.HalfCutCheckThreadExecutorADWINImpl;
import de.tub.bdapro.adwin.core.SequentialADWINImpl;
import de.tub.bdapro.adwin.core.histogram.Histogram;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * This is the benchmark for the evaluation of ADWIN.
 * We Insert 100 batches with 1 mio. elements with a constant value
 * This Micobenchmark makes use of our {@link DataGenerator},
 * which generates concepts drift according to the benchmark parameters.
 *
 * To execute the benchmark we define the following program parameter:
 *
 * AdwinType = ORIGINAL | SERIAL | HALFCUT | SNAPSHOT
 * ChangeType = ABRUPT | INCREMENTAL | GRADUAL | OUTLIER | CONSTANT
 * BatchSize = the number of elements in one batch
 * NumConstant = the number of constant elements between a concept drift
 * NumChange = the length of a concept drift
 * Delta = the delta parameter of ADWIN
 *
 *
 * For example:
 * Snapshot constant 1000000 10 10 0.001
 *
 */
@State(Scope.Benchmark)
public class Microbenchmark {

    private ADWINInterface adwin;

    private int adwinCount;

    private DataGenerator dataGenerator;

    private double[] data;

    private int numInvocations;

    private int numTotalInvocations;

    private boolean warmup;

    // Dummy parameter values, because JMH requires default parameter values.
    // The actual parameter values are set in the main method.
    @Param({"SNAPSHOT"})
    public AdwinType adwinType;

    @Param({"0.01"})
    public double delta;

    @Param({"INCREMENTAL"})
    public DataGenerator.Type changeType;

    @Param({"1000"})
    public int numConstant;

    @Param({"200"})
    public int numChange;

    @Param({"20"})
    public int warmupIterations;

    @Param({"100"})
    public int measurementIterations;

    @Param({"100000"})
    public int batchSize;


    @Setup( Level.Trial )
    public void setupTrial() throws Exception {
        data = new double[batchSize];
        warmup = true;
        adwinCount = 0;
        numTotalInvocations = 0;
    }

    @Setup( Level.Iteration )
    public void setupIteration() throws Exception {
        numInvocations = 0;
        if (adwin == null) { adwin = newAdwin(); }
        if (dataGenerator == null){ dataGenerator = newDatagenerator();}
        for (int i=0; i < data.length; i++) {
            data[i] = dataGenerator.getNext();
        }
    }

    @Benchmark()
    public boolean benchmarkAdwin() throws Exception {
        return adwin.addElement(data[numInvocations++]);
    }

    @TearDown( Level.Iteration )
    public void teardownIteration() {
        System.out.print("<Number of ADWIN executions: " + (adwin.getAdwinCount() - adwinCount) + "> ");
        adwinCount = adwin.getAdwinCount();
        numTotalInvocations += numInvocations;
        if (warmup && numTotalInvocations == warmupIterations * batchSize) {
            adwin.terminateAdwin();
            warmup = false;
            adwinCount = 0;
            numTotalInvocations = 0;
            adwin = null;
            dataGenerator = null;
        }
    }

    @TearDown( Level.Trial )
    public void teardownTrial() {
        adwin.terminateAdwin();
    }

    public static void main(String[] args) throws RunnerException {

        // Read user arguments
        String argAdwinType = args[0].toUpperCase(Locale.US);
        String argChangeType = args[1].toUpperCase(Locale.US);
        String argBatchSize = args[2];
        String argNumConstant = args[3];
        String argNumChange = args[4];
        String argDelta = args[5];

        String warmupIterations = "20";
        String measurementIterations = "100";

        Options opt = new OptionsBuilder()
                .include(Microbenchmark.class.getName())
                .mode(Mode.SingleShotTime)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(Integer.valueOf(warmupIterations))
                .warmupBatchSize(Integer.valueOf(argBatchSize))
                .measurementIterations(Integer.valueOf(measurementIterations))
                .measurementBatchSize(Integer.valueOf(argBatchSize))
                .param("adwinType", argAdwinType)
                .param("delta", argDelta)
                .param("changeType", argChangeType)
                .param("numConstant", argNumConstant)
                .param("numChange", argNumChange)
                .param("warmupIterations", warmupIterations)
                .param("measurementIterations", measurementIterations)
                .param("batchSize", argBatchSize)
                .forks(1)
                .build();

        new Runner(opt).run();
    }


    private ADWINInterface newAdwin() throws Exception {
        switch (adwinType) {
            case ORIGINAL:
                return new ADWINWrapperOriginal(delta);
            case SERIAL:
                return new ADWINWrapper(delta, Histogram.class, SequentialADWINImpl.class);
            case HALFCUT:
                return new ADWINWrapper(delta, Histogram.class, HalfCutCheckThreadExecutorADWINImpl.class);
            case SNAPSHOT:
                return new SnapshotThreadExecuterADWINWrapper(delta, Histogram.class, SequentialADWINImpl.class);
        }
        throw new Exception("Unknown ADWIN type");
    }


    private DataGenerator newDatagenerator() {
        return new DataGenerator(numConstant, changeType, numChange, DataGenerator.VarianceType.NOVARIANCE);
    }

    public enum AdwinType { ORIGINAL, SERIAL, HALFCUT, SNAPSHOT }
}
