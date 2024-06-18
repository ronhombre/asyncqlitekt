package asia.hombre.asyncqlitekt

import androidx.sqlite.SQLiteStatement
import asia.hombre.asyncqlitekt.exceptions.IncorrectColumnTypeException

/**
 * Contains the values resulting from calling [AsyncQLiteConnection.prepare].
 *
 * @author Ron Lauren Hombre
 * @since 0.0.1
 */
class AsyncQLiteResult
internal constructor(private val gottenMap: Map<Int, Any?>, private val columnCount: Int, private val columnNames: List<String>): SQLiteStatement {

    /**
     * Does nothing.
     */
    @Deprecated("This method is not used here.")
    override fun bindBlob(index: Int, value: ByteArray) {
        //DO NOTHING
    }

    /**
     * Does nothing.
     */
    @Deprecated("This method is not used here.")
    override fun bindBoolean(index: Int, value: Boolean) {
        //DO NOTHING
    }

    /**
     * Does nothing.
     */
    @Deprecated("This method is not used here.")
    override fun bindDouble(index: Int, value: Double) {
        //DO NOTHING
    }

    /**
     * Does nothing.
     */
    @Deprecated("This method is not used here.")
    override fun bindFloat(index: Int, value: Float) {
        //DO NOTHING
    }

    /**
     * Does nothing.
     */
    @Deprecated("This method is not used here.")
    override fun bindInt(index: Int, value: Int) {
        //DO NOTHING
    }

    /**
     * Does nothing.
     */
    @Deprecated("This method is not used here.")
    override fun bindLong(index: Int, value: Long) {
        //DO NOTHING
    }

    /**
     * Does nothing.
     */
    @Deprecated("This method is not used here.")
    override fun bindNull(index: Int) {
        //DO NOTHING
    }

    /**
     * Does nothing.
     */
    @Deprecated("This method is not used here.")
    override fun bindText(index: Int, value: String) {
        //DO NOTHING
    }

    /**
     * Does nothing.
     */
    @Deprecated("This method is not used here.")
    override fun clearBindings() {
        //DO NOTHING
    }

    /**
     * Does nothing.
     */
    @Deprecated("This method is not used here.")
    override fun close() {
        //DO NOTHING
    }

    /**
     * Checks if the [index] exists and throws an [IndexOutOfBoundsException] if it does not.
     *
     * @throws IndexOutOfBoundsException when the [index] does not exist or has not been declared before calling.
     * [AsyncQLiteStatement.step].
     */
    private fun getValue(index: Int): Any? {
        if(!gottenMap.containsKey(index))
            throw IndexOutOfBoundsException("This index $index does not exist. Make sure it was declared or exists in this row.")

        return gottenMap[index]
    }

    override fun getColumnCount(): Int {
        return columnCount
    }

    override fun getColumnName(index: Int): String {
        return columnNames[index]
    }

    override fun getBlob(index: Int): ByteArray {
        val value = getValue(index)

        if(value !is ByteArray)
            throw IncorrectColumnTypeException(index, ByteArray::javaClass.name)

        return value
    }

    override fun getBoolean(index: Int): Boolean {
        val value = getValue(index)

        if(value !is Boolean)
            throw IncorrectColumnTypeException(index, Boolean::javaClass.name)

        return value
    }

    override fun getDouble(index: Int): Double {
        val value = getValue(index)

        if(value !is Double)
            throw IncorrectColumnTypeException(index, Double::javaClass.name)

        return value
    }

    override fun getFloat(index: Int): Float {
        val value = getValue(index)

        if(value !is Float)
            throw IncorrectColumnTypeException(index, Float::javaClass.name)

        return value
    }

    override fun getInt(index: Int): Int {
        val value = getValue(index)

        if(value !is Int)
            throw IncorrectColumnTypeException(index, Int::javaClass.name)

        return value
    }

    override fun getLong(index: Int): Long {
        val value = getValue(index)

        if(value !is Long)
            throw IncorrectColumnTypeException(index, Long::javaClass.name)

        return value
    }

    override fun getText(index: Int): String {
        val value = getValue(index)

        if(value !is String)
            throw IncorrectColumnTypeException(index, String::javaClass.name)

        return value
    }

    override fun isNull(index: Int): Boolean {
        return getValue(index)?.let { false }?: true
    }

    /**
     * Does nothing.
     */
    @Deprecated("This method is not used here.")
    override fun reset() {
        //DO NOTHING
    }

    /**
     * Does nothing.
     */
    @Deprecated("This method is not used here.", ReplaceWith("false"))
    override fun step(): Boolean {
        //DO NOTHING

        return false
    }
}