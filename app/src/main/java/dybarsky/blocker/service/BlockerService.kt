package dybarsky.blocker.service

import android.telecom.Call
import android.telecom.CallScreeningService
import dybarsky.blocker.BlockerApp
import kotlinx.coroutines.runBlocking

class BlockerService : CallScreeningService() {

    private val serviceModel by lazy {
        BlockerApp
            .instance
            .injection
            .serviceModelFactory
            .obtain(BlockerServiceModel::class.java)
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val builder = CallResponse.Builder()
        // Fail open: any problem (hidden number, storage error) must allow the call.
        val reject = try {
            val number = callDetails.handle?.schemeSpecificPart
            runBlocking { serviceModel.shouldBlock(number) }
        } catch (e: Exception) {
            false
        }
        val response = if (reject) builder.reject() else builder.allow()
        respondToCall(callDetails, response)
    }

    private fun CallResponse.Builder.reject(): CallResponse {
        return this
            .setRejectCall(true)
            .setDisallowCall(true)
            .setSkipCallLog(false)
            .setSkipNotification(true)
            .build()
    }

    private fun CallResponse.Builder.allow(): CallResponse {
        return build()
    }
}
