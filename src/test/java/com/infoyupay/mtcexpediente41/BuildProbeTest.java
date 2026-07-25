package com.infoyupay.mtcexpediente41;

import org.junit.jupiter.api.Test;

/**
 * The BuildProbeTest class is designed to provide basic verification mechanisms
 * within a testing framework. It includes methods that act as "canary" tests
 * to ensure the proper setup and operational readiness of the test environment.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
class BuildProbeTest {
    /**
     * A test method intended to verify basic operational functionality or act as a "canary"
     * test within the test suite. It outputs a predefined confirmation message to ensure
     * that the testing framework is functioning correctly.
     */
    @Test
    void mineCanary() {
        System.out.println("Yes, you can breathe!");
    }
}
