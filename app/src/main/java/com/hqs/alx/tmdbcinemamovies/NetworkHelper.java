package com.hqs.alx.tmdbcinemamovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by Alex on 27/11/2017.
 */
        /*
        this class is checking if there is network connection or not
        */

public class NetworkHelper {

    private static final String TAG = NetworkHelper.class.getSimpleName();

    public static boolean isInternetAvailable(Context context)
    {
        NetworkInfo info = (NetworkInfo) ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (info == null)
        {
            Log.d(TAG,"No Network connection");
            return false;
        }
        else
        {
            if(info.isConnected())
            {
                Log.d(TAG," Network connection available...");
                return true;
            }
            else
            {
                Log.d(TAG,"no Network connection");
                return false;
            }

        }
    }
}
