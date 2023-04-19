package com.tatasky.binge.ui.features.notifications

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentNotificationBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.onUIComplete
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.notifications.adapter.TYPE_HEADER
import javax.inject.Inject

class NotificationFragment : BaseFragment<FragmentNotificationBinding, NotificationViewModel>() {

    private var mMenuSelectItem: MenuItem? = null
    private var mDataAvailable = false

    @Inject
    lateinit var analytics: NotificationAnalytics

    private val onBackPressedCallback = object : OnBackPressedCallback(
        true // default to enabled
    ) {
        override fun handleOnBackPressed() {
            if (mMenuSelectItem?.title.toString().trim() != resources.getString(R.string.select)) {
                viewModel.getNotificationAdapter().setEditMode(false)
            } else {
                isEnabled = false
                activity?.onBackPressed()
            }
        }
    }


    override fun layoutId(): Int = R.layout.fragment_notification

    override fun getViewModelClass(): Class<NotificationViewModel> =
        NotificationViewModel::class.java


    override fun getViewModelOwner(): ViewModelStoreOwner =
        findNavController().getViewModelStoreOwner(R.id.nav_notifications)

    override fun toBeCalledOnce() {
        setHasOptionsMenu(true)
        binding.vm = viewModel
        setListeners()
        analytics.trackNotificationScreenVisit()
        binding.root.post {
            context?.let {
                viewModel.fetchNotificationCTList(it)
            }
        }
    }


    override fun setObserver() {
        viewModel.watchNotificationList.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { contentList ->
                mDataAvailable = contentList.size > 0
                setUpDataView()
                binding.rvNotification.onUIComplete {
                    viewModel.setReadStatus()
                }
            }
        }
        viewModel.getSelectedNotificationCount().observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { selectedItemSize ->
                handleMenuItemText(selectedItemSize)
                mMenuSelectItem?.let { applyFontToMenuItem(it,R.font.volteplay_semibold) }

            }
        }

        viewModel.getNotificationAvailableStatus().observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { dataAvailable ->
                mDataAvailable = dataAvailable
                setUpDataView()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_select, menu)
        mMenuSelectItem = menu.findItem(R.id.menu_select)
        mMenuSelectItem?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_select -> {
                when (item.title.toString().trim()) {
                    resources.getString(R.string.select) -> {
                        viewModel.getNotificationAdapter().setEditMode(true)
                    }
                    resources.getString(R.string.cancel) -> {
                        viewModel.getNotificationAdapter().setEditMode(false)
                        binding.tvSelectAll.hide()
                    }
                    else -> {
                        viewModel.getNotificationAdapter().removeNotification()
                    }
                }
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        mMenuSelectItem?.let { applyFontToMenuItem(it,R.font.volteplay_semibold) }

            if (mDataAvailable)
                mMenuSelectItem?.isVisible = true
            else
                return
            viewModel.getSelectedNotificationCount().value?.peekContent()?.let { selectedItemSize ->
                handleMenuItemText(selectedItemSize)
            }
        super.onPrepareOptionsMenu(menu)
        }



    private fun handleMenuItemText(selectedItemSize: Int) {
        when (selectedItemSize) {
            -1 -> {
                mMenuSelectItem?.title = resources.getString(R.string.select)
                binding.tvSelectAll.hide()
            }
            0 -> {
                mMenuSelectItem?.title = resources.getString(R.string.cancel)
                binding.tvSelectAll.text = resources.getString(R.string.select_all)
                binding.tvSelectAll.show()
            }
            else -> {
                mMenuSelectItem?.title =
                    resources.getString(R.string.remove_with_number, selectedItemSize)
                if (binding.tvSelectAll.text == resources.getString(R.string.select_all)) {
                    binding.tvSelectAll.text = resources.getString(R.string.unselect_all)
                }
            }
        }
    }

    private fun setUpDataView() {
        if (mDataAvailable) {
            mMenuSelectItem?.isVisible = true
            binding.tvEmpty.hide()
            binding.groupMarkAllRead.show()
        } else {
            mMenuSelectItem?.isVisible = false
            binding.tvEmpty.show()
            binding.groupMarkAllRead.hide()
        }

    }

    private fun setListeners() {

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        binding.tvSelectAll.setOnClickListener {
            if (binding.tvSelectAll.text == getString(R.string.select_all)) {
                viewModel.getNotificationAdapter().handleAllSelection(true)
                binding.tvSelectAll.text = getString(R.string.unselect_all)
            } else {
                viewModel.getNotificationAdapter().handleAllSelection(false)
                binding.tvSelectAll.text = getString(R.string.select_all)
            }
        }

        binding.tvMarkAllRead.setOnClickListener {
            viewModel.getNotificationAdapter().markAllRead()
        }

        val itemTouchHelper = ItemTouchHelper(object : SwipeToDeleteCallback(binding.root.context) {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
            ): Int {
                if (mMenuSelectItem?.title.toString().trim() != resources.getString(R.string.select)
                    || viewHolder.itemViewType == TYPE_HEADER
                ) {
                    return 0
                }
                return super.getMovementFlags(recyclerView, viewHolder)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.getNotificationAdapter()
                    .removeNotificationOnPosition(viewHolder.absoluteAdapterPosition)
                super.onSwiped(viewHolder, direction)
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.rvNotification)

    }

}