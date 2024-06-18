package asia.hombre.asyncqlitekt

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import androidx.sqlite.use
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * A wrapper for [SQLiteConnection] to provide asynchronous capabilities.
 *
 * All SQL calls from concurrent threads must go through this wrapper class to ensure serial access.
 *
 * @author Ron Lauren Hombre
 * @since 0.0.1
 */
class AsyncQLiteConnection private constructor(val synced: SQLiteConnection) {
    internal var closing: AtomicBoolean = AtomicBoolean(false)
    private var consumerThread: Thread? = null
    private val queue = LinkedBlockingQueue<Job>()

    init {
        tryStartConsumerThread()
    }

    /**
     * Tries to start the Consumer Thread if it has not been started prior or if it had previously died.
     */
    private fun tryStartConsumerThread() {
        if(consumerThread != null && consumerThread?.isAlive == true) {
            return
        }

        consumerThread = thread(start = true, name = "AsyncSQLiteConnection consumerThread", priority = Thread.MAX_PRIORITY) {
            while(queue.isNotEmpty() || !closing.get()) {
                val job = queue.take()
                if(job.bindingsMap == null) {
                    try {
                        synced.execSQL(job.statement)
                        job.onSuccess?.invoke(null)
                    } catch (exception: Exception) {
                        job.onSuccess?.invoke(exception)
                    }
                } else if(job.asyncQLiteStatement is AsyncQLiteStatement) {
                    try {
                        synced.prepare(job.statement).use {
                            job.bindingsMap.forEach { (index, value) ->
                                when(value) {
                                    is ByteArray -> it.bindBlob(index, value)
                                    is Boolean -> it.bindBoolean(index, value)
                                    is Double -> it.bindDouble(index, value)
                                    is Float -> it.bindFloat(index, value)
                                    is Int -> it.bindInt(index, value)
                                    is Long -> it.bindLong(index, value)
                                    is String -> it.bindText(index, value)
                                    else -> it.bindNull(index)
                                }
                            }
                            val gottenMap = mutableMapOf<Int, Any?>()
                            while(it.step()) {
                                job.gettingsMap?.forEach { (index, value) ->
                                    when(value) {
                                        AsyncQLiteStatement.Types.Blob -> gottenMap[index] = it.getBlob(index)
                                        AsyncQLiteStatement.Types.Boolean -> gottenMap[index] = it.getBoolean(index)
                                        AsyncQLiteStatement.Types.Double -> gottenMap[index] = it.getDouble(index)
                                        AsyncQLiteStatement.Types.Float -> gottenMap[index] = it.getFloat(index)
                                        AsyncQLiteStatement.Types.Int -> gottenMap[index] = it.getInt(index)
                                        AsyncQLiteStatement.Types.Long -> gottenMap[index] = it.getLong(index)
                                        AsyncQLiteStatement.Types.Null -> gottenMap[index] = if(it.isNull(index)) null else value
                                        AsyncQLiteStatement.Types.Text -> gottenMap[index] = it.getText(index)
                                    }
                                }
                                if(job.asyncQLiteStatement.onResult?.invoke(AsyncQLiteResult(gottenMap.toMap(), it.getColumnCount(), it.getColumnNames())) == false)
                                    break
                            }
                            job.onSuccess?.invoke(null)
                        }
                    } catch(exception: Exception) {
                        job.onSuccess?.invoke(exception)
                    }
                }
            }
        }

        consumerThread?.setUncaughtExceptionHandler { t, e ->
            consumerThread = null
        }
    }

    /**
     * Adds a Job to the Job Queue and tries to start the Consumer Thread.
     */
    internal fun addJob(job: Job) {
        queue.add(job)
        tryStartConsumerThread()
    }

    /**
     * Signals the Consumer Thread to die once the Job Queue is empty. This also prevents further addition to the queue.
     *
     * This is a blocking method that waits for the Consumer Thread before closing the wrapped [SQLiteConnection].
     *
     * @throws IllegalStateException when the Consumer Thread was unexpectedly stopped before the Job Queue was emptied.
     */
    fun close() {
        closing.set(true)
        if(queue.isEmpty()) consumerThread?.interrupt()
        if(consumerThread != null && consumerThread?.isAlive == true) consumerThread?.join()
        if(queue.isNotEmpty())
            throw IllegalStateException("Job queue was not empty when the connection was closed.")

        synced.close()

        listOfWrappedConnections.remove(synced)
    }

    /**
     * Internal data class to hold values relevant for the Job that the Consumer Thread will consume.
     */
    internal data class Job(val statement: String, val bindingsMap: Map<Int, Any?>?, val gettingsMap: Map<Int, AsyncQLiteStatement.Types>?, val asyncQLiteStatement: AsyncQLiteStatement?, val onSuccess: ((error: Exception?) -> Unit)? = null)

    companion object {
        private val listOfWrappedConnections = mutableListOf<SQLiteConnection>()
        /**
         * Wraps an [SQLiteConnection] to provide asynchronous capabilities.
         */
        fun wrap(connection: SQLiteConnection): AsyncQLiteConnection {
            //Prevent multiple Consumer Threads running on the same SQLiteConnection
            if(listOfWrappedConnections.contains(connection))
                throw IllegalStateException("SQLiteConnection has already been wrapped before!")

            listOfWrappedConnections.add(connection)

            return AsyncQLiteConnection(connection)
        }
    }
}