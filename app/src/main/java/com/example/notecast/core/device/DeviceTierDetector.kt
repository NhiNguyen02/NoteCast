package com.example.notecast.core.device

import android.os.Build
import kotlin.collections.any
import kotlin.text.contains

object DeviceTierDetector {
    /**
     * Very simple heuristic: RAM + ABI.
     * Tweak thresholds for your target cohort.
     */
    fun evaluate(): DeviceTier {
        val ramMB = Runtime.getRuntime().maxMemory() / (1024 * 1024)
        val isArm64 = Build.SUPPORTED_ABIS.any { it.contains("arm64") }

        return when {
            ramMB < 2500 -> DeviceTier.LOW_END
            !isArm64 -> DeviceTier.LOW_END
            else -> DeviceTier.MID_HIGH_END
        }
    }
}