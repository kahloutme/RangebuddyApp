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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
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
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

import me.kahlout.rangebuddy.Libraries.DistanceMath;
import me.kahlout.rangebuddy.Libraries.TinyDB;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    // location variables

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    Location mLastLocation;
    public static double current_lat=0, current_lon=0,clicked_lat=0,clicked_lon=0;
    GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000; // = 5 seconds
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

    // Do we have location?
    boolean mLocationFirstUpdate = false;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {



        View view = inflater.inflate(R.layout.fragment_map, null, false);
        mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.googleMap);
        mapFrag.getMapAsync(this);

        /// Screen elements
        mClearButton = view.findViewById(R.id.Clear_Button);
        mTextView = view.findViewById(R.id.Distance_Text);

        ///Ad Testing
        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //you can set the title for your toolbar here for different fragments different titles
        MainActivity.getActivity().setTitle("RangeBuddy");

        // Create instance of TinyDB
        tinydb = new TinyDB(MainActivity.getActivity());


        // Create Marker icon
        int height = 100;
        int width = 100;
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.mipmap.marker_icon);
        Bitmap b = bitmapdraw.getBitmap();
        MarkerIcon = Bitmap.createScaledBitmap(b, width, height, false);


        // Hide Ads if premium
        mPremium = tinydb.getBoolean("Premium");
        if (mPremium) {
            if (mAdView != null) {
                mAdView.destroy();
                mAdView.setVisibility(View.GONE);
            }
        }

        // TODO: 19/03/2019 Add a Change log record


    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //This is google maps api connection callback,here we will have current location

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (mLastLocation != null) {
            current_lat = mLastLocation.getLatitude();
            current_lon = mLastLocation.getLongitude();
        }
        Log.d("onConnected","connected " + current_lat + "  " + current_lon);

        startLocationUpdates();
    }
    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


    //Here we setup location updates request
    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        //We will set HIGHT ACCURANCY priority and request every location update
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        LocationSettings();



        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged",""+location);
        if (location != null) {

            current_lat = location.getLatitude();
            current_lon = location.getLongitude();
            LocationUpdate();
        }
    }
    public boolean LocationSettings() {
        mSettings = false;
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(MainActivity.getActivity()).checkLocationSettings(builder.build());

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
                            Toast.makeText(MainActivity.getActivity(), "Resolution required!", Toast.LENGTH_LONG).show();

                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                // Note: Comes back to MainActivity Results first.
                                resolvable.startResolutionForResult(MainActivity.getActivity(), 999);
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
        Log.d("onMapReady","ready");
        // Check location settings
        //Here we will use Ted permissions library, so can rid of a lot of code such a onRequestPermissionsResult callback

            PermissionListener permissionListener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {

                    mGoogleMap.setMyLocationEnabled(true);
                    // we build google api client
                    googleApiClient = new GoogleApiClient.Builder(MainActivity.getActivity()).
                            addApi(LocationServices.API).
                            addConnectionCallbacks(MapFragment.this).
                            addOnConnectionFailedListener(MapFragment.this).build();
                    if (googleApiClient != null) {
                        googleApiClient.connect();
                    }
                    Log.d("onMapReady","has permission");

                }

                @Override
                public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getActivity());
                    //Source of the data in the Dialog

                    // Set the dialog title
                    builder.setTitle("Location Error")
                            .setMessage("Cannot continue without Location Enabled")
                            // Set the action buttons
                            .setNeutralButton("Close App", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    MainActivity.getActivity().finish();
                                    System.exit(0);
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            };
            TedPermission.with(MainActivity.getActivity())
                    .setPermissionListener(permissionListener)
                    .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

                    .check();




        /// We are now actually ready at this point

        mClearButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Clear clicked coordinates
                clicked_lat = 0;
                clicked_lon = 0;
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
                //we assign value to variables, so we could use them again in some other place, for example in location update
                clicked_lat = clickCoords.latitude;
                clicked_lon = clickCoords.longitude;

                mline = mGoogleMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(clicked_lat, clicked_lon), new LatLng(current_lat, current_lon))
                        .width(12)
                        .color(Color.RED));
                mEndMarker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(clicked_lat, clicked_lon))
                        .icon(BitmapDescriptorFactory.fromBitmap(MarkerIcon))
                );
                calc_distance();

                Log.i("MapsActivity", "Polyline added at " + clickCoords.latitude + " " + clickCoords.longitude);
            }
        });

    }
    public void calc_distance()
    {
        mUnits = tinydb.getIntUnits("UnitsToUse");

        if (mUnits == 0) {
            double calculatedDistance = DistanceMath.distanceYards(clicked_lat, current_lat, clicked_lon, current_lon, 0, 0);
            int DisplayDistance = (int) calculatedDistance;
            mTextView = (TextView) getView().findViewById(R.id.Distance_Text);
            mTextView.setText(DisplayDistance + "yd");
        } else {
            double calculatedDistance = DistanceMath.distanceMeters(clicked_lat, current_lat, clicked_lon, current_lon, 0, 0);
            int DisplayDistance = (int) calculatedDistance;
            mTextView = (TextView) getView().findViewById(R.id.Distance_Text);
            mTextView.setText(DisplayDistance + "m");
        }
    }


    public void LocationUpdate() {
        mGoogleMap.setMinZoomPreference(17f);
        LatLng latLng = new LatLng(current_lat, current_lon);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));
        if(clicked_lat!=0 && clicked_lon!=0) {
            //Now when the person is moving and updating his current location, the distance will be recalculated
            // We will clear the poliline and redraw the line again, So the line will be following current position
            if (mline != null) {
                mline.remove();
                mline = null;

                Log.i("MapsActivity", "This will clear the line");
            }
            mline = mGoogleMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(clicked_lat, clicked_lon), new LatLng(current_lat, current_lon))
                    .width(12)
                    .color(Color.RED));

            calc_distance();
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Don't know why defining this LocationSettingsStates,it's never used and causing the exception
       // final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);

        switch (requestCode) {
            case 999:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made

                        break;
                    case Activity.RESULT_CANCELED:
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getActivity());
                        //Source of the data in the Dialog

                        // Set the dialog title
                        builder.setTitle("Location Error")
                                .setMessage("Cannot continue without Location Services Enabled")
                                // Set the action buttons
                                .setNeutralButton("Close App", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        MainActivity.getActivity().finish();
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
