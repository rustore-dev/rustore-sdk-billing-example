package ru.rustore.sdk.billingexample.util

import kotlinx.coroutines.suspendCancellableCoroutine
import ru.rustore.sdk.core.tasks.Task
import kotlin.coroutines.resume

suspend fun <T> Task<T>.wrapInCoroutine(): Result<T> {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) {
                continuation.resume(Result.success(result))
            }
        }
        .addOnFailureListener { throwable ->
            if (continuation.isActive) {
                continuation.resume(Result.failure(throwable))
            }
        }
        continuation.invokeOnCancellation {
            cancel()
        }
    }
}
