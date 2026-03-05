package com.selegic.encye.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.selegic.encye.data.local.entity.TrainingEntity

@Dao
interface TrainingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(trainings: List<TrainingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(training: TrainingEntity)

    @Query("SELECT * FROM trainings ORDER BY createdAt DESC")
    suspend fun getAll(): List<TrainingEntity>

    @Query("SELECT * FROM trainings WHERE id = :id")
    suspend fun getById(id: String): TrainingEntity?

    @Query("DELETE FROM trainings")
    suspend fun clearAll()

    @Query("SELECT MAX(cachedAtEpochMillis) FROM trainings")
    suspend fun getLatestCacheTimestamp(): Long?
}
