package wee.vietinbank.kiosk

import android.app.Application
import wee.digital.camera.RealSense
import wee.digital.finger.HeroFun
import wee.digital.library.Library

class App : Application() {

    companion object {

        lateinit var instance: App private set
    }

    private fun onModulesInject() {
        instance = this
        Library.app = this
        HeroFun.app = this
        RealSense.app = this
        RealSense.initOpenCV()
    }

    /**
     * [Application implement
     */
    override fun onCreate() {
        super.onCreate()
        onModulesInject()

    }

    override fun onTerminate() {
        super.onTerminate()
        RealSense.stop()
    }

}