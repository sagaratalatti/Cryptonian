package in.gitbox.cryptonian;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import java.util.List;

/**
 * Created by sagar on 23-02-2018.
 */

class LocationResultHelper {

    final static String KEY_LOCATION_UPDATES_RESULT = "location-updates-result";

    final private static String PRIMARY_CHANNEL = "default";

    final static String KEY_LOCATION_CO_ORDINATED = "location-co-ordinates";


    private Context mContext;
    private List<Location> mLocations;
    private NotificationManager mNotificationManager;

    LocationResultHelper(Context context, List<Location> locations) {
        mContext = context;
        mLocations = locations;
    }

    private Double getLatitude(){
        if (mLocations.isEmpty()){
            return 22.572646;
        }
        double latitude = 22.572646;
        for (Location location : mLocations){
            latitude = location.getLatitude();
        }
        return latitude;
    }

    private Double getLongitude(){
        if (mLocations.isEmpty()){
            return 88.363895;
        }
        double longitude = 88.363895;
        for (Location location : mLocations){
            longitude = location.getLongitude();
        }
        return longitude;
    }

    /**
     * Saves location result as a string to {@link android.content.SharedPreferences}.
     */

    void saveCoordinates(){
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putLong(Constants.LatitudePref, Double.doubleToLongBits(getLatitude()))
                .putLong(Constants.LongitudePref, Double.doubleToLongBits(getLongitude()))
                .apply();
    }

    /**
     * Fetches location results from {@link android.content.SharedPreferences}.
     */
    static String getSavedLocationResult(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, "");
    }

    static double getLatitude(final SharedPreferences prefs) {
        if ( !prefs.contains(Constants.LatitudePref))
            return 22.572646;
        return Double.longBitsToDouble(prefs.getLong(Constants.LatitudePref, 0));
    }

    static double getLongitude(final SharedPreferences prefs) {
        if ( !prefs.contains(Constants.LatitudePref))
            return 88.363895;
        return Double.longBitsToDouble(prefs.getLong(Constants.LongitudePref, 0));
    }
    /**
     * Get the notification mNotificationManager.
     * <p>
     * Utility method as this helper works with it a lot.
     *
     * @return The system service NotificationManager
     */
    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(
                    Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }
}
