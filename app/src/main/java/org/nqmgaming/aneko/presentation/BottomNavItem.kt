package org.nqmgaming.aneko.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ramcosta.composedestinations.generated.destinations.ExploreSkinScreenDestination
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.spec.Direction
import org.nqmgaming.aneko.R

sealed class BottomNavItem(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val route: String,
    val direction: Direction,
) {
    data object Home : BottomNavItem(
        title = R.string.app_name,
        icon = R.drawable.icon,
        route = HomeScreenDestination.route,
        direction = HomeScreenDestination()
    )

    data object Explore : BottomNavItem(
        title = R.string.txt_explore,
        icon = R.drawable.ic_explore,
        route = ExploreSkinScreenDestination.route,
        direction = ExploreSkinScreenDestination()
    )
}