package com.anyonehub.abl.annotations

/**
 * Declares a runtime downloadable/mountable asset in the A.B.L. toolchain.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class AblRuntimeAsset(
    val id: String,
    val bucket: String,
    val fileName: String,
    val sha256: String,
    val arch: String = "aarch64",
    val isDexedJar: Boolean = false,
    val byteSize: Long = 0L
)

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class AblRuntimeAssets(
    val value: Array<AblRuntimeAsset>
)

