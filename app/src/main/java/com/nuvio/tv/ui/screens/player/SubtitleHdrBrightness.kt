package com.nuvio.tv.ui.screens.player

/**
 * Darkens subtitle RGB for HDR playback while keeping the user's text opacity intact.
 * This controls the subtitle signal, not a calibrated display luminance in nits.
 */
internal fun dimSubtitleColorForHdr(color: Int, percent: Int): Int {
    val brightness = percent.coerceIn(20, 100)
    fun channel(shift: Int): Int =
        ((((color ushr shift) and 0xFF) * brightness + 50) / 100)

    return (color and 0xFF000000.toInt()) or
        (channel(16) shl 16) or
        (channel(8) shl 8) or
        channel(0)
}
