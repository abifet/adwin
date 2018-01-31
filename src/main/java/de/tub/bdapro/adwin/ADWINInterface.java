package de.tub.bdapro.adwin;

/**
 * The {@link ADWINInterface } is the basic interface for an external usage of this library.
 */
public interface ADWINInterface {

    /**
     * Adds a new element to the ADWIN algorithm
     * @param element
     * @return a concept drift was found
     * @throws Exception
     */
    boolean addElement(double element) throws Exception;

    // Some utility methods for monitoring
    int getAdwinCount();
    int resetAdwinCount();
    int getNumElementsProcessed();
    int getSize();
    void terminateAdwin();
}
