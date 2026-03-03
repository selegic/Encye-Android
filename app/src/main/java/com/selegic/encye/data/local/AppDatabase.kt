package com.selegic.encye.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.selegic.encye.data.local.converter.Converters
import com.selegic.encye.data.local.dao.ArticleDao
import com.selegic.encye.data.local.dao.ArticleRemoteKeyDao
import com.selegic.encye.data.local.entity.ArticleEntity
import com.selegic.encye.data.local.entity.ArticleRemoteKey

@Database(
    entities = [ArticleEntity::class, ArticleRemoteKey::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val articleDao: ArticleDao
    abstract val articleRemoteKeyDao: ArticleRemoteKeyDao
}
