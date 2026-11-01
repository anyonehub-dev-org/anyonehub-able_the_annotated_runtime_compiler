package com.anyonehub.abl.compiler

import com.android.tools.r8.D8
import com.android.tools.r8.D8Command
import com.android.tools.r8.DexIndexedConsumer
import com.android.tools.r8.DiagnosticsHandler
import com.android.tools.r8.origin.Origin
import com.anyonehub.abl.di.RuntimeContainer
import com.anyonehub.abl.utils.EngineLogger
import java.io.ByteArrayOutputStream

class RuntimeDexProcessor {

    /**
     * Transpiles standard JVM bytecode into Dalvik bytecode (.dex) entirely in-memory using D8.
     * 
     * @param classBytes A map of fully qualified class names to their compiled JVM .class byte arrays.
     * @return A ByteArray containing the aggregated classes.dex file payload.
     */
    fun transpileToDex(classBytes: Map<String, ByteArray>): ByteArray {
        val outputStream = ByteArrayOutputStream()
        
        val consumer = object : DexIndexedConsumer {
            override fun accept(
                index: Int,
                data: ByteArray,
                descriptors: Set<String>,
                handler: DiagnosticsHandler?
            ) {
                // Intercept the DEX output and write it strictly to our ByteArrayOutputStream buffer.
                outputStream.writeBytes(data)
            }

            override fun finished(handler: DiagnosticsHandler?) {
                // In-memory DEX output is fully written.
            }
        }

        val builder = D8Command.builder()
            .setProgramConsumer(consumer)
            .setMinApiLevel(26)
            .setDisableDesugaring(true)

        // Inject bytecode directly into D8 memory stream, zero disk footprint.
        classBytes.values.forEach { bytes ->
            builder.addClassProgramData(bytes, Origin.unknown())
        }

        // Check if mounted r8-dexed classloader is present in RuntimeContainer
        val r8Loader = RuntimeContainer.getMountedClassLoader("r8-dexed")
            ?: RuntimeContainer.getMountedClassLoader("r8-dexed.jar")

        if (r8Loader != null) {
            try {
                EngineLogger.logInfo("Binding to mounted self-dexed r8-dexed.jar classloader...")
                val d8Class = r8Loader.loadClass("com.android.tools.r8.D8")
                val runMethod = d8Class.getMethod("run", D8Command::class.java)
                runMethod.invoke(null, builder.build())
                return outputStream.toByteArray()
            } catch (e: Exception) {
                EngineLogger.logWarn("Mounted r8-dexed classloader execution failed (${e.message}), falling back to direct D8.")
            }
        }

        D8.run(builder.build())

        return outputStream.toByteArray()
    }
}
