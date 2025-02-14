package com.example.basiclocationsamplekotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.basiclocationsamplekotlin.databinding.MainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import java.util.*


/**
 * Location sample.
 *
 *
 * Demonstrates use of the Location API to retrieve the last known location for a device.
 */
class MainActivity : AppCompatActivity() {
    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    /**
     * Represents a geographical location.
     */
    
    private var mLatitudeLabel: String? = null
    private var mLongitudeLabel: String? = null
    private lateinit var mLatitudeText: TextView
    private lateinit var mLongitudeText: TextView

    private lateinit var binding: MainBinding
    private var settings = false


    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mLatitudeLabel = resources.getString(R.string.latitude_label)
        mLongitudeLabel = resources.getString(R.string.longitude_label)
        mLatitudeText = binding.latitudeText
        mLongitudeText = binding.longitudeText

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { permission ->
            when {
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Only approximate location access granted.
                    Log.i(TAG, getString(R.string.user_agreed))
                    lastLocation
                } else -> {
                // No location access granted.
                showSnackbar(
                    R.string.permission_denied_explanation,
                    R.string.settings
                ) { // Build intent that displays the App settings screen.
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts(
                        getString(R.string.paquete),
                        BuildConfig.APPLICATION_ID, null
                    )
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        if (!checkPermissions() && !settings) {
            requestPermissions()
        } else {
            lastLocation
        }
    }

    /**
     * Provides a simple way of getting a device's location and is well suited for
     * applications that do not require a fine-grained location and that do not need location
     * updates. Gets the best and most recent location currently available, which may be null
     * in rare cases when a location is not available.
     *
     *
     * Note: this method should be called after location permission has been granted.
     */
    private val lastLocation: Unit
        get() {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
              ) return

            mFusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        // Got last known location. In some rare situations this can be null.
                mLatitudeText.text = String.format(
                    Locale.ENGLISH, "%s: %f",
                    mLatitudeLabel,
                    location?.latitude
                )
                mLongitudeText.text = String.format(
                    Locale.ENGLISH, "%s: %f",
                    mLongitudeLabel,
                    location?.longitude)
                    }
                .addOnFailureListener {
                        showSnackbar(getString(R.string.failed))
                }
/*
            mFusedLocationClient.lastLocation.addOnCompleteListener { task: Task<Location> ->
                if (task.isSuccessful() && task.getResult() != null) {
                    val mLastLocation = task.getResult()
                    mLatitudeText.text = String.format(
                        Locale.ENGLISH, "%s: %f",
                        mLatitudeLabel,
                        mLastLocation?.latitude
                    )
                    mLongitudeText.text = String.format(
                        Locale.ENGLISH, "%s: %f",
                        mLongitudeLabel,
                        mLastLocation?.longitude
                    )
                } else {
                    Log.w(TAG, "getLastLocation:exception", task.getException())
                    showSnackbar(getString(R.string.no_location_detected))
                }
            }
*/
        }


    /**
     * Shows a [Snackbar] using `text`.
     *
     * @param text The Snackbar text.
     */
    fun showSnackbar(text: String) {
        val container = findViewById<View>(R.id.main_activity_container)
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show()
        }
    }

    /**
     * Shows a [Snackbar].
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    fun showSnackbar(
        mainTextStringId: Int, actionStringId: Int,
        listener: View.OnClickListener
    ) {
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(mainTextStringId),
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(getString(actionStringId), listener).show()
    }

    /**
     * Return the current state of the permissions needed.
     */
    fun checkPermissions(): Boolean {
        val coarsePermissionState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return (coarsePermissionState == PackageManager.PERMISSION_GRANTED)
    }

    fun startLocationPermissionRequest() {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, getString(R.string.displaying))
            showSnackbar(
                R.string.permission_rationale, android.R.string.ok
            ) { // Request permission
                startLocationPermissionRequest()
            }
            settings = true
        } else {
            Log.i(TAG, getString(R.string.requesting))
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest()
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}


