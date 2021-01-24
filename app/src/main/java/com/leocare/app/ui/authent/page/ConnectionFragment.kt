package com.leocare.app.ui.authent.page

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.foursquare.android.nativeoauth.FoursquareOAuth
import com.foursquare.android.nativeoauth.model.AccessTokenResponse
import com.leocare.app.R
import com.leocare.app.databinding.FragmentConnectionBinding
import com.leocare.app.extension.showToast
import com.leocare.app.ui.main.LeomapActivity
import www.sanju.motiontoast.MotionToast

/**
 * This fragment is in charge of foursquare connection
 */
class ConnectionFragment : Fragment(R.layout.fragment_connection) {

    private val mFoursquareAuthentLauncher = createFoursquareConnectionLauncher()

    private val mFoursquareTokenRetrieverLauncher = createFoursquareRetrieveTokenLauncher()

    private val mBinding: FragmentConnectionBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.connectionLoginButton.setOnClickListener { startFoursquareConnection() }
        mBinding.connectionWhoIsFoursquareButton.setOnClickListener {
            startActivity(
                    Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(getString(R.string.foursquare_wikipedia_link))
                    )
            )
        }
    }

    /**
     * This method is in charge of creating the connection intent thanks to [FoursquareOAuth.getTokenFromResult]
     * and to launch via a [ActivityResultLauncher]. The aim is to retrieve user token in order to
     * request foursquare api later
     *
     * @see createFoursquareConnectionLauncher
     */
    private fun startFoursquareConnection() {
        val intent = FoursquareOAuth.getConnectIntent(requireActivity(), getString(R.string.foursquare_client_id))

        // If the device does not have the Foursquare app installed, we'd get an intent back that
        // would open the Play Store for download. Otherwise we start the auth flow.
        if (FoursquareOAuth.isPlayStoreIntent(intent)) {
            showToast(
                    null,
                    R.string.no_foursquare_application_detected_toast_mesage,
                    MotionToast.TOAST_INFO,
                    MotionToast.SHORT_DURATION
            )
            startActivity(intent)
        } else {
            mFoursquareAuthentLauncher.launch(intent)
        }
    }

    /**
     * This method is in charge of returning an [ActivityResultLauncher] for the foursquare
     * connection intent used in [startFoursquareConnection]. The next step is to retrieve access
     * token before going to the map except when we failed to get the user code. When it is the case,
     * an error toast is shown.
     *
     * @see startFoursquareConnection
     * @see goToMapWithFoursquareToken
     */
    private fun createFoursquareConnectionLauncher(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val authCodeResponse = FoursquareOAuth.getAuthCodeFromResult(it.resultCode, it.data)
            if (Activity.RESULT_OK == it.resultCode
                    && authCodeResponse != null
                    && authCodeResponse.exception == null) {
                requestTokenAccordingUser(authCodeResponse.code)
            } else {
                showToast(
                        R.string.problem_occured_toast_title,
                        R.string.foursquare_connection_failed_toast_message,
                        MotionToast.TOAST_ERROR,
                        MotionToast.LONG_DURATION
                )
            }
        }
    }

    /**
     * This method is in charge of creating the token exchange intent [FoursquareOAuth.getTokenFromResult]
     * and to launch it via [mFoursquareTokenRetrieverLauncher]
     *
     * @see createFoursquareRetrieveTokenLauncher
     */
    private fun requestTokenAccordingUser(code: String) {
        val intent = FoursquareOAuth.getTokenExchangeIntent(
                requireContext(),
                getString(R.string.foursquare_client_id),
                getString(R.string.foursquare_client_secret),
                code
        )

        mFoursquareTokenRetrieverLauncher.launch(intent)
    }

    /**
     * This method is in charge of returning an [ActivityResultLauncher] for the foursquare
     * token exchange intent used in [requestTokenAccordingUser]. According to activity result,
     * application goes to the map view or warns the user that the connection failed by
     * showing an error toast.
     *
     * @see startFoursquareConnection
     * @see goToMapWithFoursquareToken
     */
    private fun createFoursquareRetrieveTokenLauncher(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val tokenResponse = FoursquareOAuth.getTokenFromResult(it.resultCode, it.data)

            if (Activity.RESULT_OK == it.resultCode
                    && tokenResponse != null
                    && tokenResponse.exception == null
            ) {
                goToMapWithFoursquareToken(tokenResponse.accessToken)
            } else {
                showToast(
                        R.string.problem_occured_toast_title,
                        R.string.foursquare_connection_failed_toast_message,
                        MotionToast.TOAST_ERROR,
                        MotionToast.LONG_DURATION
                )
            }
        }
    }

    /**
     * This method is in charge of the application behavior when the foursquare connection succeeded.
     * It retrieves the [AccessTokenResponse] thanks to [FoursquareOAuth.getTokenFromResult] and
     * the activity's result stored in [ActivityResult] and then gives it as extra to the new activity
     * [LeomapActivity]
     *
     * @param token is the user token give by Foursquare
     */
    private fun goToMapWithFoursquareToken(token: String) {
        // Initialize and setup intent
        val intent = Intent(context, LeomapActivity::class.java)
        // We do not want to back on activity connection
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(LeomapActivity.ACCESS_TOKEN_BUNDLE_KEY, token)

        startActivity(intent)
    }
}