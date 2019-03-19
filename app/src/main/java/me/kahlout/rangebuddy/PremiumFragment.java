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
    private String mAdRemovalPrice;


    // In-app products.
    static final String ITEM_SKU_ADREMOVAL = "rb_remove_ads";

    private BillingClient mBillingClient;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments

        View view = inflater.inflate(R.layout.fragment_premium, container, false);

        // TODO: 19/03/2019  tinydb reference
        // Establish connection to billing client
        mBillingClient = BillingClient.newBuilder(getContext()).setListener(this).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.

                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                //TODO implement your own retry policy
                Toast.makeText(getContext(), getResources().getString(R.string.billing_connection_failure), Toast.LENGTH_SHORT);
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });


        mBuyButton = view.findViewById(R.id.buyButton);

        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If user clicks the buy button, launch the billing flow for an ad removal purchase
                // Response is handled using onPurchasesUpdated listener
                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                        .setSku(ITEM_SKU_ADREMOVAL)
                        .setType(BillingClient.SkuType.INAPP)
                        .build();
                int responseCode = mBillingClient.launchBillingFlow(getActivity(), flowParams);
            }
        });

        // Query purchases incase a user is connecting from a different device and they've already purchased the app
        queryPurchases();
        queryPrefPurchases();


        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Premium");


    }

    private void queryPrefPurchases() {
//        Boolean adFree = mSharedPreferences.getBoolean(getResources().getString(R.string.pref_remove_ads_key), false);
//        if (adFree) {
//            mBuyButton.setText(getResources().getString(R.string.pref_ad_removal_purchased));
//            mBuyButton.setEnabled(false);
//        }
    }

    private void queryPurchases() {

        //Method not being used for now, but can be used if purchases ever need to be queried in the future
        Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
        if (purchasesResult != null) {
            List<Purchase> purchasesList = purchasesResult.getPurchasesList();
            if (purchasesList == null) {
                return;
            }
            if (!purchasesList.isEmpty()) {
                for (Purchase purchase : purchasesList) {
                    if (purchase.getSku().equals(ITEM_SKU_ADREMOVAL)) {

                        // TODO: 19/03/2019  remove ad's
//                        mSharedPreferences.edit().putBoolean(getResources().getString(R.string.pref_remove_ads_key), true).commit();
//                        setAdFree(true);
//                        mBuyButton.setText(getResources().getString(R.string.pref_ad_removal_purchased));
//                        mBuyButton.setEnabled(false);
                    }
                }
            }
        }

    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getSku().equals(ITEM_SKU_ADREMOVAL)) {
//            mSharedPreferences.edit().putBoolean(getResources().getString(R.string.pref_remove_ads_key), true).commit();
//            setAdFree(true);
//            mBuyButton.setText(getResources().getString(R.string.pref_ad_removal_purchased));
//            mBuyButton.setEnabled(false);
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
//            mSharedPreferences.edit().putBoolean(getResources().getString(R.string.pref_remove_ads_key), true).commit();
//            setAdFree(true);
//            mBuyButton.setText(getResources().getString(R.string.pref_ad_removal_purchased));
//            mBuyButton.setEnabled(false);
        } else {
            Log.d(TAG, "Other code" + responseCode);
            // Handle any other error codes.
        }

    }


}