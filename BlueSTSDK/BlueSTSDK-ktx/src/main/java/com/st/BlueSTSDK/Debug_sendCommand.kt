package com.st.BlueSTSDK

import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.util.concurrent.TimeoutException

data class ResponseWaitingConfig(val timeout: Long = 1000L, val isComplete: (StringBuffer) -> Boolean)


private class ConsoleCancellableListener(
        private val console: Debug,
        private val continuation: CancellableContinuation<String>,
        private val responseWaitingConfig: ResponseWaitingConfig
) : Debug.DebugOutputListener {

    private val answerBuffer = StringBuffer()

    private val mTimeout = Handler(Looper.getMainLooper())

    private val onTimeout: Runnable = Runnable {
        console.removeDebugOutputListener(this)
        continuation.resumeWith(Result.failure(TimeoutException()))
    }

    override fun onStdErrReceived(debug: Debug, message: String) {
        Log.d("DebugConsole", "error: $message")
        debug.removeDebugOutputListener(this)
        mTimeout.removeCallbacks(onTimeout)
        continuation.resumeWith(Result.failure(Exception(message)))
    }

    override fun onStdOutReceived(debug: Debug, message: String) {
        Log.d("DebugConsole", "received: $message")

        //reset the timeout
        mTimeout.removeCallbacks(onTimeout)
        answerBuffer.append(message)
        if (responseWaitingConfig.isComplete(answerBuffer)) {
            debug.removeDebugOutputListener(this)
            Log.d("DebugConsole", "complete: $answerBuffer")
            continuation.resumeWith(Result.success(answerBuffer.toString()))
        } else {
            mTimeout.postDelayed(onTimeout, responseWaitingConfig.timeout)
        }
    }

    override fun onStdInSent(debug: Debug, message: String, writeResult: Boolean) {
        Log.d("DebugConsole", "sent: $message")
        if (!writeResult) {
            debug.removeDebugOutputListener(this)
            mTimeout.removeCallbacks(onTimeout)
            continuation.resumeWith(Result.failure(IOException("Error sending the value: $message")))
        } else {
            mTimeout.postDelayed(onTimeout, responseWaitingConfig.timeout)
        }
    }
}


suspend fun Debug.sendCommand(cmd: String, answerComplete: ResponseWaitingConfig): String? {
    return try {
        suspendCancellableCoroutine<String> { continuation ->
            val listener = ConsoleCancellableListener(this, continuation, answerComplete)
            addDebugOutputListener(listener)
            write(cmd)
            continuation.invokeOnCancellation {
                Log.d("DebugConsole", "remove listener")
                removeDebugOutputListener(listener)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("Debug", "error :$e")
        null
    }

}