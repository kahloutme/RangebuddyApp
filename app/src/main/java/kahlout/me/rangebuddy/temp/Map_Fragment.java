//package kahlout.me.rangebuddy;
//
//
//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.graphics.Color;
//import android.location.Location;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.Fragment;
//import android.support.v4.content.ContextCompat;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.google.android.gms.maps.model.CircleOptions;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.model.PolylineOptions;
//
//public class Map_Fragment extends Fragment implements OnMapReadyCallback {
//
//    SupportMapFragment mapFragment;
//    private GoogleMap mMap;
//    private boolean mLocationPermissionGranted;
//
//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//
//
//        View view = inflater.inflate(R.layout.fragment_map, null, false);
//        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.googleMap);
//
//        if (mapFragment != null) {
//            mapFragment.getMapAsync(this);
//        }
//
//
//        return view;
//
//    }
//
//
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        enableMyLocationIfPermitted();
//        updateLocationUI();
//
//
//    }
//
//
//
//    private void enableMyLocationIfPermitted() {
//        if (ContextCompat.checkSelfPermission(getActivity(),
//                Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(getActivity(),
//                    new String[]{
//
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                            Manifest.permission.ACCESS_FINE_LOCATION
//                    },
//                    1);
//        } else if (mMap != null) {
//            mMap.setMyLocationEnabled(true);
//            mLocationPermissionGranted = true;
//        }
//    }
//
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case 1: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    enableMyLocationIfPermitted();
//                    mLocationPermissionGranted = true;
//                } else {
//                    showDefaultLocation();
//                    mLocationPermissionGranted = false;
//                }
//                return;
//            }
//
//        }
//    }
//
//
//    @SuppressWarnings("MissingPermission")
//    private void updateLocationUI() {
//        if (mMap == null) {
//            return;
//        }
//
//        if (mLocationPermissionGranted) {
//            mMap.setMyLocationEnabled(true);
//            mMap.getUiSettings().setMyLocationButtonEnabled(true);
//            mMap.getUiSettings().setZoomControlsEnabled(true);
//        } else {
//            mMap.setMyLocationEnabled(false);
//            mMap.getUiSettings().setMyLocationButtonEnabled(false);
//        }
//    }
//
//
//    // If things go wrong!
//
//    private void showDefaultLocation() {
//        Toast.makeText(getActivity(), "Location permission not granted, " +
//                        "showing default location",
//                Toast.LENGTH_SHORT).show();
//        LatLng redmond = new LatLng(47.6739881, -122.121512);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(redmond));
//    }
//
//
//
//}
