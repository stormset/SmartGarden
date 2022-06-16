package com.breco.android.app.garden.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.breco.android.app.garden.R;
import com.breco.android.app.garden.UrlRequest.HttpRequest;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.validator.routines.UrlValidator;


/**
 * Created by stormset on 2016. 04. 25.
 */

public class LoginActivity extends AppCompatActivity {
    SharedPreferences prefs;
    EditText web;
    EditText auth;
    RelativeLayout relativeLayout;

    public static boolean isParsable(String input) {
        boolean parsable = true;
        try {
            Integer.parseInt(input);
        } catch (ParseException e) {
            parsable = false;
        } catch (NumberFormatException e) {
            parsable = false;
        }
        return parsable;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        Button button = findViewById(R.id.login);
        web = findViewById(R.id.web);
        auth = findViewById(R.id.auth);
        relativeLayout = findViewById(R.id.mainView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!isOnline()) {
            Snackbar.make(relativeLayout, "Ellenőrizze az internetkapcsolatot!", Snackbar.LENGTH_LONG).show();
        }
    }

    public void save() {
        if (web.getText().toString().length() > 0 && auth.getText().toString().length() > 0) {
            UrlValidator urlValidator = new UrlValidator();
            if (urlValidator.isValid(web.getText().toString())) {
                if (isParsable(auth.getText().toString())) {
                    String url = web.getText().toString() + "/?deviceAuth=" + auth.getText().toString();
                    new HttpRequest.HttpGetRequest(new HttpRequest.HttpGetRequest.AsyncResponse() {
                        @Override
                        public void processFinish(String output) {
                            if (url != null && output != null && isParsable(output)) {
                                if (Integer.parseInt(output) == Integer.parseInt(auth.getText().toString())) {
                                    prefs.edit().putString("web", web.getText().toString()).apply();
                                    prefs.edit().putString("auth", auth.getText().toString()).apply();
                                    prefs.edit().putBoolean("loggedStatusAUTHORIDD", false).apply();
                                    Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(myIntent);
                                }
                            } else if (!isParsable(output)) {
                                Snackbar.make(relativeLayout, "Helytelen azonosító!", Snackbar.LENGTH_LONG).show();
                            } else {
                                Snackbar.make(relativeLayout, "Ellenőrizze az internetkapcsolatot!", Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }).execute(web.getText().toString() + "/?deviceAuth=" + auth.getText().toString());
                } else {
                    Snackbar.make(relativeLayout, "Az azonosító csak számokat tartalmazhat!", Snackbar.LENGTH_LONG).show();
                }
            } else if (!urlValidator.isValid(web.getText().toString())) {
                Snackbar.make(relativeLayout, "Helytelen webcím!", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(relativeLayout, "Ellenőrizze a megadott adatokat!", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
