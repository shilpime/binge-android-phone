package com.tatasky.binge.utils

import android.content.Context
import android.view.View
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.home.TabletType

fun LandingActivity.calculateGameAnimCoordinates(
    context: Context,
    position: Int,
    lv: LottieAnimationView,
    bottomNavView: BottomNavigationView
){
    val item = bottomNavView.menu.getItem(position)
    if (getTabletType(context) == TabletType.MOBILE) {
        val x1 = this.findViewById<View>(item.itemId).x
        val x2 = x1 + this.findViewById<View>(item.itemId).width
        val middle = ((x1 + x2) / 2) - (lv.width / 3)
        lv.x = middle
        lv.y = bottomNavView.y
        this.showGameBottomAnim()
    } else {
        val menuView = bottomNavView.getChildAt(0) as BottomNavigationMenuView
        val iconView: View = menuView.getChildAt(position)
            .findViewById(com.google.android.material.R.id.icon) as View
        val location = IntArray(2)
        iconView.getLocationInWindow(location)

        lv.x = location[0].toFloat() - 20
        this.showGameBottomAnim()
    }
}

