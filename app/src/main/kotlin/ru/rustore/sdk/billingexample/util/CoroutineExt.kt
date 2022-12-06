package ru.rustore.sdk.billingexample.util

import kotlinx.coroutines.suspendCancellableCoroutine
import ru.rustore.sdk.core.tasks.OnCompleteListener
import ru.rustore.sdk.core.tasks.Task
import kotlin.coroutines.resume

suspend fun <T> Task<T>.wrapInCoroutine(): Result<T> {
    return suspendCancellableCoroutine { continuation ->
        addOnCompleteListener(object : OnCompleteListener<T> {

            override fun onSuccess(result: T) {
                if (continuation.isActive) {
                    continuation.resume(Result.success(result))
                }
            }

            override fun onFailure(throwable: Throwable) {
                if (continuation.isActive) {
                    continuation.resume(Result.failure(throwable))
                }
            }
        })

        continuation.invokeOnCancellation {
            cancel()
        }

    }
}
