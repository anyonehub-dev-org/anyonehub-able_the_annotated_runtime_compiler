package com.anyonehub.abl.di

import com.anyonehub.abl.annotations.AblModule
import com.anyonehub.abl.annotations.AblTool
import com.anyonehub.abl.annotations.AblRuntimeAsset
import com.anyonehub.abl.annotations.AblToolchainAssetDescriptor

/**
 * A.B.L. Runtime Toolchain Module declaring the 11 cross-compiled SDK and compiler artifacts.
 */
@AblModule
@AblTool(name = "AblRuntimeToolchainModule", category = "toolchain")
@AblRuntimeAsset(
    id = "android-sdk",
    bucket = "ide-assets",
    fileName = "android-sdk.zip",
    sha256 = "8a97fb5a190a7ab431658896b3d0a3f1d53ecb1597130c0adb6b79fcfbdec8b1",
    arch = "all",
    isDexedJar = false,
    byteSize = 49745273L
)
@AblRuntimeAsset(
    id = "build-tools-34.0.4-aarch64",
    bucket = "ide-assets",
    fileName = "build-tools-34.0.4-aarch64.tar.xz",
    sha256 = "4bbbdeee608ff8d3a2c1534d0a5270305a02b1f200403a536c75ac84c41e52fa",
    arch = "aarch64",
    isDexedJar = false,
    byteSize = 41666832L
)
@AblRuntimeAsset(
    id = "build-tools-34.0.4-arm",
    bucket = "ide-assets",
    fileName = "build-tools-34.0.4-arm.tar.xz",
    sha256 = "a58bf993d107db54a07b14dfd2962837fdefb7d31285e65921f98c303406d826",
    arch = "arm",
    isDexedJar = false,
    byteSize = 41457844L
)
@AblRuntimeAsset(
    id = "platform-tools-34.0.4-aarch64",
    bucket = "ide-assets",
    fileName = "platform-tools-34.0.4-aarch64.tar.xz",
    sha256 = "08b5b5c9899201081586f8f8682882784458ff67e6421cef65b1d6a0da877236",
    arch = "aarch64",
    isDexedJar = false,
    byteSize = 4354052L
)
@AblRuntimeAsset(
    id = "platform-tools-34.0.4-arm",
    bucket = "ide-assets",
    fileName = "platform-tools-34.0.4-arm.tar.xz",
    sha256 = "d541f076940cbb7b6113632e7a1bd9eb7b1ad35fc76a321533624ffb43770894",
    arch = "arm",
    isDexedJar = false,
    byteSize = 4013492L
)
@AblRuntimeAsset(
    id = "EmmyLua-Dexed",
    bucket = "jar-silo",
    fileName = "EmmyLua-Dexed.jar",
    sha256 = "b2b0754582a0d445e51e2c0c44999c6888c0bcd00b65abc117e6a26067766bef",
    arch = "all",
    isDexedJar = true,
    byteSize = 2864192L
)
@AblRuntimeAsset(
    id = "javac-v21-aarch64.dex",
    bucket = "jar-silo",
    fileName = "javac-v21-aarch64.dex.jar",
    sha256 = "4dfcd9242e0aa3aaa4cf2af7a160d761ed0ed274959a40a77befdf126ce32d00",
    arch = "aarch64",
    isDexedJar = true,
    byteSize = 1775562L
)
@AblRuntimeAsset(
    id = "kotlinc-dexed",
    bucket = "jar-silo",
    fileName = "kotlinc-dexed.jar",
    sha256 = "6031a6d4f650e62265706edb5396bf4ff8ee0b0371ca02006a1513f132cb9737",
    arch = "all",
    isDexedJar = true,
    byteSize = 16959316L
)
@AblRuntimeAsset(
    id = "nb-javac-17.0.0.4-android-dexed",
    bucket = "jar-silo",
    fileName = "nb-javac-17.0.0.4-android-dexed.jar",
    sha256 = "659368f71d271a779315f78861fc86b4ac2c48763e72a24e56ad4838f5c26a63",
    arch = "all",
    isDexedJar = true,
    byteSize = 1555912L
)
@AblRuntimeAsset(
    id = "r8-dexed",
    bucket = "jar-silo",
    fileName = "r8-dexed.jar",
    sha256 = "afa3b255fe998876c626a2c4f3d4750c95bf93cfb1c717b9639552045e3c5b30",
    arch = "all",
    isDexedJar = true,
    byteSize = 13582219L
)
@AblRuntimeAsset(
    id = "android-sdk-tools-static-aarch64",
    bucket = "android-sdk",
    fileName = "android-sdk-tools-static-aarch64.zip",
    sha256 = "db1cea2c4454d5f9c5a802646b2d1cf560b4ee7badbe23e51ab8e1881bb50fc2",
    arch = "aarch64",
    isDexedJar = false,
    byteSize = 14401170L
)
class AblRuntimeToolchainModule {

    init {
        registerDefaults(RuntimeContainer)
    }

    companion object {
        val ALL_ASSETS: List<AblToolchainAssetDescriptor> = listOf(
            AblToolchainAssetDescriptor(
                id = "android-sdk",
                bucket = "ide-assets",
                fileName = "android-sdk.zip",
                sha256 = "8a97fb5a190a7ab431658896b3d0a3f1d53ecb1597130c0adb6b79fcfbdec8b1",
                arch = "all",
                isDexedJar = false,
                byteSize = 49745273L
            ),
            AblToolchainAssetDescriptor(
                id = "build-tools-34.0.4-aarch64",
                bucket = "ide-assets",
                fileName = "build-tools-34.0.4-aarch64.tar.xz",
                sha256 = "4bbbdeee608ff8d3a2c1534d0a5270305a02b1f200403a536c75ac84c41e52fa",
                arch = "aarch64",
                isDexedJar = false,
                byteSize = 41666832L,
                mirrorBucket = "build-tools"
            ),
            AblToolchainAssetDescriptor(
                id = "build-tools-34.0.4-arm",
                bucket = "ide-assets",
                fileName = "build-tools-34.0.4-arm.tar.xz",
                sha256 = "a58bf993d107db54a07b14dfd2962837fdefb7d31285e65921f98c303406d826",
                arch = "arm",
                isDexedJar = false,
                byteSize = 41457844L
            ),
            AblToolchainAssetDescriptor(
                id = "platform-tools-34.0.4-aarch64",
                bucket = "ide-assets",
                fileName = "platform-tools-34.0.4-aarch64.tar.xz",
                sha256 = "08b5b5c9899201081586f8f8682882784458ff67e6421cef65b1d6a0da877236",
                arch = "aarch64",
                isDexedJar = false,
                byteSize = 4354052L,
                mirrorBucket = "platform-tools"
            ),
            AblToolchainAssetDescriptor(
                id = "platform-tools-34.0.4-arm",
                bucket = "ide-assets",
                fileName = "platform-tools-34.0.4-arm.tar.xz",
                sha256 = "d541f076940cbb7b6113632e7a1bd9eb7b1ad35fc76a321533624ffb43770894",
                arch = "arm",
                isDexedJar = false,
                byteSize = 4013492L
            ),
            AblToolchainAssetDescriptor(
                id = "EmmyLua-Dexed",
                bucket = "jar-silo",
                fileName = "EmmyLua-Dexed.jar",
                sha256 = "b2b0754582a0d445e51e2c0c44999c6888c0bcd00b65abc117e6a26067766bef",
                arch = "all",
                isDexedJar = true,
                byteSize = 2864192L
            ),
            AblToolchainAssetDescriptor(
                id = "javac-v21-aarch64.dex",
                bucket = "jar-silo",
                fileName = "javac-v21-aarch64.dex.jar",
                sha256 = "4dfcd9242e0aa3aaa4cf2af7a160d761ed0ed274959a40a77befdf126ce32d00",
                arch = "aarch64",
                isDexedJar = true,
                byteSize = 1775562L
            ),
            AblToolchainAssetDescriptor(
                id = "kotlinc-dexed",
                bucket = "jar-silo",
                fileName = "kotlinc-dexed.jar",
                sha256 = "6031a6d4f650e62265706edb5396bf4ff8ee0b0371ca02006a1513f132cb9737",
                arch = "all",
                isDexedJar = true,
                byteSize = 16959316L
            ),
            AblToolchainAssetDescriptor(
                id = "nb-javac-17.0.0.4-android-dexed",
                bucket = "jar-silo",
                fileName = "nb-javac-17.0.0.4-android-dexed.jar",
                sha256 = "659368f71d271a779315f78861fc86b4ac2c48763e72a24e56ad4838f5c26a63",
                arch = "all",
                isDexedJar = true,
                byteSize = 1555912L
            ),
            AblToolchainAssetDescriptor(
                id = "r8-dexed",
                bucket = "jar-silo",
                fileName = "r8-dexed.jar",
                sha256 = "afa3b255fe998876c626a2c4f3d4750c95bf93cfb1c717b9639552045e3c5b30",
                arch = "all",
                isDexedJar = true,
                byteSize = 13582219L
            ),
            AblToolchainAssetDescriptor(
                id = "android-sdk-tools-static-aarch64",
                bucket = "android-sdk",
                fileName = "android-sdk-tools-static-aarch64.zip",
                sha256 = "db1cea2c4454d5f9c5a802646b2d1cf560b4ee7badbe23e51ab8e1881bb50fc2",
                arch = "aarch64",
                isDexedJar = false,
                byteSize = 14401170L
            )
        )

        fun registerDefaults(container: RuntimeContainer = RuntimeContainer) {
            ALL_ASSETS.forEach { container.registerToolchainAsset(it) }
        }
    }
}
