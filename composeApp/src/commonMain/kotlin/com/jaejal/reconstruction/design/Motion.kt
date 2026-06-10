package com.jaejal.reconstruction.design

/**
 * Whether decorative motion (confetti, the reveal scale-in, the celebratory beats) should
 * play. When false, the gamification collapses those to a plain fade so the app respects a
 * user's "reduce motion" preference.
 *
 * v1: stubbed `true` on both platforms (matching the existing no-op iOS expect/actual idiom).
 * v2 wires the real OS flags — Android `Settings.Global.ANIMATOR_DURATION_SCALE != 0`,
 * iOS `UIAccessibility.isReduceMotionEnabled`.
 */
expect val isAnimationEnabled: Boolean
