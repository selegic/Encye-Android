package com.selegic.encye.di

import com.selegic.encye.data.repository.PostRepository
import com.selegic.encye.data.repository.fake.FakePostRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        repository: FakePostRepository
    ): PostRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
