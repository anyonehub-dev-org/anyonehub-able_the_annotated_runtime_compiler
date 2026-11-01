package com.anyonehub.abl.resources

import com.anyonehub.abl.di.RuntimeContainer
import com.anyonehub.abl.exceptions.AblCompilationException
import com.anyonehub.abl.utils.EngineLogger
import java.io.File
import java.io.InputStreamReader

class NativeAapt2Wrapper(private val explicitBinaryPath: String? = null) {

    val aapt2BinaryPath: String
        get() {
            if (!explicitBinaryPath.isNullOrEmpty() && File(explicitBinaryPath).exists()) {
                return explicitBinaryPath
            }
            // Check RuntimeContainer for provisioned paths
            val directMounted = RuntimeContainer.getMountedToolPath("aapt2")
            if (directMounted != null && directMounted.exists()) {
                return directMounted.absolutePath
            }
            val searchPaths = listOf(
                RuntimeContainer.getMountedToolPath("build-tools-34.0.4-aarch64"),
                RuntimeContainer.getMountedToolPath("android-sdk-tools-static-aarch64"),
                RuntimeContainer.getMountedToolPath("build-tools")
            ).filterNotNull()

            for (dir in searchPaths) {
                val candidates = listOf(
                    File(dir, "34.0.4/aapt2"),
                    File(dir, "build-tools/aapt2"),
                    File(dir, "aapt2")
                )
                for (cand in candidates) {
                    if (cand.exists()) return cand.absolutePath
                }
            }
            return explicitBinaryPath ?: "aapt2"
        }

    fun compile(resDir: File, outputDir: File) {
        val command = listOf(
            aapt2BinaryPath,
            "compile",
            "--dir", resDir.absolutePath,
            "-o", outputDir.absolutePath
        )
        executeCommand(command, "AAPT2 Compile")
    }

    fun link(manifestFile: File, flatFilesDir: File, androidJar: File, outputApk: File, rJavaOutputDir: File) {
        val command = listOf(
            aapt2BinaryPath,
            "link",
            "-I", androidJar.absolutePath,
            "--manifest", manifestFile.absolutePath,
            "-R", flatFilesDir.absolutePath,
            "--java", rJavaOutputDir.absolutePath,
            "-o", outputApk.absolutePath
        )
        executeCommand(command, "AAPT2 Link")
    }

    private fun executeCommand(command: List<String>, phase: String) {
        try {
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
                
            val output = StringBuilder()
            InputStreamReader(process.inputStream).use { reader ->
                reader.forEachLine { line ->
                    output.appendLine(line)
                    EngineLogger.logInfo("[$phase] $line")
                }
            }
            
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                EngineLogger.logError("[$phase] Failed with exit code $exitCode")
                throw AblCompilationException("Native AAPT2 failed during $phase with exit code $exitCode.\nOutput:\n$output")
            }
        } catch (e: Exception) {
            if (e is AblCompilationException) throw e
            throw AblCompilationException("Failed to execute native AAPT2 during $phase", e)
        }
    }
}
