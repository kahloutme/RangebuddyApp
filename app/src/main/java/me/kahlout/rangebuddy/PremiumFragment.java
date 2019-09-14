package me.kahlout.rangebuddy;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.util.List;

import me.kahlout.rangebuddy.Libraries.TinyDB;


public class PremiumFragment extends Fragment implements PurchasesUpdatedListener {

    private static final String TAG = "InAppBilling";

    private TinyDB tinydb;
    private Button mBuyButton;

    //Premium User Flag
    private boolean mPremium;

    // In-app products.
    static final String ITEM_SKU_ADREMOVAL = "rb_remove_ads";

    private BillingClient mBillingClient;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_premium, container, false);

        // Create instance of TinyDB
        tinydb = new TinyDB(MainActivity.getActivity());
        mPremium = tinydb.getBoolean("Premium");

        // Button
        mBuyButton = view.findViewById(R.id.buyButton);


        // Establish connection to billing client
        mBillingClient = BillingClient.newBuilder(MainActivity.getActivity()).setListener(this).build();

        // Connect to billing server
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                    queryPurchases();
//                    queryPrefPurchases();

                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                //TODO implement your own retry policy
                //Here we had the IllegalState Exception because of accessing string resource without application context. So we will use the context we defined
                // in the App class

                //Also getContext can throw an exception, the fragment can 'lose' the Activity in case of out of memory(for example slow device or too much apps opened)
                //So we will be using the application context directly from the MainActivity.
                Toast.makeText(getContext(), App.getContext().getString(R.string.billing_connection_failure), Toast.LENGTH_SHORT);
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
//                queryPrefPurchases();
            }
        });


        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //you can set the title for your toolbar here for different fragments different titles
        MainActivity.getActivity().setTitle("Premium");

        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If user clicks the buy button, launch the billing flow for an ad removal purchase
                // Response is handled using onPurchasesUpdated listener
                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                        .setSku(ITEM_SKU_ADREMOVAL)
                        .setType(BillingClient.SkuType.INAPP)
                        .build();
                int responseCode = mBillingClient.launchBillingFlow(MainActivity.getActivity(), flowParams);
            }
        });


    }

    private void queryPrefPurchases() {
        if (mPremium) {
            mBuyButton.setText(getResources().getString(R.string.pref_ad_removal_purchased));
            mBuyButton.setEnabled(false);
        }
    }

    //Used if purchases ever need to be queried in the future
    private void queryPurchases() {
        Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
        if (purchasesResult != null) {
            List<Purchase> purchasesList = purchasesResult.getPurchasesList();
            if (purchasesList == null) {
                return;
            }
            if (!purchasesList.isEmpty()) {
                for (Purchase purchase : purchasesList) {
                    if (purchase.getSku().equals(ITEM_SKU_ADREMOVAL)) {
                        tinydb.putBoolean("Premium", true);
                        mBuyButton.setText(getResources().getString(R.string.pref_ad_removal_purchased));
                        mBuyButton.setEnabled(false);
                    }
                }
            }
        }

    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getSku().equals(ITEM_SKU_ADREMOVAL)) {
            tinydb.putBoolean("Premium", true);
            mBuyButton.setText(getResources().getString(R.string.pref_ad_removal_purchased));
            mBuyButton.setEnabled(false);
        }
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<com.android.billingclient.api.Purchase> purchases) {

        //Handle the responseCode for the purchase
        //If response code is OK,  handle the purchase
        //If user already owns the item, then indicate in the shared prefs that item is owned
        //If cancelled/other code, log the error

        if (responseCode == BillingClient.BillingResponse.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Log.d(TAG, "User Canceled" + responseCode);
        } else if (responseCode == BillingClient.BillingResponse.ITEM_ALREADY_OWNED) {
              tinydb.putBoolean("Premium", true);
            mBuyButton.setText(getResources().getString(R.string.pref_ad_removal_purchased));
            mBuyButton.setEnabled(false);
        } else {
            Log.d(TAG, "Other code" + responseCode);
            // Handle any other error codes.
        }

    }


}