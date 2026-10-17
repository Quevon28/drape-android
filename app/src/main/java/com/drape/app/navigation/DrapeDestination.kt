package com.drape.app.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe top-level navigation destinations for DRAPE.
 * Includes the 4 main navigation tabs (Today, Wardrobe, Style, Profile) while
 * preserving Phase 1 destination objects for backward compatibility.
 */
sealed interface DrapeTopLevelDestination {
    val routeKey: String
    val label: String
}

@Serializable
data object TodayDestination : DrapeTopLevelDestination {
    override val routeKey: String = "today"
    override val label: String = "Today"
}

@Serializable
data object WardrobeDestination : DrapeTopLevelDestination {
    override val routeKey: String = "wardrobe"
    override val label: String = "Wardrobe"
}

@Serializable
data object StyleDestination : DrapeTopLevelDestination {
    override val routeKey: String = "style"
    override val label: String = "Style"
}

@Serializable
data object ProfileDestination : DrapeTopLevelDestination {
    override val routeKey: String = "profile"
    override val label: String = "Profile"
}

@Serializable
data object OutfitStudioDestination : DrapeTopLevelDestination {
    override val routeKey: String = "studio"
    override val label: String = "Studio"
}

@Serializable
data object AiStylistDestination : DrapeTopLevelDestination {
    override val routeKey: String = "stylist"
    override val label: String = "Stylist"
}

/**
 * Canonical 4-tab main navigation bar order for DRAPE.
 */
val DrapeMainTabDestinations: List<DrapeTopLevelDestination> = listOf(
    TodayDestination,
    WardrobeDestination,
    StyleDestination,
    ProfileDestination
)
