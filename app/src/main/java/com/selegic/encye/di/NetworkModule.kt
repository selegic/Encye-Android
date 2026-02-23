package com.selegic.encye.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.selegic.encye.BuildConfig
import com.selegic.encye.data.remote.ArticleApiService
import com.selegic.encye.data.remote.CommentApiService
import com.selegic.encye.data.remote.PostApiService
import com.selegic.encye.data.remote.UserApiService
import com.selegic.encye.data.repository.ArticleRepository
import com.selegic.encye.data.repository.ArticleRepositoryImpl
import com.selegic.encye.data.repository.CommentRepository
import com.selegic.encye.data.repository.CommentRepositoryImpl
import com.selegic.encye.data.repository.PostRepository
import com.selegic.encye.data.repository.PostRepositoryImpl
import com.selegic.encye.data.repository.UserRepository
import com.selegic.encye.data.repository.UserRepositoryImpl
import com.selegic.encye.util.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val contentType = "application/json".toMediaType()
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideArticleApiService(retrofit: Retrofit): ArticleApiService {
        return retrofit.create(ArticleApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideArticleRepository(articleApiService: ArticleApiService): ArticleRepository {
        return ArticleRepositoryImpl(articleApiService)
    }

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserRepository(userApiService: UserApiService): UserRepository {
        return UserRepositoryImpl(userApiService)
    }

    @Provides
    @Singleton
    fun providePostApiService(retrofit: Retrofit): PostApiService {
        return retrofit.create(PostApiService::class.java)
    }
    @Provides
    @Singleton
    fun providePostRepository(postApiService: PostApiService): PostRepository {
        return PostRepositoryImpl(postApiService)
    }

    @Provides
    @Singleton
    fun provideCommentApiService(retrofit: Retrofit): CommentApiService {
        return retrofit.create(CommentApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCommentRepository(commentApiService: CommentApiService): CommentRepository {
        return CommentRepositoryImpl(commentApiService)
    }
}
