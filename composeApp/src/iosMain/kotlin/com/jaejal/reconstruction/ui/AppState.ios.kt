package com.jaejal.reconstruction.ui

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

@OptIn(ExperimentalForeignApi::class)
internal actual fun nowMs(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()
