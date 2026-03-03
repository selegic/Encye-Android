package com.selegic.encye.di

import android.content.Context
import androidx.room.Room
import com.selegic.encye.data.local.AppDatabase
import com.selegic.encye.data.local.dao.ArticleDao
import com.selegic.encye.data.local.dao.ArticleRemoteKeyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "encye_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideArticleDao(database: AppDatabase): ArticleDao {
        return database.articleDao
    }

    @Provides
    @Singleton
    fun provideArticleRemoteKeyDao(database: AppDatabase): ArticleRemoteKeyDao {
        return database.articleRemoteKeyDao
    }
}
