package gbench.sandbox.junit;

import static gbench.util.io.Output.println;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class StandardJunit {
    @BeforeAll
    static void initAll() {
        println("initAll");
    }

    @BeforeEach
    void init() {
        println("init");
    }

    @Test
    void succeedingTest() {
        println("succeedingTest");
    }

    @Test
    void failingTest() {
        assumeTrue("abc".contains("Z"));
        fail("a failing test");
    }

    @Test
    @Disabled("for demonstration purposes")
    void skippedTest() {
        println("skippedTest");
        // not executed
    }

    @Test
    void abortedTest() {
        println("abortedTest");
        fail("test should have been aborted");
    }

    @AfterEach
    void tearDown() {
        println("tearDown");
    }

    @AfterAll
    static void tearDownAll() {
        println("tearDownAll");
    }
}
