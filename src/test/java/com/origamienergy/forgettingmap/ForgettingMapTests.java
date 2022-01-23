package com.origamienergy.forgettingmap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ForgettingMapTests {

    @Test
    public void testThatApplicationRuns(){
        ForgettingMap map = new ForgettingMap();
        assertNotNull(map);
    }

}
