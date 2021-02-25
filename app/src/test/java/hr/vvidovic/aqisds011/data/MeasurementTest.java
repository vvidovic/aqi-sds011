package hr.vvidovic.aqisds011.data;

import org.junit.Test;

import static org.junit.Assert.*;

public class MeasurementTest {
    @Test
    public void testColorFromAqi() {
        assertEquals(Measurement.Color.brown, Measurement.Color.fromAqi(1000));
        assertEquals(Measurement.Color.brown, Measurement.Color.fromAqi(300));
        assertEquals(Measurement.Color.purple, Measurement.Color.fromAqi(299));
        assertEquals(Measurement.Color.purple, Measurement.Color.fromAqi(200));
        assertEquals(Measurement.Color.red, Measurement.Color.fromAqi(199));
        assertEquals(Measurement.Color.red, Measurement.Color.fromAqi(150));
        assertEquals(Measurement.Color.orange, Measurement.Color.fromAqi(149));
        assertEquals(Measurement.Color.orange, Measurement.Color.fromAqi(100));
        assertEquals(Measurement.Color.yellow, Measurement.Color.fromAqi(99));
        assertEquals(Measurement.Color.yellow, Measurement.Color.fromAqi(50));
        assertEquals(Measurement.Color.green, Measurement.Color.fromAqi(49));
        assertEquals(Measurement.Color.green, Measurement.Color.fromAqi(0));
    }
}
