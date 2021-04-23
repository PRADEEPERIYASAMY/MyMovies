package com.example.mymovies.api

import com.example.mymovies.utils.constants.Constants
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object ApiModule {

    const val TAG = "MOVIE_API"

    @Provides
    @Singleton
    fun getInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    @Provides
    @Singleton
    fun createGson(): Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        .enableComplexMapKeySerialization()
        .create()

    @Provides
    @Singleton
    fun createOkhttpClient(interceptor: HttpLoggingInterceptor): OkHttpClient = OkHttpClient()
        .newBuilder()
        .addInterceptor(interceptor)
        .build()

    @Provides
    @Singleton
    fun buildRetrofit(
        okhttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit = Retrofit.Builder()
        .client(okhttpClient)
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun fetchApi(retrofit: Retrofit): ApiInterface = retrofit.create(ApiInterface::class.java)
}
