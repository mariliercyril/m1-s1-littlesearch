package edu.fromatoz.littlesearch;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for App.
 */
public class AppTest extends TestCase {

    /**
     * Creates the test case.
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {

        super(testName);
    }

    /**
     * Returns the suite of tests being tested.
     * 
     * @return the suite of tests being tested
     */
    public static Test suite() {

        return new TestSuite(AppTest.class);
    }

    /**
     * Rigorous test...
     */
    public void testApp() {
 
    	assertTrue(true);
    }

}
