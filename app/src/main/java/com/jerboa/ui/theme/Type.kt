package com.jerboa.ui.theme

import androidx.compose.material3.Typography

const val COMPOSE_BODY_LARGE_DEFAULT = 16
val BASE_TYPOGRAPHY = Typography()

fun generateTypography(baseFontSize: Int): Typography {
    val customRatio = baseFontSize.toFloat() / COMPOSE_BODY_LARGE_DEFAULT
    return Typography(
        bodySmall = BASE_TYPOGRAPHY.bodySmall.copy(
            fontSize = BASE_TYPOGRAPHY.bodySmall.fontSize.times(customRatio),
            lineHeight = BASE_TYPOGRAPHY.bodySmall.lineHeight.times(customRatio),
        ),
        bodyMedium = BASE_TYPOGRAPHY.bodyMedium.copy(
            fontSize = BASE_TYPOGRAPHY.bodyMedium.fontSize.times(customRatio),
            lineHeight = BASE_TYPOGRAPHY.bodyMedium.lineHeight.times(customRatio),
        ),
        bodyLarge = BASE_TYPOGRAPHY.bodyLarge.copy(
            fontSize = BASE_TYPOGRAPHY.bodyLarge.fontSize.times(customRatio),
            lineHeight = BASE_TYPOGRAPHY.bodyLarge.lineHeight.times(customRatio),
        ),
        titleSmall = BASE_TYPOGRAPHY.titleSmall.copy(
            fontSize = BASE_TYPOGRAPHY.titleSmall.fontSize.times(customRatio),
            lineHeight = BASE_TYPOGRAPHY.titleSmall.lineHeight.times(customRatio),
        ),
        titleMedium = BASE_TYPOGRAPHY.titleMedium.copy(
            fontSize = BASE_TYPOGRAPHY.titleMedium.fontSize.times(customRatio),
            lineHeight = BASE_TYPOGRAPHY.titleMedium.lineHeight.times(customRatio),
        ),
        titleLarge = BASE_TYPOGRAPHY.titleLarge.copy(
            fontSize = BASE_TYPOGRAPHY.titleLarge.fontSize.times(customRatio),
            lineHeight = BASE_TYPOGRAPHY.titleLarge.lineHeight.times(customRatio),
        ),
        // Headlines use titles font size on purpose, default size is unusable big for Jerboa
        headlineSmall = BASE_TYPOGRAPHY.headlineSmall.copy(
            fontSize = BASE_TYPOGRAPHY.titleSmall.fontSize.times(customRatio),
            lineHeight = BASE_TYPOGRAPHY.titleSmall.lineHeight.times(customRatio),
        ),
        headlineMedium = BASE_TYPOGRAPHY.headlineMedium.copy(
            fontSize = BASE_TYPOGRAPHY.titleMedium.fontSize.times(customRatio),
            lineHeight = BASE_TYPOGRAPHY.titleMedium.lineHeight.times(customRatio),
        ),
        headlineLarge = BASE_TYPOGRAPHY.headlineLarge.copy(
            fontSize = BASE_TYPOGRAPHY.titleLarge.fontSize.times(customRatio),
            lineHeight = BASE_TYPOGRAPHY.titleLarge.lineHeight.times(customRatio),
        ),
        labelSmall = BASE_TYPOGRAPHY.labelSmall.copy(
            fontSize = BASE_TYPOGRAPHY.labelSmall.fontSize.times(customRatio),
            lineHeight = BASE_TYPOGRAPHY.labelSmall.lineHeight.times(customRatio),
        ),
        labelMedium = BASE_TYPOGRAPHY.labelMedium.copy(
            fontSize = BASE_TYPOGRAPHY.labelMedium.fontSize.times(customRatio),
            lineHeight = BASE_TYPOGRAPHY.labelMedium.lineHeight.times(customRatio),
        ),
        labelLarge = BASE_TYPOGRAPHY.labelLarge.copy(
            fontSize = BASE_TYPOGRAPHY.labelLarge.fontSize.times(customRatio),
            lineHeight = BASE_TYPOGRAPHY.labelLarge.lineHeight.times(customRatio),
        ),
    )
}
