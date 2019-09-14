package me.kahlout.rangebuddy;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class App extends Application {
    //This is an Application context, not Activity context. This context is never cleared from the memory and can be used anywhere. There are some scenarious when we
    // need application context, for example when using alert dialogos, toasts etc. Now this class is created to use Application context when accessing resources
    public static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("onCreateApp","created");

        mContext = this;
    }

    public static Context getContext(){

        return mContext;
    }
}
