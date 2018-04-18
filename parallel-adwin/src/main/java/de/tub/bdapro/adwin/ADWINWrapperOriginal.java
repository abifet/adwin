package de.tub.bdapro.adwin;


import de.tub.bdapro.adwin.core.ADWINOld;

/**
 * This is the implementation of the {@link ADWINInterface} for the original ADWIN implementation {@link ADWINOld}
 */
public class ADWINWrapperOriginal implements ADWINInterface {


    private final ADWINOld adwin;

    private int adwinCount;
    private int numElementsProcessed;

    public ADWINWrapperOriginal(double delta) throws Exception {


        this.adwin = new ADWINOld(delta);
    }

    @Override
    public boolean addElement(final double element) throws Exception {
        this.numElementsProcessed++;
        adwinCount++;
        return this.adwin.setInput(element);
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
    public int getNumElementsProcessed() {
        return this.numElementsProcessed;
    }

    @Override
    public void terminateAdwin() { return; }

    public int getSize() {
        return 0;
    }

}
