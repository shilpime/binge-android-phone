package com.tatasky.binge.ui.features

import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.tatasky.binge.R
import kotlinx.android.synthetic.main.fragment_under_development.*

/**
 * Created by Srikant Karnani on 17/12/19.
 */
class UnderDevelopmentFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_under_development, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= M) {
            animateDrawable()
        }
    }

    @RequiresApi(M)
    private fun animateDrawable() {
        val animatedDrawable = iv_development.drawable
        if (animatedDrawable is AnimatedVectorDrawable) {
            animatedDrawable.start()
            animatedDrawable.registerAnimationCallback(
                object : Animatable2.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
                        (drawable as AnimatedVectorDrawable).start()
                    }
                })
        }
    }
}