package dybarsky.blocker.common.util

import kotlin.coroutines.cancellation.CancellationException

/**
 * Like [runCatching], but never swallows [CancellationException]:
 * coroutine cancellation must propagate to keep structured concurrency intact.
 */
inline fun <T> runCatchingSuspend(block: () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
