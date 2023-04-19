package com.tatasky.binge.ui.features.notifications

//import com.tatasky.binge.ui.features.notifications.adapter.NotifictionListAdapter
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.R
import com.tatasky.binge.utils.dpToPx


abstract class SwipeToDeleteCallback(val context: Context) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_camera)
    private val paint = Paint()
    private val intrinsicWidth = deleteIcon!!.intrinsicWidth
    private val intrinsicHeight = deleteIcon!!.intrinsicHeight
    private val background = ColorDrawable()
    private val backgroundColor = Color.parseColor("#f44336")
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }


    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean,
    ) {

        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive
        // Calculate position of delete icon
        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop + intrinsicHeight
        if (isCanceled) {
            clearCanvas(c,
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        // Draw the red delete background
        background.color = backgroundColor
        background.setBounds(itemView.right + dX.toInt(),
            itemView.top,
            itemView.right,
            itemView.bottom)
        background.draw(c)

        // Draw the delete icon
        //   deleteIcon!!.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        //  deleteIcon!!.draw(c)

        //Setting Swipe Text

        val rightButton = RectF(deleteIconLeft.toFloat(),
            deleteIconTop.toFloat(),
            deleteIconRight.toFloat(),
            deleteIconBottom.toFloat()
        )
        // paint.setColor(Color.parseColor("#00000000"))
        //c.drawRect(rightButton, paint)
        drawText("Delete", c, rightButton, paint)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun drawText(text: String, c: Canvas, button: RectF, p: Paint) {
        val textSize = dpToPx(context, 14).toFloat()
        p.color = Color.WHITE
        //  p.isAntiAlias = true
        p.textSize = textSize

        val textWidth = p.measureText(text)
        c.drawText(text, button.centerX() - textWidth / 2, button.centerY() + textSize / 2, p)
    }

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        //  paint.apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
        c?.drawRect(left, top, right, bottom, clearPaint)
    }
}