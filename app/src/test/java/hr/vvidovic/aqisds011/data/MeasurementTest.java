package hr.vvidovic.aqisds011.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MeasurementTest {
    @Test
    public void testColorFromAqi() {
        assertEquals(Measurement.Category.brown, Measurement.Category.fromAqi(1000));
        assertEquals(Measurement.Category.brown, Measurement.Category.fromAqi(301));
        assertEquals(Measurement.Category.purple, Measurement.Category.fromAqi(300));
        assertEquals(Measurement.Category.purple, Measurement.Category.fromAqi(201));
        assertEquals(Measurement.Category.red, Measurement.Category.fromAqi(200));
        assertEquals(Measurement.Category.red, Measurement.Category.fromAqi(151));
        assertEquals(Measurement.Category.orange, Measurement.Category.fromAqi(150));
        assertEquals(Measurement.Category.orange, Measurement.Category.fromAqi(101));
        assertEquals(Measurement.Category.yellow, Measurement.Category.fromAqi(100));
        assertEquals(Measurement.Category.yellow, Measurement.Category.fromAqi(51));
        assertEquals(Measurement.Category.green, Measurement.Category.fromAqi(50));
        assertEquals(Measurement.Category.green, Measurement.Category.fromAqi(0));
    }

    @Test
    public void testPm25UsesCurrentEpaBreakpoints() {
        assertPm25Aqi(0.0f, 0);
        assertPm25Aqi(9.0f, 50);
        assertPm25Aqi(9.1f, 51);
        assertPm25Aqi(35.4f, 100);
        assertPm25Aqi(35.5f, 101);
        assertPm25Aqi(55.4f, 150);
        assertPm25Aqi(55.5f, 151);
        assertPm25Aqi(125.4f, 200);
        assertPm25Aqi(125.5f, 201);
        assertPm25Aqi(225.4f, 300);
        assertPm25Aqi(225.5f, 301);
        assertPm25Aqi(325.4f, 500);
    }

    @Test
    public void testPm25ConcentrationIsTruncatedToOneDecimalPlace() {
        assertPm25Aqi(9.09f, 50);
        assertPm25Aqi(9.19f, 51);
        assertPm25Aqi(35.49f, 100);
        assertPm25Aqi(35.59f, 101);
    }

    @Test
    public void testPm10UsesCurrentEpaBreakpointsAndIntegerTruncation() {
        assertPm10Aqi(54.99f, 50);
        assertPm10Aqi(55.99f, 51);
        assertPm10Aqi(154.99f, 100);
        assertPm10Aqi(155.99f, 101);
        assertPm10Aqi(424.99f, 300);
        assertPm10Aqi(425.99f, 301);
        assertPm10Aqi(604.99f, 500);
    }

    @Test
    public void testHazardousRangeUsesSingleCurrentEpaSegment() {
        assertPm10Aqi(504.0f, 389);
        assertPm25Aqi(275.0f, 400);
    }

    @Test
    public void testAqiCanExceed500UsingLastBreakpointSegment() {
        assertPm10Aqi(605.0f, 501);
        assertPm25Aqi(400.0f, 649);
    }

    @Test
    public void testNegativeSensorValuesAreClampedToZero() {
        assertPm25Aqi(-1.0f, 0);
        assertPm10Aqi(-1.0f, 0);
    }

    private static void assertPm25Aqi(float concentration, int expectedAqi) {
        assertEquals(expectedAqi, new Measurement(concentration, 0).aqiPm25());
    }

    private static void assertPm10Aqi(float concentration, int expectedAqi) {
        assertEquals(expectedAqi, new Measurement(0, concentration).aqiPm10());
    }
}
