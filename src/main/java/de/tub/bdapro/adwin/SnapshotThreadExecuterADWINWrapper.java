package de.tub.bdapro.adwin;


import de.tub.bdapro.adwin.core.ADWIN;
import de.tub.bdapro.adwin.core.histogram.Histogram;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * This is an optimistic implementation of the ADWIN algorithm.
 * It uses a separate thread for the adwin calculation, which is executed on a copy of the histogram.
 * Concurrently we are processing new elements, which are added to histogram and an redo log.
 * If the ADWIN Thread has detected a concept drift, the histogram is replaced by the old copy and the redo log is processed.
 * If no change was detected we start a new iteration of the ADWIN thread.
 * <p>
 * This solution should be correct in therms of quality
 */
public class SnapshotThreadExecuterADWINWrapper implements ADWINInterface {


    private final ADWIN adwin;
    private Histogram histogram;
    private Future<Boolean> adwinResult;
    private List<Double> redoLog = new ArrayList<>();
    private ThreadManager threadManager = ThreadManager.getInstance();
    private Histogram histogramCopy;
    private int AdwinCount;
    private int numElementsProcessed;


    public SnapshotThreadExecuterADWINWrapper(double delta, Class<? extends Histogram> histogramClass, Class<? extends ADWIN> adwinClass) throws Exception {
        this.histogram = (Histogram) histogramClass.getConstructors()[0].newInstance(6);
        this.adwin = (ADWIN) adwinClass.getConstructors()[0].newInstance(delta);
        this.numElementsProcessed = 0;
    }

    public boolean addElement(final double element) throws Exception {
        this.numElementsProcessed++;
        if (this.adwinResult == null) {
            this.histogram.addElement(element);
            newADWINTask();
            return false;
        } else if (this.adwinResult.isDone()) {
            boolean adwinResult = this.adwinResult.get();
            if (adwinResult) {
                // adwin has found a cut !
                rollbackHistogram();
            }
            // drop log
            this.redoLog.clear();
            this.histogram.addElement(element);
            newADWINTask();
            return adwinResult;
        } else {
            this.histogram.addElement(element);
            redoLog.add(element);
            return false;
        }
    }

    @Override
    public int getAdwinCount() {
        return this.AdwinCount;
    }

    @Override
    public int resetAdwinCount() {
        return AdwinCount = 0;
    }

    private void newADWINTask() {
        this.histogramCopy = this.histogram.copy();
        this.AdwinCount++;
        adwinResult = threadManager.addTask(() -> {
            //System.out.println("Start Adwin");
            try {
                return adwin.execute(histogramCopy);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    @Override
    public int getNumElementsProcessed() {
        return this.numElementsProcessed;
    }

    private void rollbackHistogram() {

        // rollback histogram
        this.histogram = this.histogramCopy;

        for (int i = 0; i < redoLog.size(); i++) {
            this.histogram.addElement(redoLog.get(i));
        }
    }

    public int getSize() {
        return histogram.getNumBuckets();
    }

    @Override
    public void terminateAdwin() {
        this.adwin.terminate();
        this.threadManager.shutdown();
    }

}
