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
AQI calculation & coloring adapted from:
https://github.com/zefanja/aqi/blob/master/html/aqi.js
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
        brown(300, R.color.brown),
        purple(200,R.color.purple),
        red(150,R.color.red),
        orange(100,R.color.orange),
        yellow(50, R.color.yellow),
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

    private static int calcAQIpm25(float pm25) {
        double pm1 = 0.0;
        double pm2 = 12.0;
        double pm3 = 35.4;
        double pm4 = 55.4;
        double pm5 = 150.4;
        double pm6 = 250.4;
        double pm7 = 350.4;
        double pm8 = 500.4;

        double aqi1 = 0.0;
        double aqi2 = 50.0;
        double aqi3 = 100.0;
        double aqi4 = 150.0;
        double aqi5 = 200.0;
        double aqi6 = 300.0;
        double aqi7 = 400.0;
        double aqi8 = 500.0;

        double aqipm25 = 0.0;

        if (pm25 >= pm1 && pm25 <= pm2) {
            aqipm25 = ((aqi2 - aqi1) / (pm2 - pm1)) * (pm25 - pm1) + aqi1;
        } else if (pm25 >= pm2 && pm25 <= pm3) {
            aqipm25 = ((aqi3 - aqi2) / (pm3 - pm2)) * (pm25 - pm2) + aqi2;
        } else if (pm25 >= pm3 && pm25 <= pm4) {
            aqipm25 = ((aqi4 - aqi3) / (pm4 - pm3)) * (pm25 - pm3) + aqi3;
        } else if (pm25 >= pm4 && pm25 <= pm5) {
            aqipm25 = ((aqi5 - aqi4) / (pm5 - pm4)) * (pm25 - pm4) + aqi4;
        } else if (pm25 >= pm5 && pm25 <= pm6) {
            aqipm25 = ((aqi6 - aqi5) / (pm6 - pm5)) * (pm25 - pm5) + aqi5;
        } else if (pm25 >= pm6 && pm25 <= pm7) {
            aqipm25 = ((aqi7 - aqi6) / (pm7 - pm6)) * (pm25 - pm6) + aqi6;
        } else if (pm25 >= pm7 && pm25 <= pm8) {
            aqipm25 = ((aqi8 - aqi7) / (pm8 - pm7)) * (pm25 - pm7) + aqi7;
        }

        return (int)Math.round(aqipm25);
    }

    private static int calcAQIpm10(float pm10) {
        double pm1 = 0.0;
        double pm2 = 54.0;
        double pm3 = 154.0;
        double pm4 = 254.0;
        double pm5 = 354.0;
        double pm6 = 424.0;
        double pm7 = 504.0;
        double pm8 = 604.0;

        double aqi1 = 0.0;
        double aqi2 = 50.0;
        double aqi3 = 100.0;
        double aqi4 = 150.0;
        double aqi5 = 200.0;
        double aqi6 = 300.0;
        double aqi7 = 400.0;
        double aqi8 = 500.0;

        double aqipm10 = 0.0;

        if (pm10 >= pm1 && pm10 <= pm2) {
            aqipm10 = ((aqi2 - aqi1) / (pm2 - pm1)) * (pm10 - pm1) + aqi1;
        } else if (pm10 >= pm2 && pm10 <= pm3) {
            aqipm10 = ((aqi3 - aqi2) / (pm3 - pm2)) * (pm10 - pm2) + aqi2;
        } else if (pm10 >= pm3 && pm10 <= pm4) {
            aqipm10 = ((aqi4 - aqi3) / (pm4 - pm3)) * (pm10 - pm3) + aqi3;
        } else if (pm10 >= pm4 && pm10 <= pm5) {
            aqipm10 = ((aqi5 - aqi4) / (pm5 - pm4)) * (pm10 - pm4) + aqi4;
        } else if (pm10 >= pm5 && pm10 <= pm6) {
            aqipm10 = ((aqi6 - aqi5) / (pm6 - pm5)) * (pm10 - pm5) + aqi5;
        } else if (pm10 >= pm6 && pm10 <= pm7) {
            aqipm10 = ((aqi7 - aqi6) / (pm7 - pm6)) * (pm10 - pm6) + aqi6;
        } else if (pm10 >= pm7 && pm10 <= pm8) {
            aqipm10 = ((aqi8 - aqi7) / (pm8 - pm7)) * (pm10 - pm7) + aqi7;
        }
        return (int)Math.round(aqipm10);
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
