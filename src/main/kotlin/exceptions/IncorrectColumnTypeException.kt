package asia.hombre.asyncqlitekt.exceptions

/**
 * Thrown when called to get a value with a different type than expected.
 *
 * If AsyncQLiteResult.getInt(0) actually has a value of "0" with type of String then this exception if thrown.
 *
 *
 */
class IncorrectColumnTypeException(
    /**
     * The index of the column.
     */
    index: Int,
    /**
     * The incorrect type of the column.
     */
    type: String
): RuntimeException("This column index: $index does not have type of $type!")