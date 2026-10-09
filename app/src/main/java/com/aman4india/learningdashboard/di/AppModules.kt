package com.aman4india.learningdashboard.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.aman4india.learningdashboard.data.local.AppDatabase
import com.aman4india.learningdashboard.data.local.CourseDao
import com.aman4india.learningdashboard.data.remote.AuthApi
import com.aman4india.learningdashboard.data.remote.ConnectivityNetworkMonitor
import com.aman4india.learningdashboard.data.remote.CourseApi
import com.aman4india.learningdashboard.data.remote.MockAuthApi
import com.aman4india.learningdashboard.data.remote.MockCourseApi
import com.aman4india.learningdashboard.data.remote.NetworkMonitor
import com.aman4india.learningdashboard.data.repository.AuthRepositoryImpl
import com.aman4india.learningdashboard.data.repository.CourseRepositoryImpl
import com.aman4india.learningdashboard.data.session.DataStoreSessionStore
import com.aman4india.learningdashboard.data.session.SessionStore
import com.aman4india.learningdashboard.domain.repository.AuthRepository
import com.aman4india.learningdashboard.domain.repository.CourseRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME).build()

    @Provides
    fun provideCourseDao(db: AppDatabase): CourseDao = db.courseDao()

    @Provides
    @Singleton
    fun provideSessionDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("session") }

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {

    @Binds
    abstract fun bindNetworkMonitor(impl: ConnectivityNetworkMonitor): NetworkMonitor

    @Binds
    abstract fun bindCourseApi(impl: MockCourseApi): CourseApi

    @Binds
    abstract fun bindAuthApi(impl: MockAuthApi): AuthApi

    @Binds
    abstract fun bindSessionStore(impl: DataStoreSessionStore): SessionStore

    @Binds
    @Singleton
    abstract fun bindCourseRepository(impl: CourseRepositoryImpl): CourseRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
