package com.selegic.encye.util

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            sessionManager.getAuthToken()
        }
        val request = chain.request().newBuilder()
        token?.let {
            request.addHeader("Authorization", "Bearer $it")
        }
        return chain.proceed(request.build())
    }
}