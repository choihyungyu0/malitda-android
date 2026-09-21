package kr.voicemate.malitda

import android.app.Application
import kr.voicemate.malitda.di.AppContainer

class MalitdaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
