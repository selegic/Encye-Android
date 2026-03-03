package com.selegic.encye.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.selegic.encye.data.local.entity.ArticleRemoteKey

@Dao
interface ArticleRemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<ArticleRemoteKey>)

    @Query("SELECT * FROM article_remote_keys WHERE id = :id")
    suspend fun remoteKeyId(id: String): ArticleRemoteKey?

    @Query("DELETE FROM article_remote_keys")
    suspend fun clearRemoteKeys(): Int
}
