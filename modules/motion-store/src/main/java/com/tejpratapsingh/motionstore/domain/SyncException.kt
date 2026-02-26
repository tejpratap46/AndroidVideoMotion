package com.tejpratapsingh.motionstore.domain

/**
 * Typed exceptions thrown by the sync framework.
 * Catch these in your ViewModel / use-case layer to show appropriate UI.
 */
sealed class SyncException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    /** A network call to the backend failed (non-2xx or connection error). */
    class NetworkError(
        message: String,
        cause: Throwable? = null,
    ) : SyncException(message, cause)

    /** A row downloaded from the server could not be parsed into a local entity. */
    class ParseError(
        tableName: String,
        row: Map<String, Any?>,
        cause: Throwable? = null,
    ) : SyncException("Failed to parse row from '$tableName': $row", cause)

    /** A local write (insert/update) failed during the sync process. */
    class LocalWriteError(
        message: String,
        cause: Throwable? = null,
    ) : SyncException(message, cause)

    /** The adapter threw an unexpected error. */
    class UnknownError(
        cause: Throwable,
    ) : SyncException(cause.message ?: "Unknown sync error", cause)
}
