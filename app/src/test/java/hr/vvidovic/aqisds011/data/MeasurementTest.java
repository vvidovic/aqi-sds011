package hr.vvidovic.aqisds011.data;

import org.junit.Test;

import static org.junit.Assert.*;

public class MeasurementTest {
    @Test
    public void testColorFromAqi() {
        assertEquals(Measurement.Category.brown, Measurement.Category.fromAqi(1000));
        assertEquals(Measurement.Category.brown, Measurement.Category.fromAqi(300));
        assertEquals(Measurement.Category.purple, Measurement.Category.fromAqi(299));
        assertEquals(Measurement.Category.purple, Measurement.Category.fromAqi(200));
        assertEquals(Measurement.Category.red, Measurement.Category.fromAqi(199));
        assertEquals(Measurement.Category.red, Measurement.Category.fromAqi(150));
        assertEquals(Measurement.Category.orange, Measurement.Category.fromAqi(149));
        assertEquals(Measurement.Category.orange, Measurement.Category.fromAqi(100));
        assertEquals(Measurement.Category.yellow, Measurement.Category.fromAqi(99));
        assertEquals(Measurement.Category.yellow, Measurement.Category.fromAqi(50));
        assertEquals(Measurement.Category.green, Measurement.Category.fromAqi(49));
        assertEquals(Measurement.Category.green, Measurement.Category.fromAqi(0));
    }
}
