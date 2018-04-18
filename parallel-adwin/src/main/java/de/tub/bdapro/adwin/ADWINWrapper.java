package de.tub.bdapro.adwin;

import de.tub.bdapro.adwin.core.ADWIN;
import de.tub.bdapro.adwin.core.histogram.Histogram;

/**
 * The {@link ADWINWrapper} is used to combine a given {@link Histogram} with an given implementation of {@link ADWIN}
 */
public class ADWINWrapper implements ADWINInterface {


    private final ADWIN adwin;
    private final Histogram histogram;
    private int numElementsProcessed;
    private int adwinCount;

    public ADWINWrapper(double delta, Class<? extends Histogram> histogramClass, Class<? extends ADWIN> adwinClass) throws Exception {

        this.histogram = (Histogram) histogramClass.getConstructors()[0].newInstance(6);
        this.adwin = (ADWIN) adwinClass.getConstructors()[0].newInstance(delta);
        this.numElementsProcessed = 0;
    }

    @Override
    public boolean addElement(final double element) throws Exception {
        this.numElementsProcessed++;
        this.histogram.addElement(element);
        adwinCount++;
        return this.adwin.execute(histogram);
    }

    @Override
    public int getAdwinCount() {
        return adwinCount;
    }

    @Override
    public int resetAdwinCount() {
        return adwinCount = 0;
    }

    @Override
    public void terminateAdwin() { this.adwin.terminate(); }

    @Override
    public int getNumElementsProcessed() {
        return this.numElementsProcessed;
    }

    public int getSize() {
        return histogram.getNumBuckets();
    }

}
