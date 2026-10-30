package com.olens.launcher.core

import android.util.Log
import kotlin.math.abs
import kotlin.math.cos

object NativeEngine {
    private const val TAG = "OlensNativeEngine"
    private var isNativeLoaded = false

    init {
        try {
            System.loadLibrary("olens_core")
            isNativeLoaded = true
            Log.i(TAG, "Rust native engine (libolens_core.so) successfully loaded")
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Native library libolens_core.so not found, falling back to Kotlin implementation: ${e.message}")
            isNativeLoaded = false
        }
    }

    fun init(): Boolean {
        return if (isNativeLoaded) {
            nativeInit() == 1
        } else {
            true
        }
    }

    /**
     * Compute ergonomic wave offsets for alphabet scrubber items.
     * @param selectedIndex The continuous floating index of current touch
     * @param totalCount Total alphabet letters
     * @param waveRadius The width of the wave effect in item units
     * @param maxOffsetPx The maximum horizontal protrusion in pixels
     */
    fun calculateWaveOffsets(
        selectedIndex: Float,
        totalCount: Int,
        waveRadius: Float = 3.5f,
        maxOffsetPx: Float = 42f
    ): FloatArray {
        if (isNativeLoaded) {
            try {
                return nativeCalculateWaveOffsets(selectedIndex, totalCount, waveRadius, maxOffsetPx)
            } catch (e: Exception) {
                Log.e(TAG, "nativeCalculateWaveOffsets failed, using fallback", e)
            }
        }
        
        // Pure Kotlin fallback
        val offsets = FloatArray(totalCount)
        val radius = waveRadius.coerceAtLeast(1f)
        for (i in 0 until totalCount) {
            val diff = abs(i.toFloat() - selectedIndex)
            if (diff <= radius) {
                val factor = cos(Math.PI * diff / (radius * 2.0)).toFloat()
                offsets[i] = factor * factor * maxOffsetPx
            } else {
                offsets[i] = 0f
            }
        }
        return offsets
    }

    /**
     * Fuzzy search scoring and ranking using Rust core
     */
    fun fuzzySearch(
        query: String,
        labels: Array<String>,
        packages: Array<String>
    ): IntArray {
        if (query.isBlank()) {
            return IntArray(labels.size) { it }
        }

        if (isNativeLoaded) {
            try {
                return nativeFuzzySearch(query, labels, packages)
            } catch (e: Exception) {
                Log.e(TAG, "nativeFuzzySearch failed, using fallback", e)
            }
        }

        // Pure Kotlin fallback
        val q = query.trim().lowercase()
        return labels.indices
            .map { idx ->
                val label = labels[idx].lowercase()
                val score = when {
                    label == q -> 10000
                    label.startsWith(q) -> 5000 + (100 - label.length)
                    label.contains(q) -> 2000 - label.indexOf(q)
                    else -> -1
                }
                idx to score
            }
            .filter { it.second >= 0 }
            .sortedByDescending { it.second }
            .map { it.first }
            .toIntArray()
    }

    // --- Native JNI declarations ---
    private external fun nativeInit(): Int
    private external fun nativeQuickScore(query: String, target: String): Int
    private external fun nativeCalculateWaveOffsets(
        selectedIndex: Float,
        totalCount: Int,
        waveRadius: Float,
        maxOffset: Float
    ): FloatArray
    private external fun nativeFuzzySearch(
        query: String,
        labels: Array<String>,
        packages: Array<String>
    ): IntArray
}
