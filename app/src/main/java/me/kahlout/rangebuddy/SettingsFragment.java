package me.kahlout.rangebuddy;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import me.kahlout.rangebuddy.Libraries.TinyDB;


public class SettingsFragment extends Fragment {


    private TinyDB tinydb;
    private ListView mListView;
    private int mUnits;
    private boolean mZoom = false;
    private int mZoomDefault;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Create instance of TinyDB
//        tinydb = new TinyDB(getContext());
        tinydb = new TinyDB(MainActivity.getActivity());

        mListView = (ListView) view.findViewById(R.id.SettingListView);
        String[] myKeys = getResources().getStringArray(R.array.listviewSettings);
        mListView.setAdapter(new ArrayAdapter<String>(MainActivity.getActivity(), android.R.layout.simple_list_item_1, myKeys));


        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //you can set the title for your toolbar here for different fragments different titles
        MainActivity.getActivity().setTitle("Settings");

        mUnits = tinydb.getIntUnits("UnitsToUse");
        mZoom = tinydb.getBoolean("ZoomLock");
        mZoomDefault = tinydb.getInt("ZoomDefault");

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            String twitter_user_name = "Kahlout";

            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                switch (position) {

                    case 0: /// Units for Distance


                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getActivity());
                        //Source of the data in the DIalog
                        final CharSequence[] array = {"Yards", "Meters"};

                        // Set the dialog title
                        builder.setTitle("Select Units")
                                // Specify the list array, the items to be selected by default (null for none),
                                // and the listener through which to receive callbacks when items are selected
                                .setSingleChoiceItems(array, mUnits, new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {


                                        mUnits = which;
                                    }
                                })

                                // Set the action buttons
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        tinydb.putInt("UnitsToUse", mUnits);

                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                });

                        AlertDialog alert = builder.create();
                        alert.show();

                        break;


                    case 1: //  Zoom Locked

                        AlertDialog.Builder builderZoom = new AlertDialog.Builder(MainActivity.getActivity());
                        //Source of the data in the Dialog
                        final CharSequence[] arrayZoom = {"Locked", "Unlocked"};

                        // Set the dialog title
                        builderZoom.setTitle("Set Zoom Lock")
                                // Specify the list array, the items to be selected by default (null for none),
                                // and the listener through which to receive callbacks when items are selected
                                .setSingleChoiceItems(arrayZoom, mZoomDefault, new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        mZoom = false;

                                        if (which == 1) {

                                            mZoom = true;
                                        }

                                        mZoomDefault = which;

                                    }
                                })

                                // Set the action buttons
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {


                                        tinydb.putBoolean("ZoomLock", mZoom);
                                        tinydb.putInt("ZoomDefault", mZoomDefault);

                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                });

                        AlertDialog alertZoom = builderZoom.create();
                        alertZoom.show();

                        break;

                    case 2: // Email Me!
                        Intent intent = new Intent(Intent.ACTION_SENDTO); // it's not ACTION_SEND
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "RangeBuddy Email: Feature / Bug");
                        intent.putExtra(Intent.EXTRA_TEXT, ".......");
                        intent.setData(Uri.parse("mailto:apps@greenpenguin.ie")); // or just "mailto:" for blank
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this will make such that when user returns to your app, your app is displayed, instead of the email app.
                        startActivity(intent);
                        break;

                    case 3: // Tweet me!
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + twitter_user_name)));
                        } catch (Exception e) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/" + twitter_user_name)));
                        }
                        break;


                }


            }
        });


    }


}