package com.example.yousafsaleem.buslocation;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yousafsaleem.buslocation.Constants.Constant;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult>, LocationListener {


    LatLng current = null;
    ConnectionResult s;
    Boolean netStatus = false;
    Location mLastLocation = null;
    LocationSettingsRequest.Builder builder;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    SharedPreferences prefs;
    Button button;
    TextView textView;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //----For Full Screen----
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);


        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        textView = (TextView) findViewById(R.id.tv);
        button = (Button) findViewById(R.id.button);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationRequest(MainActivity.this);
                startLocationUpdates();

                LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                if (statusOfGPS) {

                    if (checkConnection(MainActivity.this)) {

                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                            if (mLastLocation != null) {
                                textView.setText("Latitude:- " + mLastLocation.getLatitude() + "  " + "Longitude:- " + mLastLocation.getLongitude() + "\n");
                                startLocationUpdates();
                            }


                        }
                        else{
                            Toast.makeText(MainActivity.this, "Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(gpsOptionsIntent);
                        Toast.makeText(MainActivity.this, "Enable your GPS", Toast.LENGTH_SHORT).show();
                    }





            }
        });


        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


    }




    @Override
    public void onConnected(Bundle ConnectionHint) {


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        s = connectionResult;
        netStatus = false;
    }


    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Toast.makeText(MainActivity.this, "Resolution Required", Toast.LENGTH_SHORT).show();
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Toast.makeText(MainActivity.this, "Failed Fetching Location", Toast.LENGTH_SHORT).show();
                // Error, cannot retrieve location updates.
                break;
        }
    }


    public void locationRequest(Context context) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constant.INTERVAL);
        mLocationRequest.setFastestInterval(Constant.FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        builder.build()
                );

        result.setResultCallback((ResultCallback<? super LocationSettingsResult>) context);
    }


    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onLocationChanged(final Location location) {

        textView.append("Latitude:- "+location.getLatitude() + "  " + "Longitude:- "+location.getLongitude()+"\n");


        String url = "http://demo.lbspak.com/BTS/yousaf/updateDeviceLocation.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST,url,new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                Log.e("LPC",response.toString());
                if (response.equals("1")){

                }
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){public Map<String, String> getParams() {
            Map<String, String> params = new HashMap<>();
            params.put("name", prefs.getString("name",null).toString());
            params.put("pass", prefs.getString("pass",null).toString());
            params.put("location",location.getLatitude()+","+location.getLongitude());
            params.put("udate",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime()).toString());
            return params;
        }};
        queue.add(stringRequest);



        BusAsync busAsync = new BusAsync(this);
        busAsync.doInBackground(location.getLatitude()+","+location.getLongitude());



    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
      //  Toast.makeText(MainActivity.this, "Location Update Started", Toast.LENGTH_SHORT).show();
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkConnection(Context context){
        if (!isOnline(context)){
            showDialog(context);
            return false;
        }
        else{
            return true;
        }
    }
    public void showDialog(final Context context)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Internet Error");
        builder.setIcon(R.drawable.siren);
        builder.setMessage("Internet Connection Error Occured in this case we can not provide our Services")
                .setCancelable(false)
                .setPositiveButton("Connect to WIFI", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("Check Again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(isOnline(context)){
                            dialog.cancel();
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Not Connected Yet", Toast.LENGTH_SHORT).show();
                            checkConnection(context);
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }






}
