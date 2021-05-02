package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.concurrent.TimeUnit

class SaveReminderFragment : BaseFragment() {


    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
                "LocationReminders.saveReminderFragment.action.ACTION_GEOFENCE_EVENT"
        private const val TAG = "SaveReminderFragment"
        private const val GEOFENCE_RADIUS_IN_METERS = 250f // Geo fence set to 250 metres
        private const val REQUEST_BACKGROUND_PERMISSION_REQUEST_CODE = 30
        val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(24)
    }

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel : SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var reminderDataItem: ReminderDataItem
    private var title: String? = null
    private var desc: String? = null
    private var location: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    private lateinit var geofencingClient: GeofencingClient// A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View{
        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        binding.setLifecycleOwner(this)

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        _viewModel.reminderTitle.observe(viewLifecycleOwner, Observer {
            title = it
        })

        _viewModel.reminderDescription.observe(viewLifecycleOwner, Observer {
            desc = it
        })

        _viewModel.reminderSelectedLocationStr.observe(viewLifecycleOwner, Observer{
            location = it
        })

        _viewModel.latitude.observe(viewLifecycleOwner, Observer{
            latitude = it
        })

        _viewModel.longitude.observe(viewLifecycleOwner, Observer {
            longitude = it
        })


        binding.saveReminder.setOnClickListener {
            reminderDataItem = ReminderDataItem(title, desc, location, latitude, longitude)
            Log.d("ReminderDataItem", reminderDataItem.toString())

            if(_viewModel.validateEnteredData(reminderDataItem))

                checkForBackgroundPermission()
        }
    }

    private fun checkForBackgroundPermission() {
        if (runningQOrLater) {
            if (PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                addGeofence(reminderDataItem)
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    AlertDialog.Builder(requireActivity())
                            .setTitle(getString(R.string.permission_needed))
                            .setMessage(getString(R.string.background_permission_message))
                            .setPositiveButton(getString(R.string.permission_OK), DialogInterface.OnClickListener { dialogInterface, i ->
                                this.requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_BACKGROUND_PERMISSION_REQUEST_CODE)
                            })
                            .setNegativeButton(getString(R.string.permission_cancel), DialogInterface.OnClickListener { dialogInterface, i ->
                                dialogInterface.dismiss()
                            })
                            .create().show()
                } else {
                    this.requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_BACKGROUND_PERMISSION_REQUEST_CODE)
                }
            }
        } else {
            addGeofence(reminderDataItem)
        }
    }


    @SuppressLint("MissingPermission")
    fun addGeofence(reminderDataItem: ReminderDataItem) {
        val geofence = Geofence.Builder()
                .setRequestId(reminderDataItem.id)
                .setCircularRegion(reminderDataItem.latitude!!, reminderDataItem.longitude!!,
                        GEOFENCE_RADIUS_IN_METERS)
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
        val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                Toast.makeText(requireContext(), "Geofence added",
                        Toast.LENGTH_SHORT)
                        .show()
                Log.d(TAG, "Added Geofence,${geofence.requestId}")

                _viewModel.saveReminder(reminderDataItem)
            }
            addOnFailureListener {
                Toast.makeText(requireContext(), R.string.geofences_not_added,
                        Toast.LENGTH_SHORT).show()
                if ((it.message != null)) {
                    Log.d(TAG, it.message!!)
                }
            }
        }
    }


    /*
   * In all cases, we need to have the location permission.  On Android 10+ (Q) we need to have
   * the background permission as well.
   */
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        if (requestCode == REQUEST_BACKGROUND_PERMISSION_REQUEST_CODE) {
            Log.d(TAG, "inside onRequestPermissionResult")
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "inside grantResults")
                Toast.makeText(requireContext(), "Permission granted", Toast.LENGTH_SHORT).show()
                addGeofence(reminderDataItem)
            } else {
                Toast.makeText(requireContext(), "Permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
