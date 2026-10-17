package com.drape.app.core.di

import com.drape.app.BuildConfig
import com.drape.app.core.data.remote.DrapeAiStylistApi
import com.drape.app.core.data.remote.DrapeAiStylistEngine
import com.drape.app.core.network.DrapeConnectivityMonitor
import com.drape.app.core.security.DrapeSecurityPolicy
import com.drape.app.core.security.ProxyEndpointValidationResult
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    /**
     * Keyless client attestation interceptor.
     * Attaches non-secret client metadata headers and blocks any request that accidentally
     * contains a raw third-party AI API key.
     */
    @Provides
    @Singleton
    fun provideKeylessSecurityInterceptor(): Interceptor = Interceptor { chain ->
        val original = chain.request()
        val urlString = original.url.toString()
        if (DrapeSecurityPolicy.containsForbiddenClientSecret(urlString)) {
            throw SecurityException("Blocked outgoing request containing forbidden client API secret in URL.")
        }
        val request = original.newBuilder()
            .header("X-Drape-Application-Id", BuildConfig.APPLICATION_ID)
            .header("X-Drape-Client-Version", BuildConfig.VERSION_NAME)
            .header("X-Drape-Request-Nonce", UUID.randomUUID().toString())
            .build()
        chain.proceed(request)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        securityInterceptor: Interceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(securityInterceptor)
        .connectTimeout(4500, TimeUnit.MILLISECONDS)
        .readTimeout(4500, TimeUnit.MILLISECONDS)
        .writeTimeout(4500, TimeUnit.MILLISECONDS)
        .build()

    @Provides
    @Singleton
    fun provideDrapeAiStylistApi(
        okHttpClient: OkHttpClient,
        json: Json
    ): DrapeAiStylistApi {
        val validation = DrapeSecurityPolicy.validateProxyEndpointUrl(BuildConfig.AI_PROXY_BASE_URL)
        val safeBaseUrl = when (validation) {
            is ProxyEndpointValidationResult.ValidRemoteProxy -> validation.normalizedHttpsUrl
            else -> "https://api.drape-stylist.example.com/"
        }
        return Retrofit.Builder()
            .baseUrl(safeBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(DrapeAiStylistApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDrapeAiStylistEngine(
        api: DrapeAiStylistApi,
        connectivityMonitor: DrapeConnectivityMonitor
    ): DrapeAiStylistEngine {
        val validation = DrapeSecurityPolicy.validateProxyEndpointUrl(BuildConfig.AI_PROXY_BASE_URL)
        val isValidRemote = validation is ProxyEndpointValidationResult.ValidRemoteProxy
        return DrapeAiStylistEngine(
            remoteApi = api,
            isRemoteEndpointConfigured = isValidRemote,
            isOnlineProvider = connectivityMonitor::isCurrentlyOnline
        )
    }
}
