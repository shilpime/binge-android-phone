package com.tatasky.binge.ui.features.details

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.tatasky.binge.R
import com.tatasky.binge.customviews.VoiceView
import com.tatasky.binge.databinding.LayoutVoiceSearchDialogBinding
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.features.search.model.SearchViewModel
import com.tatasky.binge.utils.e
import io.reactivex.Completable
import java.lang.Exception
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EpisodeSeeAllVoiceSearchDialogFragment :
    BaseFragment<LayoutVoiceSearchDialogBinding, DetailViewModel>(), VoiceView.Listener, RecognitionListener {

    private var mSpeechRecognizer: SpeechRecognizer? = null
    private var mSpeechRecognizerIntent: Intent? = null

    override fun getViewModelClass(): Class<DetailViewModel> = DetailViewModel::class.java

    override fun layoutId(): Int = R.layout.layout_voice_search_dialog

    override fun getViewModelOwner(): ViewModelStoreOwner =
        navController().getViewModelStoreOwner(R.id.nav_details)

    override fun setObserver() {
    }

    override fun toBeCalledOnce() {
        binding.voiceSearchCancelBtn.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.voiceview.setListener(this)
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity)
        mSpeechRecognizer?.setRecognitionListener(this)
        createRecognizerIntent()
        startListening()
    }

    private fun startListening() {
        mSpeechRecognizer?.startListening(mSpeechRecognizerIntent)
        binding.voiceview.startRecording()
        binding.stateTv.setText(R.string.listening)
        binding.displayTv.setText(R.string.try_saying_text)
    }


    private fun createRecognizerIntent() {
        mSpeechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mSpeechRecognizerIntent?.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
        )
        mSpeechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
        mSpeechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "en-IN")
        //Commented for TSF-5117
        /*mSpeechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 65000 /*Reduce this to 2000ms = 2seconds*/)
        mSpeechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 65000)*/
        /*mSpeechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_SP)*/
        mSpeechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        mSpeechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity?.packageName)
    }

    override fun onStartListen() {
//        searchAnalytics.trackSearchReactivateMic()
        startListening()
    }

    fun onCancel() {
        e("VoiceRecognizerFragment","inside onCancel")
        mSpeechRecognizer?.cancel()
        binding.voiceview.stopRecording()
    }

    override fun onStop() {
        super.onStop()
        onCancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mSpeechRecognizer?.cancel()
            mSpeechRecognizer?.destroy()
            mSpeechRecognizer = null
        }catch (e : Exception){
            //ignore this crash
        }
    }

    override fun onPartialResults(partialResults: Bundle) {
        e("VoiceRecognizerFragment","inside onPartialResults : $partialResults")
        val matches =
            partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
        var i = 0
        var textFromVoice: String? = ""
        for (s in matches) {
            if (i == 0) {
                textFromVoice = s
            }
            i++
        }
        binding.stateTv.text = textFromVoice
        binding.displayTv.visibility = View.GONE
    }

    override fun onResults(results: Bundle) {
        e("VoiceRecognizerFragment","inside onResults : $results")

        val matches =
            results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
        var i = 0
        var voiceSearchResult: String? = ""
        for (s in matches) {
            if (i == 0) {
                voiceSearchResult = s
            }
            i++
        }
//            val output = Intent()
//            output.putExtra(AppConstants.VOICE_DATA_RESULT, voiceSearchResult)
//            getActivity().setResult(-1, output)
//            Handler().postDelayed({ getActivity().finish() }, 200)
        viewModel._voiceText.postValue(SingleEvent(voiceSearchResult?:""))
        requireActivity().onBackPressed()
    }

    override fun onReadyForSpeech(params: Bundle) {
        e("VoiceRecognizerFragment","inside onRecordStart")
    }
    override fun onRmsChanged(v: Float) {}
    override fun onBeginningOfSpeech() {
        e("VoiceRecognizerFragment","inside onBeginningOfSpeech")

    }
    override fun onBufferReceived(buffer: ByteArray) {}
    override fun onEndOfSpeech() {

        e("VoiceRecognizerFragment","inside onBeginningOfSpeech")
    }
    override fun onError(error: Int) {
        e("VoiceRecognizerFragment","inside onError $error")
        onFailure(error)
    }

    private fun onFailure(error: Int) {
        binding.voiceview.stopRecording()
        binding.stateTv.setText(R.string.try_saying_text)
        binding.displayTv.setText(R.string.tap_on_mic)
        binding.displayTv.visibility = View.VISIBLE
    }
    fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Didn't understand, please try again."
        }
    }
    override fun onEvent(eventType: Int, params: Bundle) {}

}