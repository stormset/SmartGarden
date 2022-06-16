package com.breco.android.app.garden.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.breco.android.app.garden.R;
import com.breco.android.app.garden.UrlRequest.HttpRequest;
import com.google.android.material.snackbar.Snackbar;

/**
 * Created by stormset on 2016. 04. 24.
 */

public class MainActivity extends AppCompatActivity {
    private final Handler handler = new Handler();
    RelativeLayout relativeLayout;
    String urlSave = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);
        relativeLayout = findViewById(R.id.welcome);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        handler.postDelayed(runnable, 1000);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isOnline()) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                if (prefs.getBoolean("loggedStatusAUTHORIDD", true)) {
                    Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(myIntent);

                } else {
                    urlSave = prefs.getString("web", null) + "/?deviceAuth=" + prefs.getString("auth", null) + "&checkSavedInstance=";
                    new HttpRequest.HttpGetRequest(new HttpRequest.HttpGetRequest.AsyncResponse() {
                        @Override
                        public void processFinish(String output) {
                            if (output != null) {
                                if (Integer.parseInt(output) == 0) {
                                    Intent intent = new Intent(MainActivity.this, Configuration.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    MainActivity.this.finish();
                                    startActivity(intent);
                                    handler.removeCallbacks(runnable);
                                } else {
                                    Intent myIntent = new Intent(MainActivity.this, BaseActivity.class);
                                    startActivity(myIntent);
                                    handler.removeCallbacks(runnable);
                                }
                            } else {
                                Snackbar.make(relativeLayout, "Ellenőrizze az internetkapcsolatot!", Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }).execute(urlSave);
                    handler.postDelayed(this, 10000);
                }
            } else {
                Snackbar.make(relativeLayout, "Nincs internetkapcsolat!", Snackbar.LENGTH_LONG).show();
                handler.postDelayed(this, 10000);
            }
        }
    };


}