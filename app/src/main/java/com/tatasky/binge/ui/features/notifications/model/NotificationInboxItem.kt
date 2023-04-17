package com.tatasky.binge.ui.features.notifications.model
import com.tatasky.binge.data.networking.models.notifications.MoEngageGenericModel

class NotificationInboxItem() {
    var isHeader: Boolean = false
    var image: String = ""
    var isRead: Boolean = false
    var isClicked: Boolean = false
    var description: String? = ""
    var contentTitle: String? = ""
    var deepLinkUrl : String? = null
    var ctId : String? = null
    var notificationTime : Long  =0L
    var screenData : String? = null
    var id: String = "0"
    var isDetailPage: Boolean = false
    var title: String = ""
    var provider: String = ""
    var payload: MoEngageGenericModel? = null
    fun getImageItem() : String{
        if(!image.isEmpty()) return  image
        else return ""
    }

}
