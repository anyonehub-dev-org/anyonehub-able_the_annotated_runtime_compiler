package com.anyonehub.abl.utils

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Structured log levels for the ABL compiler engine.
 */
enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR;

    val ansiColorPrefix: String
        get() = when (this) {
            DEBUG -> "\u001B[36m[ABL DEBUG]\u001B[0m"
            INFO -> "\u001B[32m[ABL INFO]\u001B[0m"
            WARN -> "\u001B[33m[ABL WARN]\u001B[0m"
            ERROR -> "\u001B[31m[ABL ERROR]\u001B[0m"
        }
}

/**
 * Pluggable log listener interface allowing terminal multiplexers and IDE telemetry
 * sinks to receive real-time compilation, D8, AAPT2, and signer events.
 */
fun interface LogListener {
    fun onLog(level: LogLevel, message: String, throwable: Throwable?)
}

/**
 * Pluggable, thread-safe logger dispatcher for ABL compiler toolchain.
 */
object EngineLogger {
    private val listeners = CopyOnWriteArrayList<LogListener>()

    @Volatile
    var isEchoToStdoutEnabled: Boolean = true

    fun setLogListener(listener: LogListener?) {
        listeners.clear()
        if (listener != null) {
            listeners.add(listener)
        }
    }

    fun addLogListener(listener: LogListener) {
        listeners.add(listener)
    }

    fun removeLogListener(listener: LogListener) {
        listeners.remove(listener)
    }

    fun clearListeners() {
        listeners.clear()
    }

    fun logDebug(message: String) {
        dispatch(LogLevel.DEBUG, message, null)
    }

    fun logInfo(message: String) {
        dispatch(LogLevel.INFO, message, null)
    }

    fun logWarn(message: String, throwable: Throwable? = null) {
        dispatch(LogLevel.WARN, message, throwable)
    }

    fun logError(message: String, throwable: Throwable? = null) {
        dispatch(LogLevel.ERROR, message, throwable)
    }

    private fun dispatch(level: LogLevel, message: String, throwable: Throwable?) {
        if (listeners.isNotEmpty()) {
            for (listener in listeners) {
                try {
                    listener.onLog(level, message, throwable)
                } catch (e: Exception) {
                    System.err.println("EngineLogger listener threw: ${e.message}")
                }
            }
        }

        if (isEchoToStdoutEnabled || listeners.isEmpty()) {
            val prefix = level.ansiColorPrefix
            if (level == LogLevel.ERROR) {
                System.err.println("$prefix $message")
                throwable?.printStackTrace(System.err)
            } else {
                println("$prefix $message")
                throwable?.printStackTrace(System.out)
            }
        }
    }
}
