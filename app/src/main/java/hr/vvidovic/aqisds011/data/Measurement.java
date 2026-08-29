package hr.vvidovic.aqisds011.data;

import android.graphics.Color;
import android.location.Location;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.time.Instant;

import hr.vvidovic.aqisds011.R;

/*
US EPA AQI breakpoint and interpolation rules based on 40 CFR Part 58, Appendix G.
The PM2.5 breakpoints reflect the 2024 EPA revision effective May 6, 2024.
The caller supplies the particulate concentration; this class does not calculate a 24-hour average or NowCast.
*/
@Entity
public class Measurement {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "date_time")
    public long dateTime;
    @ColumnInfo(name = "pm_25")
    public float pm25;
    @ColumnInfo(name = "pm_10")
    public float pm10;
    @ColumnInfo(name = "loc_accuracy")
    public float locAccuracy;
    @ColumnInfo(name = "loc_time")
    public long locDateTime;
    @ColumnInfo(name = "loc_latitude")
    public double locLatitude;
    @ColumnInfo(name = "loc_longitude")
    public double locLongitude;

    public Measurement() {
    }

    @Ignore
    public Measurement(float pm25, float pm10) {
        this.dateTime = Instant.now().toEpochMilli();
        this.pm25 = pm25;
        this.pm10 = pm10;
    }

    @Ignore
    public Measurement(float pm25, float pm10, Location location) {
        this(pm25, pm10);
        if(location != null) {
            this.locAccuracy = location.getAccuracy();
            this.locDateTime = location.getTime();
            this.locLatitude = location.getLatitude();
            this.locLongitude = location.getLongitude();
        }
    }

    public int aqiPm25() {
        return calcAQIpm25(pm25);
    }

    public Category aqiPm25Category() {
        return Category.fromAqi(calcAQIpm25(pm25));
    }

    public Category aqiPm10Category() {
        return Category.fromAqi(calcAQIpm10(pm10));
    }

    public int aqiPm10() {
        return calcAQIpm10(pm10);
    }

    public boolean hasLocation() {
        return locDateTime > 0;
    }

    public enum Category {
        brown(301, R.color.brown),
        purple(201,R.color.purple),
        red(151,R.color.red),
        orange(101,R.color.orange),
        yellow(51, R.color.yellow),
        green(0, R.color.green),
        ;

        private final int aqiAtLeast;
        public final int color;

        Category(int aqiAtLeast, String color) {
            this.aqiAtLeast = aqiAtLeast;
            this.color = Color.parseColor(color);
        }

        Category(int aqiAtLeast, int color) {
            this.aqiAtLeast = aqiAtLeast;
            this.color = color;
        }

        public static Category fromAqi(int aqi) {
            for (Category c: Category.values()) {
                if(aqi >= c.aqiAtLeast) {
                    return c;
                }
            }
            return green;
        }
    }

    private static final double[][] PM25_BREAKPOINTS = {
            {0.0, 9.0, 0, 50},
            {9.1, 35.4, 51, 100},
            {35.5, 55.4, 101, 150},
            {55.5, 125.4, 151, 200},
            {125.5, 225.4, 201, 300},
            {225.5, 325.4, 301, 500}
    };

    private static final double[][] PM10_BREAKPOINTS = {
            {0, 54, 0, 50},
            {55, 154, 51, 100},
            {155, 254, 101, 150},
            {255, 354, 151, 200},
            {355, 424, 201, 300},
            {425, 604, 301, 500}
    };

    private static int calcAQIpm25(float pm25) {
        // EPA requires PM2.5 concentrations to be truncated to one decimal place.
        double concentration = truncate(Math.max(0.0, pm25), 10.0);
        return calculateAqi(concentration, PM25_BREAKPOINTS);
    }

    private static int calcAQIpm10(float pm10) {
        // EPA requires PM10 concentrations to be truncated to an integer.
        double concentration = Math.floor(Math.max(0.0, pm10));
        return calculateAqi(concentration, PM10_BREAKPOINTS);
    }

    private static double truncate(double value, double scale) {
        return Math.floor(value * scale) / scale;
    }

    private static int calculateAqi(double concentration, double[][] breakpoints) {
        for (double[] breakpoint : breakpoints) {
            if (concentration >= breakpoint[0] && concentration <= breakpoint[1]) {
                return interpolateAqi(concentration, breakpoint);
            }
        }

        // Appendix G permits concentrations above the highest breakpoint to be
        // calculated using the last two breakpoints (AQI may therefore exceed 500).
        return interpolateAqi(concentration, breakpoints[breakpoints.length - 1]);
    }

    private static int interpolateAqi(double concentration, double[] breakpoint) {
        double concentrationLow = breakpoint[0];
        double concentrationHigh = breakpoint[1];
        double aqiLow = breakpoint[2];
        double aqiHigh = breakpoint[3];
        double aqi = ((aqiHigh - aqiLow) / (concentrationHigh - concentrationLow))
                * (concentration - concentrationLow) + aqiLow;
        return (int) Math.round(aqi);
    }

    @Override
    public String toString() {
        return "Measurement{" +
                "id=" + id +
                ", dateTime=" + dateTime +
                ", pm25=" + pm25 +
                ", pm10=" + pm10 +
                ", locAccuracy=" + locAccuracy +
                ", locLatitude=" + locLatitude +
                ", locLongitude=" + locLongitude +
                ", locDateTime=" + locDateTime +
                '}';
    }
}
