package in.gitbox.cryptonian;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import java.util.List;

/**
 * Created by sagar on 23-02-2018.
 */

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = LocationUpdatesBroadcastReceiver.class.getSimpleName();

    static final String ACTION_PROCESS_UPDATES =
            "in.gitbox.cryptonian.action" +
                    ".PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
                    LocationResultHelper locationResultHelper = new LocationResultHelper(
                            context, locations);
                    locationResultHelper.saveCoordinates();
                    Log.i(TAG, LocationResultHelper.getSavedLocationResult(context));
                }
            }
        }
    }
}
