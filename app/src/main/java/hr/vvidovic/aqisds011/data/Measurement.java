package hr.vvidovic.aqisds011.data;

import android.graphics.Color;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.time.Instant;

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

    public Measurement() {
    }

    @Ignore
    public Measurement(float pm25, float pm10) {
        this.dateTime = Instant.now().toEpochMilli();
        this.pm25 = pm25;
        this.pm10 = pm10;
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

    public enum Category {
        brown(300, "#592F2a"),
        purple(200,"#89609E"),
        red(150,"#870202"),
        orange(100,"#E85C1C"),
        yellow(50,"#F3D111"),
        green(0,"#749B4F");

        private final int aqiAtLeast;
        public final int color;

        Category(int aqiAtLeast, String color) {
            this.aqiAtLeast = aqiAtLeast;
            this.color = Color.parseColor(color);
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
}
