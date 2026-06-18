package com.social.flare.core.ui.theme

fun textSizeScaleToFontScale(value: Float): Float {
    return 0.85f + value.coerceIn(0f, 1f) * 0.3f
}
