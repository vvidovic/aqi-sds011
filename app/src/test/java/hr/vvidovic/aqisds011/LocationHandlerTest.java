package hr.vvidovic.aqisds011;

import android.content.Context;

import org.junit.Test;

import static org.junit.Assert.*;

public class LocationHandlerTest {
    @Test
    public void calculateLocationUpdateInterval_test() {
        final long[] saveMeasurementIntervalMillisValues = {
                20, 2000, 10000, 12000, 30000, 50000, 56000, 100000,130000, 140000, 200000, 600000
        };
        final long[] locationIntervalValues = {
                10000, 10000, 10000, 11000, 20000, 30000, 32000, 46666, 56666, 60000, 60000, 60000
        };

        LocationHandler locationHandler = new LocationHandler();

        for (int idx = 0; idx < saveMeasurementIntervalMillisValues.length; idx++) {
            long saveInterval = saveMeasurementIntervalMillisValues[idx];
            long locationIntervalExpected = locationIntervalValues[idx];
            final long locationInterval =
                    locationHandler.calculateLocationUpdateInterval(saveInterval);
            assertEquals(String.format("Save interval: %d", saveInterval),
                    locationIntervalExpected, locationInterval);
        }
    }
}
