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
        val number = callDetails.handle?.schemeSpecificPart
        // Fail open: any problem (hidden number, storage error) must allow the call.
        val verdict = try {
            runBlocking { serviceModel.screen(number) }
        } catch (e: Exception) {
            Verdict.Allow
        }
        val response = if (verdict is Verdict.Block) builder.reject() else builder.allow()
        respondToCall(callDetails, response)
        // Log only after responding so the call is never delayed by (or dependent on) logging.
        try {
            runBlocking { serviceModel.record(number, verdict) }
        } catch (e: Exception) {
            // Logging is best effort.
        }
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
