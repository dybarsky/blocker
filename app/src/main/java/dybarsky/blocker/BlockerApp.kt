package dybarsky.blocker

import android.app.Application
import dybarsky.blocker.di.Injection

class BlockerApp : Application() {

    companion object {
        lateinit var instance: BlockerApp private set
    }

    val injection by lazy { Injection(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
