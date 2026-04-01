package com.neonnoir.di

import com.neonnoir.BuildConfig
import com.neonnoir.Data.remote.api.OmdbApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Intercepts every request and appends the OMDB API key as a query param
    private val apiKeyInterceptor = Interceptor { chain ->
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("apikey", BuildConfig.OMDB_API_KEY)
            .build()
        chain.proceed(original.newBuilder().url(url).build())
    }

    // Provides the OkHttpClient with API key and logging interceptors
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BODY
                else
                    HttpLoggingInterceptor.Level.NONE
            })
            .build()

    // Provides the Retrofit instance pointed at OMDB base URL
    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://www.omdbapi.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // Provides the OmdbApiService created from the Retrofit instance
    @Provides
    @Singleton
    fun provideOmdbApiService(retrofit: Retrofit): OmdbApiService =
        retrofit.create(OmdbApiService::class.java)
}