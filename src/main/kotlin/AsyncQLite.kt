package asia.hombre.asyncqlitekt

import androidx.sqlite.SQLiteConnection
import asia.hombre.asyncqlitekt.AsyncQLiteConnection.Job

/**
 * Directly wraps an existing [SQLiteConnection] to provide async capability.
 *
 * Equivalent to [AsyncQLiteConnection.wrap].
 */
fun SQLiteConnection.async(): AsyncQLiteConnection {
    return AsyncQLiteConnection.wrap(this)
}

/**
 * Adds the SQL to the Job Queue to be executed.
 *
 * This returns nothing.
 *
 * @param sql The SQL Statement.
 * @param onSuccess Called after the SQL has been executed in the Job Queue.
 *
 * @throws Exception Through the onSuccess
 */
fun AsyncQLiteConnection.execSQL(sql: String, onSuccess: ((error: Exception?) -> Unit)? = null) {
    if(closing.get())
        throw IllegalStateException("Already closed.")

    addJob(Job(sql, null, null, null, onSuccess))
}

/**
 * Prepares a virtual SQL Statement and adds it to the Job Queue with [AsyncQLiteStatement.step].
 *
 * Values to get must be declared before the [AsyncQLiteStatement.step] method is called by calling e.g. getInt(0)
 * and ignoring the returned value.
 *
 * @param sql The SQL Statement.
 * @param onSuccess Called after the SQL has been executed in the Job Queue.
 *
 * @throws Exception Through the onSuccess
 */
fun AsyncQLiteConnection.prepare(sql: String, onSuccess: ((error: Exception?) -> Unit)? = null): AsyncQLiteStatement {
    if(closing.get())
        throw IllegalStateException("Already closed.")

    return AsyncQLiteStatement(sql, { sqlString, bindings, gettings, asyncStatement ->
        addJob(Job(sqlString, bindings, gettings, asyncStatement, onSuccess))
    })
}

/**
 * Opens a function with this object as the 'this' value.
 *
 * Useful for binding, declaring, and reading the results.
 */
inline fun AsyncQLiteStatement.useAsync(block: (AsyncQLiteStatement) -> Unit): AsyncQLiteStatement {
    block.invoke(this)

    return this
}

/**
 * Process a row returned by the SQL statement.
 */
fun AsyncQLiteStatement.process(block: ((AsyncQLiteResult) -> Boolean)) {
    this.onResult = block
}