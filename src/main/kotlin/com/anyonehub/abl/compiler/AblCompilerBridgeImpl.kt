package com.anyonehub.abl.compiler

import com.anyonehub.abl.di.RuntimeContainer
import com.anyonehub.abl.exceptions.AblCompilationException
import com.anyonehub.abl.utils.EngineLogger
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.File
import java.nio.file.Files

class AblCompilerBridgeImpl : AblCompilerBridge {

    override fun compile(sources: Map<String, String>, classpath: List<File>): Map<String, ByteArray> {
        val tempDir = Files.createTempDirectory("abl_compiler").toFile()
        val srcDir = File(tempDir, "src").apply { mkdirs() }
        val outDir = File(tempDir, "out").apply { mkdirs() }
        
        val errors = mutableListOf<String>()

        try {
            val javaFiles = mutableListOf<File>()
            val kotlinFiles = mutableListOf<File>()

            // Resolve mounted android.jar from RuntimeContainer if available
            val effectiveClasspath = classpath.toMutableList()
            val mountedAndroidJar = RuntimeContainer.getMountedToolPath("android.jar")
                ?: RuntimeContainer.getMountedToolPath("android-sdk")?.let { dir ->
                    if (dir.isFile && dir.name.endsWith(".jar")) dir
                    else File(dir, "platforms/android-33/android.jar").takeIf { it.exists() }
                        ?: File(dir, "platforms/android-34/android.jar").takeIf { it.exists() }
                        ?: File(dir, "platforms/android-37/android.jar").takeIf { it.exists() }
                        ?: File(dir, "android.jar").takeIf { it.exists() }
                }

            if (mountedAndroidJar != null && mountedAndroidJar.exists() && !effectiveClasspath.contains(mountedAndroidJar)) {
                effectiveClasspath.add(0, mountedAndroidJar)
            }

            // Write sources to the temporary workspace
            sources.forEach { (className, code) ->
                val isJava = className.endsWith(".java")
                val fqName = if (isJava) className.removeSuffix(".java") else className
                val relativePath = fqName.replace('.', '/') + if (isJava) ".java" else ".kt"
                val file = File(srcDir, relativePath)
                file.parentFile?.mkdirs()
                file.writeText(code)
                if (isJava) {
                    javaFiles.add(file)
                } else {
                    kotlinFiles.add(file)
                }
            }

            if (kotlinFiles.isNotEmpty() || javaFiles.isNotEmpty()) {
                val args = K2JVMCompilerArguments().apply {
                    freeArgs = (kotlinFiles + javaFiles).map { it.absolutePath }
                    destination = outDir.absolutePath
                    if (effectiveClasspath.isNotEmpty()) {
                        this.classpath = effectiveClasspath.joinToString(File.pathSeparator) { it.absolutePath }
                    }
                    noStdlib = true
                    noReflect = true
                    jvmTarget = "21"
                }
    
                val collector = object : MessageCollector {
                    override fun clear() { errors.clear() }
                    override fun hasErrors(): Boolean = errors.isNotEmpty()
                    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageSourceLocation?) {
                        val formatted = "[$severity] $message at $location"
                        if (severity.isError) {
                            errors.add(formatted)
                            EngineLogger.logError("Compiler Error: $formatted")
                        } else if (severity.isWarning) {
                            EngineLogger.logWarn("Compiler Warning: $formatted")
                        }
                    }
                }
    
                // Check if kotlinc-dexed is mounted in RuntimeContainer via DexClassLoader
                var delegatedKotlin = false
                val kotlincLoader = RuntimeContainer.getMountedClassLoader("kotlinc-dexed")
                    ?: RuntimeContainer.getMountedClassLoader("kotlinc-dexed.jar")

                if (kotlincLoader != null) {
                    try {
                        EngineLogger.logInfo("Delegating Kotlin compilation to mounted DexClassLoader for kotlinc-dexed...")
                        val compilerClass = kotlincLoader.loadClass("org.jetbrains.kotlin.cli.jvm.K2JVMCompiler")
                        val compilerInstance = compilerClass.getDeclaredConstructor().newInstance()
                        val execMethod = compilerClass.getMethod(
                            "exec",
                            MessageCollector::class.java,
                            Services::class.java,
                            K2JVMCompilerArguments::class.java
                        )
                        val exitCode = execMethod.invoke(compilerInstance, collector, Services.EMPTY, args)
                        if (exitCode.toString() != "OK" || errors.isNotEmpty()) {
                            throw AblCompilationException("Delegated Kotlin Compilation failed with exit code $exitCode.\nErrors:\n${errors.joinToString("\n")}")
                        }
                        delegatedKotlin = true
                    } catch (e: Exception) {
                        if (e is AblCompilationException) throw e
                        EngineLogger.logWarn("Mounted kotlinc-dexed delegation failed (${e.message}), falling back to embeddable compiler.")
                    }
                }

                if (!delegatedKotlin) {
                    val compiler = K2JVMCompiler()
                    val exitCode = compiler.exec(collector, Services.EMPTY, args)
                    if (exitCode != org.jetbrains.kotlin.cli.common.ExitCode.OK || errors.isNotEmpty()) {
                        throw AblCompilationException("Kotlin Compilation failed with exit code $exitCode.\nErrors:\n${errors.joinToString("\n")}")
                    }
                }
            }

            if (javaFiles.isNotEmpty()) {
                val fullClasspath = effectiveClasspath.map { it.absolutePath }.toMutableList()
                fullClasspath.add(outDir.absolutePath) // To resolve Kotlin classes

                var delegatedJava = false
                val javacLoader = RuntimeContainer.getMountedClassLoader("javac-v21-aarch64.dex")
                    ?: RuntimeContainer.getMountedClassLoader("javac-v21-aarch64.dex.jar")
                    ?: RuntimeContainer.getMountedClassLoader("nb-javac-17.0.0.4-android-dexed")
                    ?: RuntimeContainer.getMountedClassLoader("nb-javac-17.0.0.4-android-dexed.jar")

                if (javacLoader != null) {
                    try {
                        EngineLogger.logInfo("Delegating Java compilation to mounted DexClassLoader for javac-v21 / nb-javac...")
                        val mainClass = try {
                            javacLoader.loadClass("com.sun.tools.javac.Main")
                        } catch (_: Exception) {
                            javacLoader.loadClass("org.netbeans.lib.nbjavac.services.NBJavacMain")
                        }
                        val compileMethod = mainClass.getMethod("compile", Array<String>::class.java)
                        val javacArgs = mutableListOf<String>()
                        if (fullClasspath.isNotEmpty()) {
                            javacArgs.add("-cp")
                            javacArgs.add(fullClasspath.joinToString(File.pathSeparator))
                        }
                        javacArgs.add("-d")
                        javacArgs.add(outDir.absolutePath)
                        javacArgs.addAll(javaFiles.map { it.absolutePath })

                        val result = compileMethod.invoke(null, javacArgs.toTypedArray())
                        val exitCode = (result as? Number)?.toInt() ?: 0
                        if (exitCode != 0) {
                            throw AblCompilationException("Delegated Javac compilation failed with exit code $exitCode.")
                        }
                        delegatedJava = true
                    } catch (e: Exception) {
                        if (e is AblCompilationException) throw e
                        EngineLogger.logWarn("Mounted javac delegation failed (${e.message}), falling back to ECJ.")
                    }
                }

                if (!delegatedJava) {
                    val ecjArgs = mutableListOf<String>()
                    if (fullClasspath.isNotEmpty()) {
                        ecjArgs.add("-cp")
                        ecjArgs.add(fullClasspath.joinToString(File.pathSeparator))
                    }
                    ecjArgs.add("-d")
                    ecjArgs.add(outDir.absolutePath)
                    ecjArgs.add("-21") // JVM Target 21
                    ecjArgs.addAll(javaFiles.map { it.absolutePath })
                    
                    val outWriter = java.io.PrintWriter(System.out)
                    val errWriter = java.io.PrintWriter(System.err)
                    val ecjMain = org.eclipse.jdt.internal.compiler.batch.Main(outWriter, errWriter, false, null, null)
                    val success = ecjMain.compile(ecjArgs.toTypedArray())
                    if (!success) {
                        throw AblCompilationException("ECJ Compilation failed for Java sources.")
                    }
                }
            }

            // Harvest the compiled bytecode directly into memory
            val resultMap = mutableMapOf<String, ByteArray>()
            outDir.walkTopDown().filter { it.isFile && it.extension == "class" }.forEach { classFile ->
                val relativePath = classFile.relativeTo(outDir).path
                val fqName = relativePath.removeSuffix(".class").replace(File.separatorChar, '.')
                resultMap[fqName] = classFile.readBytes()
            }
            return resultMap
        } finally {
            // ZERO FOOTPRINT POLICY: guarantee cleanup to prevent disk bloat
            tempDir.deleteRecursively()
        }
    }
}
