package me.kahlout.rangebuddy;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import me.kahlout.rangebuddy.Libraries.DistanceMath;
import me.kahlout.rangebuddy.Libraries.TinyDB;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    // location variables

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    FusedLocationProviderClient mFusedLocationClient;

    // Settings variables
    public boolean mSettings;

    ////Ad
    private AdView mAdView;

    // Declare button and Distance Text
    private Button mClearButton;
    private TextView mTextView;

    // Icon
    private Bitmap MarkerIcon;

    // Memory variable for line
    private Polyline mline;

    // Markers
    private Marker mEndMarker;

    // TinyDB
    private TinyDB tinydb;

    // Units of Measure from TinyDB
    private int mUnits;

    //Premium User Flag
    private boolean mPremium;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        View view = inflater.inflate(R.layout.fragment_map, null, false);
        mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.googleMap);
        mapFrag.getMapAsync(this);

        /// Screen elements
        mClearButton = view.findViewById(R.id.Clear_Button);
        mTextView = view.findViewById(R.id.Distance_Text);

        ///Ad Testing
        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Create instance of TinyDB
        tinydb = new TinyDB(getContext());


        // Create Marker icon
        int height = 100;
        int width  = 100;
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.mipmap.marker_icon);
        Bitmap b = bitmapdraw.getBitmap();
        MarkerIcon = Bitmap.createScaledBitmap(b, width, height, false);


        // Hide Ads if premium
        mPremium = tinydb.getBoolean("Premium");
        if(mPremium){
            if(mAdView != null){
                mAdView.destroy();
                mAdView.setVisibility(View.GONE);
            }
        }

        // TODO: 19/03/2019 Add a Change log record


    }

    public boolean LocationSettings() {
        mSettings = false;
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(getActivity()).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied.
                    mSettings = true;


                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            Toast.makeText(getActivity(), "Resolution required!", Toast.LENGTH_LONG).show();

                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                // Note: Comes back to MainActivity Results first.
                                resolvable.startResolutionForResult(getActivity(), 999);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            mSettings = false;

                            break;
                    }
                }
            }
        });

        return mSettings;

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); // 5 second interval for highest accuracy
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        /// Check location settings
        LocationSettings();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();

            }
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
        }


        /// We are now actually ready at this point

            /// Set minimum zoom to stop map jumping away
            // TODO: 19/03/2019 Causing issue with map not loading until clicked. Removed.
//            mGoogleMap.setMinZoomPreference(17f);

            /**
             *  Listener for clear polyline code
             * */
            mClearButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (mline != null) {
                        mline.remove();
                        mEndMarker.remove();
                        mline = null;
                        mTextView.setText("0");

                        Log.i("MapsActivity", "This will clear the line");
                    }
                }
            });


            mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                @Override
                public void onMapClick(LatLng clickCoords) {

                    mClearButton.performClick();

                    mline = mGoogleMap.addPolyline(new PolylineOptions()
                            .add(new LatLng(clickCoords.latitude, clickCoords.longitude), new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                            .width(12)
                            .color(Color.RED));

                    String S = "String";

                    mEndMarker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(clickCoords.latitude, clickCoords.longitude))
                            .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon))

                    );


                    Log.i("MapsActivity", "Polyline added at " + clickCoords.latitude + " " + clickCoords.longitude);

                    mUnits = tinydb.getIntUnits("UnitsToUse");

                    if (mUnits == 0) {

                        double calculatedDistance = DistanceMath.distanceYards(mLastLocation.getLatitude(), clickCoords.latitude, mLastLocation.getLongitude(), clickCoords.longitude, 0, 0);
                        int DisplayDistance = (int) calculatedDistance;

                        mTextView = (TextView) getView().findViewById(R.id.Distance_Text);
                        mTextView.setText(DisplayDistance + "y");


                    } else {

                        double calculatedDistance = DistanceMath.distanceMeters(mLastLocation.getLatitude(), clickCoords.latitude, mLastLocation.getLongitude(), clickCoords.longitude, 0, 0);
                        int DisplayDistance = (int) calculatedDistance;

                        mTextView = (TextView) getView().findViewById(R.id.Distance_Text);
                        mTextView.setText(DisplayDistance + "m");


                    }
                }
            });

    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                mLastLocation = location;

                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
            }
        }
    };

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mGoogleMap.setMyLocationEnabled(true);
                        Toast.makeText(getActivity(), "permission Granted", Toast.LENGTH_LONG).show();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    //Source of the data in the Dialog

                    // Set the dialog title
                    builder.setTitle("Location Error")
                            .setMessage("Cannot continue without Location Enabled")
                            // Set the action buttons
                            .setNeutralButton("Close App", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    getActivity().finish();
                                    System.exit(0);
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
                return;
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case 999:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made

                        break;
                    case Activity.RESULT_CANCELED:
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        //Source of the data in the Dialog

                        // Set the dialog title
                        builder.setTitle("Location Error")
                                .setMessage("Cannot continue without Location Services Enabled")
                                // Set the action buttons
                                .setNeutralButton("Close App", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        getActivity().finish();
                                        System.exit(0);
                                    }
                                });

                        AlertDialog alert = builder.create();
                        alert.show();


                        break;
                    default:
                        break;
                }
                break;
        }
    }


}
