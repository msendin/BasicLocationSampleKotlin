package com.example.basiclocationsamplekotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.basiclocationsamplekotlin.databinding.MainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
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
    protected var mLastLocation: Location? = null
    private var mLatitudeLabel: String? = null
    private var mLongitudeLabel: String? = null
    private lateinit var mLatitudeText: TextView
    private lateinit var mLongitudeText: TextView

    private lateinit var binding: MainBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mLatitudeLabel = resources.getString(R.string.latitude_label)
        mLongitudeLabel = resources.getString(R.string.longitude_label)
        mLatitudeText = binding.latitudeText
        mLongitudeText = binding.longitudeText

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    public override fun onStart() {
        super.onStart()
        if (!checkPermissions()) {
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
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            )
                return
            mFusedLocationClient.lastLocation.addOnCompleteListener { task : Task<Location> ->
                if (task.isSuccessful() && task.getResult() != null) {
                    mLastLocation = task.getResult()
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
        val permissionState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val backgroundPermissionState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        return (permissionState == PackageManager.PERMISSION_GRANTED) && (backgroundPermissionState == PackageManager.PERMISSION_GRANTED)
    }

    fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this@MainActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
        ActivityCompat.requestPermissions(
            this@MainActivity, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar(
                R.string.permission_rationale, android.R.string.ok
            ) { // Request permission
                startLocationPermissionRequest()
            }
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest()
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                lastLocation
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(
                    R.string.permission_denied_explanation, R.string.settings
                ) { // Build intent that displays the App settings screen.
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts(
                        "package",
                        BuildConfig.APPLICATION_ID, null
                    )
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }
}


