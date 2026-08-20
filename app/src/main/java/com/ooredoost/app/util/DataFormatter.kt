package com.ooredoost.app.util

import java.text.DecimalFormat

/**
 * Utility for formatting data sizes (bytes) into human-readable strings.
 */
object DataFormatter {

    private val decimalFormat = DecimalFormat("#.##")
    private val preciseFormat = DecimalFormat("#.###")

    private const val KB = 1024L
    private const val MB = 1024L * 1024L
    private const val GB = 1024L * 1024L * 1024L

    /**
     * Format bytes into a human-readable string (e.g., "1.5 MB").
     */
    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 0 -> "0 B"
            bytes < KB -> "$bytes B"
            bytes < MB -> "${decimalFormat.format(bytes.toDouble() / KB)} KB"
            bytes < GB -> "${decimalFormat.format(bytes.toDouble() / MB)} MB"
            else -> "${preciseFormat.format(bytes.toDouble() / GB)} GB"
        }
    }

    /**
     * Format bytes with full precision for detailed view.
     */
    fun formatBytesPrecise(bytes: Long): String {
        return when {
            bytes < 0 -> "0 B"
            bytes < KB -> "$bytes B"
            bytes < MB -> "${preciseFormat.format(bytes.toDouble() / KB)} KB"
            bytes < GB -> "${preciseFormat.format(bytes.toDouble() / MB)} MB"
            else -> "${preciseFormat.format(bytes.toDouble() / GB)} GB"
        }
    }

    /**
     * Format bytes into separate value and unit for UI display.
     */
    fun formatBytesComponents(bytes: Long): Pair<String, String> {
        return when {
            bytes < 0 -> Pair("0", "B")
            bytes < KB -> Pair("$bytes", "B")
            bytes < MB -> Pair(decimalFormat.format(bytes.toDouble() / KB), "KB")
            bytes < GB -> Pair(decimalFormat.format(bytes.toDouble() / MB), "MB")
            else -> Pair(preciseFormat.format(bytes.toDouble() / GB), "GB")
        }
    }

    /**
     * Format duration in milliseconds to human-readable string.
     */
    fun formatDuration(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return when {
            hours > 0 -> "${hours}h ${minutes % 60}m ${seconds % 60}s"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            seconds > 0 -> "${seconds}s"
            else -> "${millis}ms"
        }
    }

    /**
     * Format a timestamp to a readable date/time string.
     */
    fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    /**
     * Format a timestamp to just time.
     */
    fun formatTime(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    /**
     * Format a timestamp to just date.
     */
    fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    /**
     * Get the speed in KB/s from bytes and duration.
     */
    fun formatSpeed(bytes: Long, durationMs: Long): String {
        if (durationMs <= 0) return "0 KB/s"
        val bytesPerSecond = bytes * 1000.0 / durationMs
        return when {
            bytesPerSecond < KB -> "${decimalFormat.format(bytesPerSecond)} B/s"
            bytesPerSecond < MB -> "${decimalFormat.format(bytesPerSecond / KB)} KB/s"
            else -> "${decimalFormat.format(bytesPerSecond / MB)} MB/s"
        }
    }
}
