package com.jaejal.reconstruction.ui

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on iOS: in-app "← 뒤로" handles the back stack.
    // Future: route iOS gesture via UIScreenEdgePanGestureRecognizer if needed.
}
