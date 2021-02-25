package hr.vvidovic.aqisds011.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MeasurementDao {
    @Query("SELECT * FROM measurement")
    List<Measurement> getAll();

    @Insert
    void insertAll(Measurement... measurements);
}
