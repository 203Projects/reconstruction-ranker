package com.jaejal.reconstruction.ui

import androidx.compose.runtime.Composable

/**
 * Hooks the platform back gesture (Android system back / predictive back) into
 * a Compose-side handler. On iOS it's a no-op — Compose UIViewController doesn't
 * have an equivalent system gesture; the in-app "← 뒤로" button is the back affordance.
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
