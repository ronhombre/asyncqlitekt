package asia.hombre.asyncqlitekt

import androidx.sqlite.SQLiteStatement

/**
 * An extension of the [SQLiteStatement] class that binds index values and declares indexes to get from the result.
 *
 * @author Ron Lauren Hombre
 * @since 0.0.1
 */
class AsyncQLiteStatement
internal constructor(
    private val sql: String,
    private val onStep: ((String, Map<Int, Any?>, Map<Int, Types>, AsyncQLiteStatement) -> Unit),
    internal var onResult: ((AsyncQLiteResult) -> Boolean)? = null
): SQLiteStatement {
    private val bindingsMap = mutableMapOf<Int, Any?>()
    private val gettingsMap = mutableMapOf<Int, Types>()

    override fun bindBlob(index: Int, value: ByteArray) {
        bindingsMap[index] = value
    }

    override fun bindBoolean(index: Int, value: Boolean) {
        bindingsMap[index] = value
    }

    override fun bindDouble(index: Int, value: Double) {
        bindingsMap[index] = value
    }

    override fun bindFloat(index: Int, value: Float) {
        bindingsMap[index] = value
    }

    override fun bindInt(index: Int, value: Int) {
        bindingsMap[index] = value
    }

    override fun bindLong(index: Int, value: Long) {
        bindingsMap[index] = value
    }

    override fun bindNull(index: Int) {
        bindingsMap[index] = null
    }

    override fun bindText(index: Int, value: String) {
        bindingsMap[index] = value
    }

    override fun clearBindings() {
        bindingsMap.clear()
    }

    /**
     * Does nothing.
     */
    @Deprecated("This method is not used here.")
    override fun close() {
        //DO NOTHING
    }

    /**
     * Does nothing.
     */
    @Deprecated("This method is not used here.", ReplaceWith("0"))
    override fun getColumnCount(): Int {
        //DO NOTHING

        return 0
    }

    /**
     * Does nothing.
     */
    @Deprecated("This method is not used here.", ReplaceWith("\"\""))
    override fun getColumnName(index: Int): String {
        //DO NOTHING

        return ""
    }

    /**
     * Declares that at [index] a [ByteArray] will be 'get' later.
     *
     * @param index 0-based index of the column.
     * @return An empty [ByteArray].
     */
    override fun getBlob(index: Int): ByteArray {
        gettingsMap[index] = Types.Blob

        return byteArrayOf()
    }

    /**
     * Declares that at [index] a [Boolean] will be 'get' later.
     *
     * @param index 0-based index of the column.
     * @return false
     */
    override fun getBoolean(index: Int): Boolean {
        gettingsMap[index] = Types.Boolean

        return false
    }

    /**
     * Declares that at [index] a [Double] will be 'get' later.
     *
     * @param index 0-based index of the column.
     * @return 0.0
     */
    override fun getDouble(index: Int): Double {
        gettingsMap[index] = Types.Double

        return 0.0
    }

    /**
     * Declares that at [index] a [Float] will be 'get' later.
     *
     * @param index 0-based index of the column.
     * @return 0F
     */
    override fun getFloat(index: Int): Float {
        gettingsMap[index] = Types.Float

        return 0F
    }

    /**
     * Declares that at [index] a [Int] will be 'get' later.
     *
     * @param index 0-based index of the column.
     * @return 0
     */
    override fun getInt(index: Int): Int {
        gettingsMap[index] = Types.Int

        return 0
    }

    /**
     * Declares that at [index] a [Long] will be 'get' later.
     *
     * @param index 0-based index of the column.
     * @return 0L
     */
    override fun getLong(index: Int): Long {
        gettingsMap[index] = Types.Long

        return 0L
    }

    /**
     * Declares that at [index] a [String] will be 'get' later.
     *
     * @param index 0-based index of the column.
     * @return An empty [String].
     */
    override fun getText(index: Int): String {
        gettingsMap[index] = Types.Text

        return ""
    }

    /**
     * Declares that at [index] a null value will be 'get' later.
     *
     * @param index 0-based index of the column.
     * @return false
     */
    override fun isNull(index: Int): Boolean {
        gettingsMap[index] = Types.Null

        return false
    }

    /**
     * Resets all declared indexes.
     */
    override fun reset() {
        gettingsMap.clear()
    }

    /**
     * Adds this [AsyncQLiteStatement] to the Job Queue to be consumed by the Consumer Thread.
     *
     * @return false
     */
    override fun step(): Boolean {
        onStep.invoke(sql, bindingsMap, gettingsMap, this)

        return false
    }

    /**
     * Internal enum class for the declared indexes to get.
     */
    internal enum class Types {
        Blob,
        Boolean,
        Double,
        Float,
        Int,
        Long,
        Null,
        Text
    }
}