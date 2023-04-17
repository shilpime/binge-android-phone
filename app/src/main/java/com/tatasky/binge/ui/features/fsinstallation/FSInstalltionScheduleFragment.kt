package com.tatasky.binge.ui.features.fsinstallation

import android.app.DatePickerDialog
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tatasky.binge.R
import com.tatasky.binge.analytics.DIY
import com.tatasky.binge.analytics.FSD
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.SlotSuggestion
import com.tatasky.binge.databinding.FragmentScheduleInstallationBinding
import com.tatasky.binge.interfaces.ConfettiDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.enable
import com.tatasky.binge.ui.features.dialog.ConfettiDialogModel
import com.tatasky.binge.ui.features.dialog.ConfettiDialogViewModel
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.utils.DEVICE_DATE_TIME_FORMAT
import com.tatasky.binge.utils.startHomeScreen
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class FSInstalltionScheduleFragment : BaseFragment<FragmentScheduleInstallationBinding, FSInstallationViewModel>() {
    val args: FSInstalltionScheduleFragmentArgs by navArgs()

    @Inject
    lateinit var mViewModelFactory : ViewModelProvider.Factory

    @Inject
    lateinit var subscriptionAnalytics: SubscriptionAnalytics
    override fun getViewModelClass(): Class<FSInstallationViewModel> =
        FSInstallationViewModel::class.java

    var dateSelected: Calendar? = null
    var timeSelected: String? = null
    var endTimeSelected: Calendar? = null
    var startTimeSelected: Calendar? = null
    var taskId = ""
    private var isDiy = false
    
    override fun layoutId(): Int = R.layout.fragment_schedule_installation
    override fun getViewModelOwner(): ViewModelStoreOwner =
        findNavController().getViewModelStoreOwner(R.id.nav_fs_journey)

    override fun setObserver() {
        viewModel.getSlots().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { slotsResponse ->
                timeSelected = null
                taskId = slotsResponse.data?.taskId?.taskId ?: ""
                binding.spinner.isEnabled = true
                if (!slotsResponse.data?.slotSuggestions.isNullOrEmpty()) {
                    val categories = getTimeSlots(slotsResponse.data?.slotSuggestions!!)
                    val dataAdapter: ArrayAdapter<String> = object : ArrayAdapter<String>(requireContext(), R.layout.layout_spinner_item, categories) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val v: View = super.getView(position, convertView, parent)
                            if (position == count) {
                                (v.findViewById(android.R.id.text1) as TextView).text = ""
                                (v.findViewById(android.R.id.text1) as TextView).hint = getItem(count) //"Hint to be displayed"
                            } else {
                                (v.findViewById<TextView>(android.R.id.text1)).text = ""
                            }
                            return v
                        }

                        override fun getCount(): Int {
                            return super.getCount() - 1 // you dont display last item. It is used as hint.
                        }

                    }
                    dataAdapter.setDropDownViewResource(R.layout.layout_spinner_drop_down_item)
                    binding.spinner.adapter = dataAdapter
                    binding.spinner.setSelection(dataAdapter.count)

                    binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {
                        }

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            timeSelected = parent!!.getItemAtPosition(position).toString()
                            if (timeSelected.equals(getString(R.string.time_slot_hint))) {
                                view?.findViewById<TextView>(android.R.id.text1)?.apply {
                                    hint = "$timeSelected"
                                    text = ""
                                }
                                timeSelected = null
                            } else {
                                view?.findViewById<TextView>(android.R.id.text1)?.apply {
                                    text = "$timeSelected"
                                    hint = ""
                                }
                                try {
                                    val slotStartTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).parse(timeSelected?.split(" - ")?.get(0)!!)
                                    val slotEndTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).parse(timeSelected?.split(" - ")?.get(1)!!)
                                    startTimeSelected?.apply {
                                        this.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().apply { time = slotStartTime }.get(Calendar.HOUR_OF_DAY))
                                        this.set(Calendar.MINUTE, Calendar.getInstance().apply { time = slotStartTime }.get(Calendar.MINUTE))
                                        this.set(Calendar.SECOND, 0)
                                    }
                                    endTimeSelected?.apply {
                                        this.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().apply { time = slotEndTime }.get(Calendar.HOUR_OF_DAY))
                                        this.set(Calendar.MINUTE, Calendar.getInstance().apply { time = slotEndTime }.get(Calendar.MINUTE))
                                        this.set(Calendar.SECOND, 0)
                                    }
                                } catch (e: Exception) {
                                    timeSelected = null
                                    onError(ErrorModel(message = "Invalid Time Format"))
                                }
                            }
                            if (dateSelected != null && timeSelected != null) {
                                binding.btnProceed.enable()
                            }
                        }
                    }
                } else {
                    onError(ErrorModel(message = "No slots found"))
                }
            }
        })

        viewModel.isWorkOrderProcessed().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it1 ->
                showScheduleDialog()
            }
        })
    }

    private fun showScheduleDialog() {

        viewModel.sharedPrefs.saveFirestickTaken(true)
        val format = SimpleDateFormat("d", Locale.getDefault())
        val date = format.format(dateSelected!!.time)
        val f = if (date.endsWith("1") && !date.endsWith("11"))
            SimpleDateFormat("EEEE d'st' MMM yyyy", Locale.getDefault())
        else if (date.endsWith("2") && !date.endsWith("12"))
            SimpleDateFormat("EEEE d'nd' MMM yyyy", Locale.getDefault())
        else if (date.endsWith("3") && !date.endsWith("13"))
            SimpleDateFormat("EEEE d'rd' MMM yyyy", Locale.getDefault())
        else
            SimpleDateFormat("EEEE d'th' MMM yyyy", Locale.getDefault())
        val displayDate = f.format(dateSelected!!.time)
//        showDialog(
//            DialogModel(
//                false,
//                R.drawable.ic_success_tick,
//                if (isDiy) {
//                    getString(R.string.thank_you)
//                } else {
//                    getString(R.string.installation_scheduled)
//                },
//                getString(R.string.start_watching_now),
//                null,
////                        getString(R.string.re_schedule_installation),
//                "$displayDate \n $timeSelected \n ${getString(R.string.details_sent_via_sms)}"
//            ), object :
//                CommonDialogEventListener {
//                override fun onPrimaryButtonClick() {
//                    hideDialog()
//                    startHomeScreen(activity)
//                }
//
//                override fun onSecondaryButtonClick() {
//                    hideDialog()
//                }
//
//                override fun onCloseButtonClick() {
//                }
//            })

        val dialogTitle = if (isDiy) {
            getString(R.string.thank_you)
        } else {
            getString(R.string.installation_scheduled)
        }

        val dialogSubTitle = if (isDiy) {
            "${getString(R.string.details_diy_option)} $displayDate \n $timeSelected"
        } else {
            "$displayDate \n $timeSelected \n ${getString(R.string.details_sent_via_sms)}"
        }

        val dialogViewModel =
            ViewModelProvider(requireActivity(), mViewModelFactory).get(ConfettiDialogViewModel::class.java)
        val dialogModel =
            ConfettiDialogModel(
                R.drawable.ic_tick_login_success,
                dialogTitle,
                "Done",
                dialogSubTitle,
                "",
                false

            )
        val eventListener = object : ConfettiDialogEventListener {
            override fun onPrimaryButtonClick() {
                (activity as? FSInstallationActivity)?.hideConfettiDialog()
                startHomeScreen(requireActivity())
            }
        }
        dialogViewModel.setDialogModel(dialogModel)
        dialogViewModel.setEventHandler(eventListener)
        (activity as? FSInstallationActivity)?.showConfettiDialog()
    }

    private fun getTimeSlots(slotSuggestion: List<SlotSuggestion>): List<String> {
        val results = arrayListOf<String>()
        for (slot in slotSuggestion) {
            results.add("${slot.start} - ${slot.end}")
        }
        results.add(getString(R.string.time_slot_hint))
        return results
    }

    override fun toBeCalledOnce() {
        binding.spinner.isEnabled = false
        isDiy = !viewModel.installationReq

        if (isDiy) {
            binding.tvTitle.text = getString(R.string.installation_schedule_diy)
            binding.tvSubLandingTitle.text = getString(R.string.installation_schedule_subtitle_diy)
        } else {
            binding.tvTitle.text = getString(R.string.installation_schedule)
            binding.tvSubLandingTitle.text = getString(R.string.installation_schedule_subtitle)
        }

        var dateSelector = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            binding.flCalendar.text = getString(R.string.date_template, dayOfMonth, month+1, year)
            val calendar = GregorianCalendar()
            calendar.set(year, month, dayOfMonth)
            dateSelected = calendar
            startTimeSelected = Calendar.getInstance().apply { timeInMillis = calendar.timeInMillis}
            endTimeSelected = Calendar.getInstance().apply { timeInMillis = calendar.timeInMillis}
            binding.spinner.adapter = null
            binding.spinner.isEnabled = false
            viewModel.fetchSlots(args.address, SimpleDateFormat(DEVICE_DATE_TIME_FORMAT, Locale.getDefault()).format(Date(calendar.timeInMillis)))
            binding.btnProceed.isEnabled = false
        }

        val c: Calendar = Calendar.getInstance()
        val year: Int = c.get(Calendar.YEAR)
        val month: Int = c.get(Calendar.MONTH)
        val day: Int = c.get(Calendar.DAY_OF_MONTH)
        binding.flCalendar.til.setOnClickListener {
            val datePickerDialog = DatePickerDialog(requireContext(), dateSelector, year, month, day)
//            datePickerDialog.datePicker.minDate = System.currentTimeMillis() //next day
            datePickerDialog.datePicker.minDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH,1) }.timeInMillis
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 29) }.timeInMillis // next month max
            datePickerDialog.show()
        }


        binding.btnNotNow.setOnClickListener {
            subscriptionAnalytics.trackFsWo(false, if(isDiy) DIY else FSD)
            startHomeScreen(activity)
        }

        binding.btnProceed.setOnClickListener {
            if (dateSelected != null && timeSelected != null && startTimeSelected != null && endTimeSelected != null) {
                subscriptionAnalytics.trackFsWo(true, if(isDiy) "DIY" else "FSD")
                if (args.address.data?.ocsFlag.equals("Y", true))
                    viewModel.confirmSlot(taskId, args.address, SimpleDateFormat(DEVICE_DATE_TIME_FORMAT, Locale.getDefault()).format(startTimeSelected!!.time)
                        , SimpleDateFormat(DEVICE_DATE_TIME_FORMAT, Locale.getDefault()).format(endTimeSelected!!.time), isDiy)
                else {
                    viewModel.callFirestickWorkOrder(SimpleDateFormat(DEVICE_DATE_TIME_FORMAT, Locale.getDefault()).format(startTimeSelected!!.time)
                        , SimpleDateFormat(DEVICE_DATE_TIME_FORMAT, Locale.getDefault()).format(endTimeSelected!!.time), isDiy)
                }
            } else {
                onError(ErrorModel(message = "Invalid slot was selected"))
            }

        }

    }
}