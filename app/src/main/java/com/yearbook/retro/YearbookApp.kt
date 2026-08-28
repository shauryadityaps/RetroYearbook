package com.yearbook.retro

import android.app.Application
import com.yearbook.retro.data.remote.SupabaseRestSource
import com.yearbook.retro.data.remote.SupabaseStorageSource
import com.yearbook.retro.data.repository.AuthRepositoryImpl
import com.yearbook.retro.data.repository.PhotoRepositoryImpl
import com.yearbook.retro.data.repository.YearbookRepositoryImpl
import com.yearbook.retro.domain.repository.AuthRepository
import com.yearbook.retro.domain.repository.PhotoRepository
import com.yearbook.retro.domain.repository.YearbookRepository
import com.yearbook.retro.util.NetworkObserver
import com.yearbook.retro.worker.ReminderScheduler

class AppContainer(val applicationContext: Application) {
    val networkObserver by lazy { NetworkObserver(applicationContext) }
    val supabaseRest by lazy { SupabaseRestSource() }
    val supabaseStorage by lazy { SupabaseStorageSource() }

    val photoRepository: PhotoRepository by lazy {
        PhotoRepositoryImpl(applicationContext, supabaseRest, supabaseStorage)
    }

    val yearbookRepository: YearbookRepository by lazy {
        YearbookRepositoryImpl(supabaseRest) { photoRepository }
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(applicationContext, supabaseRest)
    }
}

class YearbookApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderScheduler.scheduleDailyReminder(this)
    }
}
