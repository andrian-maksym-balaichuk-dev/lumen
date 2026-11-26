package sk.mpage.lumen.core.network.di

import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import sk.mpage.lumen.core.network.BuildConfig
import sk.mpage.lumen.core.network.OpenTdbApi
import sk.mpage.lumen.core.network.TaggedSocketFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
    @Provides
    @Singleton
    fun json(): Json = openTdbJson()

    @Provides
    @Singleton
    fun okHttp(): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
            .socketFactory(TaggedSocketFactory())
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
            }.build()

    @Provides
    @Singleton
    fun api(
        @OpenTdbBaseUrl baseUrl: String,
        client: Lazy<OkHttpClient>,
        json: Json,
    ): OpenTdbApi = openTdbApi(baseUrl.toHttpUrl(), { request -> client.get().newCall(request) }, json)
}

internal fun openTdbJson(): Json =
    Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

internal fun openTdbApi(
    baseUrl: HttpUrl,
    callFactory: Call.Factory,
    json: Json,
): OpenTdbApi =
    Retrofit
        .Builder()
        .baseUrl(baseUrl)
        .callFactory(callFactory)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create()
