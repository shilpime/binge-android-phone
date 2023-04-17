package com.tatasky.binge.ui.features.notifications.adapter
//
//import android.animation.ObjectAnimator
//import android.content.Context
//import android.text.Spannable
//import android.text.style.ForegroundColorSpan
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//import com.moengage.core.internal.utils.ISO8601Utils
//import com.tatasky.binge.R
//import com.tatasky.binge.data.networking.models.response.ContentItem
//import com.tatasky.binge.data.networking.models.response.ProviderLogo
//import com.tatasky.binge.databinding.LayoutNotificationListItemsBinding
//import com.tatasky.binge.databinding.NotificationHeaderviewBinding
//import com.tatasky.binge.helper.imageLoad
//import com.tatasky.binge.ui.base.frameworks.extensions.hide
//import com.tatasky.binge.ui.base.frameworks.extensions.show
//import com.tatasky.binge.ui.features.notifications.NotificationViewModel
//import com.tatasky.binge.utils.dpToPx
//import com.tatasky.binge.utils.getCloudinaryUrl
//import com.tatasky.binge.utils.updateProviderImage
//import java.util.concurrent.TimeUnit
//
//
//class NotifictionListAdapter(
//    var list: ArrayList<ContentItem>,
//    val viewModel: NotificationViewModel,
//    val context: Context,
//    val cloudinaryUrl: String?,
//    val itemClickListener: ItemClickListener,
//    val providerLogos: ProviderLogo
//) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//    private val TYPE_HEADER: Int = 1
//    private val TYPE_LIST: Int = 2
//    var isItemClicked: Boolean = false
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//
//        val vh: RecyclerView.ViewHolder
//
//        if (viewType == TYPE_HEADER) {
//            vh = TitleViewHolder(
//                NotificationHeaderviewBinding.inflate(
//                    LayoutInflater.from(parent.context),
//                    parent,
//                    false
//                )
//            )
//        } else {
//            vh = NotificationItemViewHolder(
//                LayoutNotificationListItemsBinding.inflate(
//                    LayoutInflater.from(context),
//                    parent,
//                    false
//                ), viewModel, itemClickListener, isItemClicked
//            )
//        }
//        return vh
//    }
//
//    class TitleViewHolder(val binding: NotificationHeaderviewBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//        fun bind(title: String) {
//            binding.title = title
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return list.size
//    }
//
//    override fun getItemId(position: Int): Long {
//        return list.get(position).hashCode().toLong()
//    }
//
//    fun removeAt(position: Int) {
//        val removeAt = list.removeAt(position)
//        viewModel.deleteNotification(removeAt.notificationInboxMessage)
//        var headerIndex = -1
//        if (position < list.size - 1 &&
//            list[position].isHeader && list[position - 1].isHeader
//        ) {
//
//            list.removeAt(position - 1)
//            headerIndex = position - 1
//        } else if (position == list.size && list[position - 1].isHeader) {
//            list.removeAt(position - 1)
//            headerIndex = position - 1
//        }
//        notifyItemRemoved(position)
//        if (headerIndex >= 0) notifyItemRemoved(headerIndex)
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        when (holder) {
//            is TitleViewHolder -> {
//                val content = list[position]
//
//                holder.itemView.context.getString(R.string.selected_apps_title)
//                holder.bind(content.title)
//                if (viewModel.isEditVisible) {
//                    if (holder.binding.leftSwipe.visibility == View.GONE) {
//                        holder.binding.leftSwipe.show()
//
//                        var textViewAnimator = ObjectAnimator.ofFloat(
//                            holder.binding.tvTitle,
//                            "translationX",
//                            0f,
//                            0f,
//                            dpToPx(context, 45).toFloat()
//                        )
//                        textViewAnimator.setDuration(200)
//                        textViewAnimator.start()
//
//                    }
//                } else {
//                    if (holder.binding.leftSwipe.visibility == View.VISIBLE) {
//                        var textViewAnimator = ObjectAnimator.ofFloat(
//                            holder.binding.tvTitle,
//                            "translationX",
//                            dpToPx(context, 45).toFloat(),
//                            0f
//                        );
//                        textViewAnimator.setDuration(200)
//                        textViewAnimator.start()
//                    }
//                    holder.binding.leftSwipe.hide()
//                }
//
//                if (viewModel.deleteArray.contains(content)) {
//                    viewModel.deleteArray.remove(content)
//                }
//            }
//            is NotificationItemViewHolder -> {
//                val contentItem = list[position]
//                val currentTime = System.currentTimeMillis()
//                val notificationReceivedTime = ISO8601Utils.parse(contentItem.notificationInboxMessage?.receivedTime).time
//                val diffTime = currentTime - notificationReceivedTime
//                when {
//                    TimeUnit.MILLISECONDS.toHours(diffTime) in 1..24 -> {
//                        holder.binding.duration.text = context.getString(R.string.hour, TimeUnit.MILLISECONDS.toHours(diffTime))
//                    }
//                    TimeUnit.MILLISECONDS.toDays(diffTime) > 0 -> {
//                        holder.binding.duration.text = context.getString(R.string.day, TimeUnit.MILLISECONDS.toDays(diffTime))
//                    }
//                    else -> {
//                        holder.binding.duration.text = context.getString(R.string.minute, TimeUnit.MILLISECONDS.toMinutes(diffTime))
//                    }
//                }
//                holder.bind(contentItem)
//
//
//                if (viewModel.pageType.equals(context.getString(R.string.watch))) {
//                    holder.binding.ivProvider.show()
//                    holder.binding.image.show()
//                    if (contentItem.id == "NULL") {
//                        holder.binding.image.scaleType = ImageView.ScaleType.FIT_CENTER
//                        holder.binding.image.setImageResource(R.drawable.medium_binge_logo)
//                        holder.binding.ivProvider.hide()
//                    } else {
//                        holder.binding.image.scaleType = ImageView.ScaleType.FIT_XY
//                        val width = dpToPx(holder.binding.image.context, 98)
//                        val height = dpToPx(holder.binding.image.context, 64)
//                        val url = getCloudinaryUrl(
//                            cloudinaryUrl,
//                            width, height,
//                            contentItem.getImageItem()
//                        )
//                        imageLoad(holder.binding.image, url)
//                        updateProviderImage(
//                            holder.binding.ivProvider,
//                            contentItem.provider,
//                            providerLogos,
//                            R.drawable.ic_rail_placeholder
//                        )
//                    }
//                } else {
//                    holder.binding.ivProvider.hide()
//                    holder.binding.image.hide()
//                }
//
//                if (contentItem.isRead) {
//                    holder.binding.container.setBackgroundColor(
//                        ContextCompat.getColor(
//                            context,
//                            R.color.darkBackground
//                        )
//                    )
//                } else {
//                    holder.binding.container.setBackgroundColor(
//                        ContextCompat.getColor(
//                            context,
//                            R.color.darkHighlight
//                        )
//                    )
//                }
//                if (viewModel.isEditVisible) {
//
//                    if (holder.binding.viewDelete.visibility == View.GONE && viewModel.isItemClicked != position) {
//                        holder.binding.viewDelete.show()
//                        //   val view = holder.itemView as SwipeRevealLayout
//                        //   view.dragLock(true)
//                        //  view.open(true)
//                        var textViewAnimator = ObjectAnimator.ofFloat(
//                            holder.binding.frameLayout,
//                            "translationX",
//                            0f,
//                            dpToPx(context, 45).toFloat()
//                        )
//                        textViewAnimator.setDuration(200)
//                        textViewAnimator.start()
//
//                    }
//                    if (viewModel.isItemClicked == position) {
//                        viewModel.isItemClicked = -1
//                        holder.binding.frameLayout.translationX = dpToPx(context, 45).toFloat()
//                    }
//                    holder.binding.viewDelete.show()
//                    holder.binding.deleteCheckbox.isChecked =
//                        viewModel.deleteArray.contains(contentItem)
//                } else {
//                    if (holder.binding.viewDelete.visibility == View.VISIBLE) {
//                        var textViewAnimator = ObjectAnimator.ofFloat(
//                            holder.binding.frameLayout,
//                            "translationX",
//                            dpToPx(context, 45).toFloat(),
//                            0f
//                        );
//                        textViewAnimator.setDuration(200)
//                        textViewAnimator.start()
//                    }
//                    holder.binding.viewDelete.hide()
//                    viewModel.isItemClicked = -1
//
//                }
//
//            }
//        }
//    }
//
//    class NotificationItemViewHolder(
//        val binding: LayoutNotificationListItemsBinding,
//        val viewModel: NotificationViewModel,
//        val itemClickListener: ItemClickListener,
//        var isItemClicked: Boolean
//    ) :
//        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
//        lateinit var model: ContentItem
//
//        fun bind(model: ContentItem) {
//
//            this.model = model
//            binding.contentItem = model
//            binding.desc.text = model.description
//            binding.desc.tag = model.description
//            binding.container.setOnClickListener(this)
//            binding.deleteCheckbox.setOnClickListener(this)
////            var isCollapsed = true
////            val mainText = model.description
////            val lessSuffix = " - Less"
////            val moreSuffix = " + More"
////            binding.desc.maxLines = 2
////            binding.desc.post {
////                if(binding.desc.lineCount > 2) {
////                    var x = (binding.desc.text.length / binding.desc.lineCount) * 2 - moreSuffix.length
////                    binding.desc.maxLines = 2
////                    val newText = binding.desc.text.removeRange(
////                        x, binding.desc.text.length
////                    )
////                    binding.desc.setText(
////                        String.format("%s%s", newText, moreSuffix),
////                        TextView.BufferType.SPANNABLE
////                    )
////                    val s: Spannable = binding.desc.text as Spannable
////                    val start = x
////                    val end = x + moreSuffix.length
////                    s.setSpan(
////                        ForegroundColorSpan(
////                            ContextCompat.getColor(
////                                binding.desc.context!!,
////                                R.color.more_color
////                            )
////                        ), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
////                    )
////                    isCollapsed = true
////
////                    binding.desc.setOnClickListener {
////                        if (isCollapsed) {
////                            binding.desc.maxLines = Integer.MAX_VALUE
////                            binding.desc.setText(String.format("%s%s", mainText,lessSuffix), TextView.BufferType.SPANNABLE)
////                            val s: Spannable = binding.desc.text as Spannable
////                            val start = mainText.length
////                            val end = start + lessSuffix.length
////                            s.setSpan(
////                                ForegroundColorSpan(
////                                    ContextCompat.getColor(
////                                        binding.desc.context!!,
////                                        R.color.more_color
////                                    )
////                                ), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
////                            )
////                            isCollapsed = false
////                        } else {
////                            var x = (binding.desc.text.length / binding.desc.lineCount) * 2
////                            binding.desc.maxLines = 2
////                            val newText = binding.desc.text.removeRange(
////                                x, binding.desc.text.length
////                            )
////                            binding.desc.setText(
////                                String.format("%s%s", newText, moreSuffix),
////                                TextView.BufferType.SPANNABLE
////                            )
////                            val s: Spannable = binding.desc.text as Spannable
////                            val start = x
////                            val end = x + moreSuffix.length
////                            s.setSpan(
////                                ForegroundColorSpan(
////                                    ContextCompat.getColor(
////                                        binding.desc.context!!,
////                                        R.color.more_color
////                                    )
////                                ), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
////                            )
////                            isCollapsed = true
////                        }
////                        binding.desc.invalidate()
////                    }
////                }
////            }
//        }
//
//        override fun onClick(v: View) {
//
//            when (v) {
//                binding.container -> {
//                    if (adapterPosition == RecyclerView.NO_POSITION) return
//                    if (!model.isRead) {
//                        model.isRead = true
//                        viewModel.markNotificationRead(model.notificationInboxMessage)
//                    }
//                    itemClickListener.onItemClick(v, adapterPosition)
//                    viewModel.isItemClicked = adapterPosition
//                    return
//                }
//                binding.deleteCheckbox -> {
//                    if (adapterPosition == RecyclerView.NO_POSITION) return
//
//                    if (binding.deleteCheckbox.isChecked) {
//                        if (!viewModel.deleteArray.contains(model))
//                            viewModel.deleteArray.add(model)
//                    } else if (viewModel.deleteArray.contains(model)) {
//                        viewModel.deleteArray.remove(model)
//                    }
//                }
//            }
//        }
//
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        return if (!list[position].isHeader)
//            TYPE_LIST
//        else TYPE_HEADER
//    }
//
//    fun updateHeaderView() {
//        if (list.size == 1) {
//            //list.removeAt(0)
//            // notifyItemRemoved(0)
//            list.removeAll(list)
//            notifyDataSetChanged()
//        } else if (list.size > 1)
//            if (list[0].isHeader && list[1].isHeader) {
//                list.removeAt(0)
//                notifyItemRemoved(0)
//            } else if (list[list.size - 1].isHeader) {
//                list.removeAt(list.size - 1)
//                notifyItemRemoved(list.size - 1)
//            }
//    }
//
//    fun update() {
//
//        notifyDataSetChanged()
//    }
//
//    interface ItemClickListener {
//        fun onItemClick(view: View, position: Int)
//    }
//}