package tf.veriny.keymountain.api.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Runs the specified [fn] in the background, whilst running [ours] alongside it. The runnable will
 * be ran inside a virtual thread, and this function will wait for it to complete before returning.
 */
public inline fun <R> runningInBackground(name: String, fn: Runnable, ours: () -> R): R {
    val thread = Thread.ofVirtual().name(name).start(fn)

    try {
        return ours()
    } finally {
        thread.join()
    }
}