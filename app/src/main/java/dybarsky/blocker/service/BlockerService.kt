package dybarsky.blocker.service

import android.telecom.Call
import android.telecom.CallScreeningService
import dybarsky.blocker.BlockerApp
import kotlinx.coroutines.runBlocking

class BlockerService : CallScreeningService() {

    private val serviceModel = BlockerApp
        .instance
        .injection
        .serviceModelFactory
        .obtain(BlockerServiceModel::class.java)

    override fun onScreenCall(callDetails: Call.Details) {
        val builder = CallResponse.Builder()
        val number = callDetails.handle.schemeSpecificPart
        val reject = runBlocking { serviceModel.shouldBlock(number) }
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
