package com.anyonehub.abl.packaging

import java.io.File
import java.io.FileOutputStream
import java.io.FilterOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Tracks exact byte offsets written to the underlying stream.
 */
class CountingOutputStream(out: OutputStream) : FilterOutputStream(out) {
    var bytesWritten: Long = 0
        private set

    override fun write(b: Int) {
        out.write(b)
        bytesWritten++
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        out.write(b, off, len)
        bytesWritten += len
    }
}

class ZipAligner {

    companion object {
        const val ALIGNMENT_4_BYTES = 4
        const val ALIGNMENT_16_KB = 16384
        const val EXTRA_FIELD_ALIGNMENT_ID: Int = 0xd935

        /**
         * Resolves the required alignment for an APK entry.
         * Native shared libraries (lib/&lt;abi&gt;/.so files) require 16 KB (16,384-byte) alignment for Android 15+
         * 16 KB page-size compliance. All other uncompressed entries require standard 4-byte alignment.
         */
        fun getRequiredAlignment(entryName: String): Int {
            return if (entryName.startsWith("lib/") && entryName.endsWith(".so")) {
                ALIGNMENT_16_KB
            } else {
                ALIGNMENT_4_BYTES
            }
        }
    }

    /**
     * Parses the ZIP entries and injects an "Extra Field" (ID 0xd935) into the Local File Header 
     * of uncompressed (STORED) entries to shift their data payload offsets to exact multiples
     * of 16 KB (for native libraries) or 4 bytes (for resources/assets).
     * 
     * @param inputApk The unaligned APK file.
     * @param outputApk The destination aligned APK file.
     */
    fun align(inputApk: File, outputApk: File) {
        val zipFile = ZipFile(inputApk)
        val countingOut = CountingOutputStream(FileOutputStream(outputApk))
        val zos = ZipOutputStream(countingOut)

        for (entry in zipFile.entries().toList()) {
            val inStream = zipFile.getInputStream(entry)
            
            val outEntry = ZipEntry(entry.name)
            outEntry.method = entry.method
            if (entry.method == ZipEntry.STORED) {
                outEntry.size = entry.size
                outEntry.compressedSize = entry.compressedSize
                outEntry.crc = entry.crc
            }
            outEntry.time = entry.time
            outEntry.comment = entry.comment
            
            val currentExtra = entry.extra ?: ByteArray(0)
            outEntry.extra = currentExtra

            // Only uncompressed (STORED) entries need alignment for mmap
            if (outEntry.method == ZipEntry.STORED) {
                val nameBytes = outEntry.name.toByteArray(Charsets.UTF_8)
                val alignment = getRequiredAlignment(outEntry.name)
                
                // LFH size: 30 bytes (header) + name length + extra field length
                val expectedDataOffset = countingOut.bytesWritten + 30 + nameBytes.size + currentExtra.size
                val paddingNeeded = ((alignment - (expectedDataOffset % alignment)) % alignment).toInt()
                
                if (paddingNeeded > 0) {
                    // Extra field ID 0xd935 requires at least 4 bytes (2 for ID, 2 for Data Size)
                    val padSize = if (paddingNeeded < 4) paddingNeeded + alignment else paddingNeeded
                    
                    val paddingField = ByteArray(padSize)
                    paddingField[0] = 0x35.toByte() // ID: 0xd935 (Little Endian)
                    paddingField[1] = 0xd9.toByte()
                    
                    val dataSize = padSize - 4
                    paddingField[2] = (dataSize and 0xFF).toByte()
                    paddingField[3] = ((dataSize shr 8) and 0xFF).toByte()
                    
                    // Append padding field to existing extra data
                    val newExtra = ByteArray(currentExtra.size + padSize)
                    System.arraycopy(currentExtra, 0, newExtra, 0, currentExtra.size)
                    System.arraycopy(paddingField, 0, newExtra, currentExtra.size, padSize)
                    outEntry.extra = newExtra
                }
            }

            zos.putNextEntry(outEntry)
            inStream.copyTo(zos)
            zos.closeEntry()
            inStream.close()
        }
        
        zos.close()
        zipFile.close()
    }
}
