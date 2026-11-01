package com.anyonehub.abl.annotations

/**
 * In-memory descriptor for an A.B.L. toolchain asset.
 */
data class AblToolchainAssetDescriptor(
    val id: String,
    val bucket: String,
    val fileName: String,
    val sha256: String,
    val arch: String = "aarch64",
    val isDexedJar: Boolean = false,
    val byteSize: Long = 0L,
    val mirrorBucket: String? = null
)

