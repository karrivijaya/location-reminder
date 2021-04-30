package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationFragment.Companion.SIGN_IN_REQUEST_CODE
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthenticationFragment: BaseFragment() {

    override val _viewModel by viewModels<AuthenticationViewModel>()
    private lateinit var binding: FragmentAuthenticationBinding
    companion object{

        const val SIGN_IN_REQUEST_CODE = 1000
        const val TAG = "AuthenticationFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_authentication,container,false)

        binding.viewModel = _viewModel

        return binding.root

    }

    private fun observeAuthenticationState() {

        _viewModel.authenticationState.observe(viewLifecycleOwner, Observer {authenticationState ->

            when(authenticationState){
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                  //  _viewModel.navigationCommand.postValue(
                  //      NavigationCommand.To(AuthenticationFragmentDirections.actionAuthenticationFragmentToReminderListFragment())
                  //  )
                    val intent = Intent(requireContext(), RemindersActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
                else -> {
                    binding.btnLogin.setOnClickListener {
                       launchSignInFlow()
                    }

                }

            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeAuthenticationState()

        binding.lifecycleOwner = viewLifecycleOwner

        /*binding.btnLogin.setOnClickListener {
            launchSignInFlow()
        }*/

    }


    // Implementing the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
    private fun launchSignInFlow() {

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()

        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //  Listen to the result of the sign in process by filter for when
        //  SIGN_IN_REQUEST_CODE is passed back. Start by having log statements to know
        //  whether the user has signed in successfully
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                Log.i(TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

}