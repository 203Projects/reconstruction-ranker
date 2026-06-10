package com.jaejal.reconstruction.design

/**
 * Central brand strings. The product was renamed 재건축 랭커 → 재잘 (Jaejal); keeping the
 * names in one place means the rename is one edit, not a scavenger hunt across screens.
 *
 * Lives in `design` (not `ui`) so both the design system (Logo, components) and the
 * UI layer can reference it without a `design → ui` dependency.
 */
object Brand {
    /** The product name shown to users. "재잘" = chatter/chirp — a friendly, talkative tone. */
    const val PRODUCT_NAME: String = "재잘"

    /** The guide mascot — the gold ㅈ-roof logo come alive. */
    const val MASCOT_NAME: String = "재잘이"

    /** Home-hero tagline for the Duolingo-style pivot. */
    const val TAGLINE: String = "복잡한 재건축, 재잘이가 떠먹여 드려요"
}
