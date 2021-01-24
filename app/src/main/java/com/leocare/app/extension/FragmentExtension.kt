package com.leocare.app.extension

import android.util.DisplayMetrics
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.leocare.app.R
import www.sanju.motiontoast.MotionToast

fun Fragment.showToast(title: Int?, message: Int, toastType: String, toastDuration: Long) {
    val titleValue = if (title != null) getString(title) else null

    MotionToast.darkToast(
            requireActivity(),
            titleValue,
            getString(message),
            toastType,
            MotionToast.GRAVITY_BOTTOM,
            toastDuration,
            ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
    )
}

fun Fragment.convertDpToPixel(dp: Float): Float {
    return dp * (requireContext().resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}
