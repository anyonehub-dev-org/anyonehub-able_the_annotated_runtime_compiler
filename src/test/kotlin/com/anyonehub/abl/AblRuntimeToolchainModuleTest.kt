package com.anyonehub.abl

import com.anyonehub.abl.annotations.AblTool
import com.anyonehub.abl.annotations.AblRuntimeAsset
import com.anyonehub.abl.di.AblRuntimeToolchainModule
import com.anyonehub.abl.di.RuntimeContainer
import com.anyonehub.abl.scanner.AblRuntimeScanner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class AblRuntimeToolchainModuleTest {

    @BeforeEach
    fun setUp() {
        RuntimeContainer.clear()
    }

    @Test
    fun testToolchainModuleRegistrationAndDescriptors() {
        val assets = RuntimeContainer.getAllToolchainAssets()
        assertEquals(11, assets.size, "Must register exactly 11 runtime toolchain descriptors")

        val ideAssets = assets.filter { it.bucket == "ide-assets" }
        assertEquals(5, ideAssets.size, "Bucket ide-assets must contain 5 assets")

        val jarSiloAssets = assets.filter { it.bucket == "jar-silo" }
        assertEquals(5, jarSiloAssets.size, "Bucket jar-silo must contain 5 assets")

        val androidSdkAssets = assets.filter { it.bucket == "android-sdk" }
        assertEquals(1, androidSdkAssets.size, "Bucket android-sdk must contain 1 asset")

        // Verify specific known digests
        val kotlinc = RuntimeContainer.getToolchainAsset("kotlinc-dexed")
        assertNotNull(kotlinc)
        assertEquals("kotlinc-dexed.jar", kotlinc!!.fileName)
        assertEquals("6031a6d4f650e62265706edb5396bf4ff8ee0b0371ca02006a1513f132cb9737", kotlinc.sha256)
        assertTrue(kotlinc.isDexedJar)

        val r8 = RuntimeContainer.getToolchainAsset("r8-dexed")
        assertNotNull(r8)
        assertEquals("r8-dexed.jar", r8!!.fileName)
        assertEquals("afa3b255fe998876c626a2c4f3d4750c95bf93cfb1c717b9639552045e3c5b30", r8.sha256)
        assertTrue(r8.isDexedJar)

        val sdk = RuntimeContainer.getToolchainAsset("android-sdk")
        assertNotNull(sdk)
        assertEquals("android-sdk.zip", sdk!!.fileName)
        assertEquals("8a97fb5a190a7ab431658896b3d0a3f1d53ecb1597130c0adb6b79fcfbdec8b1", sdk.sha256)
    }

    @Test
    fun testRuntimeScannerDetectsAblToolAndRuntimeAssets() {
        val scanner = AblRuntimeScanner()
        val metadata = scanner.scan(AblRuntimeToolchainModule::class)

        assertTrue(metadata.isModule, "Must be detected as @AblModule")
        assertTrue(metadata.isTool, "Must be detected as @AblTool")
        assertEquals(11, metadata.runtimeAssets.size, "Must extract all 11 @AblRuntimeAsset annotations")
    }

    @Test
    fun testClassLoaderAndToolPathMounting() {
        val dummyLoader = java.net.URLClassLoader(emptyArray())
        RuntimeContainer.mountClassLoader("kotlinc-dexed.jar", dummyLoader)
        assertEquals(dummyLoader, RuntimeContainer.getMountedClassLoader("kotlinc-dexed"))
        assertEquals(dummyLoader, RuntimeContainer.getMountedClassLoader("kotlinc-dexed.jar"))

        val dummyFile = File("usr/bin/aapt2")
        RuntimeContainer.mountToolPath("aapt2", dummyFile)
        assertEquals(dummyFile, RuntimeContainer.getMountedToolPath("aapt2"))
    }
}

