package me.kahlout.rangebuddy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
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

import java.util.List;
import java.util.Objects;

import me.kahlout.rangebuddy.Libraries.DistanceMath;
import me.kahlout.rangebuddy.Libraries.TinyDB;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    // location variables

    private GoogleMap mGoogleMap;
    private SupportMapFragment mapFrag;
    private  LocationRequest mLocationRequest;
    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;


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

    boolean permissionGranted = false;
    boolean locationActive = false;


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
        tinydb = new TinyDB(getContext());

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); // 5 second interval for highest accuracy
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

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

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);



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

                    try {

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
                        mTextView.setText(DisplayDistance + "yd");


                    } else {

                        double calculatedDistance = DistanceMath.distanceMeters(mLastLocation.getLatitude(), clickCoords.latitude, mLastLocation.getLongitude(), clickCoords.longitude, 0, 0);
                        int DisplayDistance = (int) calculatedDistance;

                        mTextView = (TextView) getView().findViewById(R.id.Distance_Text);
                        mTextView.setText(DisplayDistance + "m");


                    }

                } catch (Exception e) {
                    // This will catch any exception, because they are all descended from Exception
                    Log.i("MapsActivity", "EXCEPTION CAUGHT!");
                    showError();
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

                if (!mLocationFirstUpdate) {
                    FirstLocationUpdate();
                    mLocationFirstUpdate = true;
                }

                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
            }
        }
    };

    public void FirstLocationUpdate() {

        mGoogleMap.setMinZoomPreference(17f);
        double currentLatitude = mLastLocation.getLatitude();
        double currentLongitude = mLastLocation.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));

    }

    public void showError(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        //Source of the data in the Dialog

        // Set the dialog title
        builder.setTitle("Location Error")
                .setCancelable(false)
                .setMessage("Cannot continue without Location Services Enabled")
                // Set the action buttons
                .setNeutralButton("Fix Permissions", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        int mapid = R.id.nav_perm;
                        ((MainActivity) Objects.requireNonNull(getActivity())).displaySelectedScreen(mapid);
                    }
                });

        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }


}
