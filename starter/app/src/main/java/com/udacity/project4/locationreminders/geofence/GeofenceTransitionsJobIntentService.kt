package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment.Companion.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.errorMessage
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext


class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        // call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {

        if (intent.action == ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent.hasError()) {
                Log.e("GeoJobIntentService", geofencingEvent.errorCode.toString())
                return
            }
            val geofenceTransition = geofencingEvent.geofenceTransition
            val triggeringGeofences: MutableList<Geofence> = mutableListOf()
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.v("GeoJobIntentService","Geo fence entered")

                if(geofencingEvent.triggeringGeofences.isNotEmpty()){
                    triggeringGeofences.add(geofencingEvent.triggeringGeofences[0])
                    sendNotification(triggeringGeofences)
                }else{
                    Log.v("GeoJobIntentService", "No Geofence trigger found")
                    return
                }
            }
        }

    }

    //TODO: get the request id of the current geofence
    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        val requestId = when{
            triggeringGeofences.isNotEmpty() -> {
                triggeringGeofences[0].requestId
            }
            else -> {
                Log.v("GeoJobIntentService","No Geofence found")
                return
            }
        }
        if(requestId.isNullOrBlank()){
            return
        }

        //Get the local repository instance
        val remindersLocalRepository: ReminderDataSource by inject()
//        Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            val result = remindersLocalRepository.getReminder(requestId)
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                //send a notification to the user with the reminder details
                sendNotification(
                    this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
            }
        }
    }

}