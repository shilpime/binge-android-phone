package com.tatasky.binge.ui.features.sidemenunavdrawer

sealed class NavDrawerActions(/*private var isrenew:Boolean=false*/) {
    object LoginClicked : NavDrawerActions()
    object EditProfile : NavDrawerActions()
    object Recharge : NavDrawerActions()
    object Renew : NavDrawerActions()
    object GoVipClicked : NavDrawerActions()
    object MyPlanClicked : NavDrawerActions()
    object BingeListClicked : NavDrawerActions()
    object NotificationClicked : NavDrawerActions()
    object SettingsClicked : NavDrawerActions()
    object HelpAndSupportClicked : NavDrawerActions()
    object TnCClicked : NavDrawerActions()
    object PrivacyPolicyClicked : NavDrawerActions()
}
