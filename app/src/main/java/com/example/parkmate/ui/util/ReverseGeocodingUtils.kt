package com.example.parkmate.ui.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.example.parkmate.R
import java.util.Locale

/*
Utility for converting GPS coordinates into readable addresses.
*/
object ReverseGeocodingUtils {

    fun reverseGeocode(
        context: Context,
        latitude: Double,
        longitude: Double,
        onResult: (String?) -> Unit
    ) {
        val geocoder = Geocoder(context, Locale.getDefault())

        if (!Geocoder.isPresent()) {
            onResult(null)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(
                latitude,
                longitude,
                1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        onResult(addresses.firstOrNull()?.toReadableLabel(context))
                    }

                    override fun onError(errorMessage: String?) {
                        onResult(null)
                    }
                }
            )
        } else {
            @Suppress("DEPRECATION")
            runCatching {
                geocoder.getFromLocation(latitude, longitude, 1)
            }.onSuccess { addresses ->
                onResult(addresses?.firstOrNull()?.toReadableLabel(context))
            }.onFailure {
                onResult(null)
            }
        }
    }

    /*
    Formats an Address object into a single line string.
    Fallback to localized "Selected place" if no specific address parts are found.
    */
    private fun Address.toReadableLabel(context: Context): String {
        val parts = listOfNotNull(
            thoroughfare,
            subThoroughfare,
            subLocality,
            locality
        ).filter { it.isNotBlank() }

        return when {
            parts.isNotEmpty() -> parts.joinToString(", ")
            !featureName.isNullOrBlank() -> featureName
            !locality.isNullOrBlank() -> locality
            !adminArea.isNullOrBlank() -> adminArea
            else -> context.getString(R.string.selected_place)
        }
    }
}
