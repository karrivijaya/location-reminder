package com.udacity.project4.authentication

import android.app.Application
import androidx.lifecycle.map
import com.udacity.project4.base.BaseViewModel

class AuthenticationViewModel(app: Application) : BaseViewModel(app){

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    // Create an authenticationState variable based off the FirebaseUserLiveData object. By
    //  creating this variable, other classes will be able to query for whether the user is logged
    //  in or not
    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }
}

