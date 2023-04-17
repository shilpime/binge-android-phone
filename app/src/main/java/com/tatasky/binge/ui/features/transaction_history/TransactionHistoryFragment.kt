package com.tatasky.binge.ui.features.transaction_history

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.response.TransactionHistoryList
import com.tatasky.binge.databinding.FragmentTransactionHistoryBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.transaction_history.adapter.TransactionHistoryAdapter
import com.tatasky.binge.utils.*
import com.tatasky.binge.ui.features.updateprofile.ProfileAnalytics
import com.tatasky.binge.utils.NON_DTH_USER
import com.tatasky.binge.utils.imagepicker.ImagePicker
import com.tatasky.binge.utils.navigateUpOrOpenHome
import com.tatasky.binge.utils.showToast
import javax.inject.Inject

class TransactionHistoryFragment : BaseFragment<FragmentTransactionHistoryBinding, THViewModel>(),
    TransactionHistoryAdapter.OnClickInvoiceIcon {

    private var invoiceNo: String?=""
    private lateinit var adapter: TransactionHistoryAdapter
    @Inject
    lateinit var profileAnalytics: ProfileAnalytics

    private val args by navArgs<TransactionHistoryFragmentArgs>()
    val util = CustomSnackbarWithTwoActionsUtil()

    override fun getViewModelClass(): Class<THViewModel> {
        return THViewModel::class.java
    }

    override fun layoutId(): Int {
        return R.layout.fragment_transaction_history
    }

    override fun getViewModelOwner(): ViewModelStoreOwner {
        return this
    }

    override fun setObserver() {
        viewModel.getTransactionHistory().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { response ->
                response.data?.takeIf { it.isNotEmpty() }?.let { it1 -> setAdapter(it1) }?:binding.tvEmpty.show()
            }
        })

        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().popBackStack()
            }
        })

        /* viewModel.getDownloadComplete().observe(viewLifecycleOwner, Observer {
             it.getContentIfNotHandled()?.let { response ->

               if(downloadFile(response,requireContext(),viewModel.selectedId)){
                   viewModel.sharedPrefs.setPDFDownloaded(viewModel.selectedId)
                   //viewModel.setMockResponse(readRawFile())
                   viewModel.fetchTransactionHistory()
               }
             }
         })*/
    }

    override fun toBeCalledOnce() {
        profileAnalytics.trackTransactionHistoryInitiate()
        binding.headerSubID = getString(R.string.tran_history_sub_id, sharedPrefs.getOriginalSubscriberId())
        binding.headerAliasName = args.aliasName
//        if (viewModel.sharedPrefs.getDthStatusFreemium().equals(NON_DTH_USER, true))
//            viewModel.fetchTransactionHistoryForNonDTHUser()
//        else
//            viewModel.fetchTransactionHistory()

        viewModel.fetchTransactionHistory()

        viewModel.getInvoiceDownloadComplete().observe(viewLifecycleOwner){
            it.getContentIfNotHandled()?.let {
                it.data?.let {
                    downloadFileBase64(it.paymentInvoice?:"",requireContext(),it.fileName?:"")?.let{
                        //showToast(requireContext(),"Download Completed",R.drawable.ic_download_complete)
                        util.showCustomSnackbarWithTwoActions(requireContext(),CustomSnackbarWithTwoActionsType.SnackbarTypeNormalSizeImage,
                            getString(R.string.invoice_downloaded),"",
                            R.drawable.ic_download_complete,R.drawable.ic_download_complete,
                            null,getString(R.string.view),
                            0,0,{
                                val pdfIntent = Intent(Intent.ACTION_VIEW)
                                pdfIntent.setDataAndType(it, "application/pdf")
                                pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                startActivity(pdfIntent)
                                util.hideCustomSnackbarWithTwoActions()
                            },null)
                    }
                }
            }
        }
//        setAdapter(listOf()) // To test only with dummy transaction data
    }

    private fun setAdapter(transactionHistoryList: List<TransactionHistoryList>) {
        /** Mock transaction to test the view */
        /*val mockTransaction = TransactionHistoryList().apply {
            description = "Tata Sky Binge Premium Trial"
            date = "01/01/2020 10:00:54"
            amount = "1999.00"
            transactionId = 456436455
        }
        val mockTransactionsList = mutableListOf<TransactionHistoryList>()
        for (i in 0..10)
            mockTransactionsList.add(mockTransaction)
        binding.thRecycler.adapter = TransactionHistoryAdapter(mockTransactionsList)*/
        adapter=TransactionHistoryAdapter(
            transactionHistoryList,
            viewModel.sharedPrefs.getDthStatusFreemium().equals(NON_DTH_USER, true))
        adapter.setInvoiceDownloadClickListener(this)
        binding.thRecycler.adapter =adapter
        binding.thRecycler.show()
        binding.tvEmpty.hide()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantedResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantedResults)
        when (requestCode) {
            ImagePicker.PERMISSION_REQUEST_CODE ->
                if (grantedResults.isNotEmpty() && grantedResults.get(0) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    viewModel.getBase64Pdf(invoiceNo)
                    Log.d("Permission","Download_Permission_Granted")
                } else {
                    showToast(context, getString(R.string.permission_denied))
                }
        }
    }


    override fun onDestroyView() {
        if(::adapter.isInitialized)
            adapter.clearListener()
        util.hideCustomSnackbarWithTwoActions()
        super.onDestroyView()
    }

    private fun checkRuntimePermission(activity: Activity) {
        //check permission at runtime'
        when {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("Permission","Download_Permission_Granted_Already")
                viewModel.getBase64Pdf(invoiceNo)
                // viewModel.downloadPDF(url,transactionId)
            }
            else -> {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    ImagePicker.PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onInvoiceClick(invoiceNo: String?) {
        this.invoiceNo=invoiceNo
        checkRuntimePermission(requireActivity())
    }


}