package com.breco.android.app.garden.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.breco.android.app.garden.R;
import com.breco.android.app.garden.UrlRequest.HttpRequest;
import com.github.paolorotolo.appintro.AppIntro2;
import com.google.android.material.snackbar.Snackbar;
import com.pkmmte.view.CircularImageView;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by stormset on 2016. 04. 30.
 */

public class Configuration extends AppIntro2 {
    static String urlState;
    static String urlSwitch;
    static String urlSave;
    static String url;
    int[] iconID;
    String[] names;
    Context context;
    String request;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setZoomAnimation();
        addSlide(ConfigSlide.newInstance(R.layout.config, 1));
        addSlide(ConfigSlide.newInstance(R.layout.config, 2));
        addSlide(ConfigSlide.newInstance(R.layout.config, 3));
        addSlide(ConfigSlide.newInstance(R.layout.config, 4));
        showSkipButton(false);
        context = this;
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.backgroundserc);
        imageView.setBackgroundColor(Color.BLACK);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setBackgroundView(imageView);
        names = new String[4];
        iconID = new int[4];
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Configuration.this);
        url = prefs.getString("web", null) + "/?deviceAuth=" + prefs.getString("auth", null);
        urlSwitch = url + "&switchRequest=";
        urlState = url + "&stateRequest=";
        urlSave = url + "&saveRequest=";
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        ViewGroup viewGroup = (ViewGroup) currentFragment.getView();
        EditText editText = viewGroup.findViewById(R.id.editText);
        TextView tv = viewGroup.findViewById(R.id.idHolder);
        MultiStateToggleButton button = viewGroup.findViewById(R.id.mstb_multi_id);
        int id = Integer.parseInt(tv.getText().toString());
        names[id - 1] = editText.getText().toString();
        iconID[id - 1] = button.getValue();
        StringBuilder configString = new StringBuilder();
        for (String name : names) {
            configString.append(name);
            configString.append(";");
        }
        configString.append("|");
        for (int j : iconID) {
            configString.append(j);
            configString.append(";");
        }

        try {
            String url = urlSave + URLEncoder.encode(configString.toString(), "utf-8");

            new HttpRequest.HttpGetRequest(output -> {
                if (output != null) {
                    Intent myIntent = new Intent(Configuration.this, BaseActivity.class);
                    startActivity(myIntent);
                } else {
                    Snackbar.make(currentFragment.getView(), "A mentés sikertelen. Ellenőrizze az internetkapcsolatot!", Snackbar.LENGTH_LONG).show();
                }
            }).execute(url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        if (oldFragment != null && newFragment != null) {
            ViewGroup oldViewGroup = (ViewGroup) oldFragment.getView();
            EditText editText = oldViewGroup.findViewById(R.id.editText);
            TextView tv = oldViewGroup.findViewById(R.id.idHolder);
            MultiStateToggleButton oldButton = oldViewGroup.findViewById(R.id.mstb_multi_id);
            int id = Integer.parseInt(tv.getText().toString());
            names[id - 1] = editText.getText().toString();
            iconID[id - 1] = oldButton.getValue();
            ViewGroup viewGroup = (ViewGroup) newFragment.getView();
            MultiStateToggleButton button = viewGroup.findViewById(R.id.mstb_multi_id);
            TextView newTv = viewGroup.findViewById(R.id.idHolder);
            setImageIcon(viewGroup, button.getValue());
            ImageView imageView = viewGroup.findViewById(R.id.testDevice);
            int newId = Integer.parseInt(newTv.getText().toString());
            refreshColorState(imageView, newId, null);
        }
    }

    public void clickTest(View view, int id) {
        ImageView imageView = (ImageView) view;
        new HttpRequest.HttpGetRequest(output -> {
            if (output != null) {
                refreshColorState(imageView, id, output);
            }
        }).execute(urlSwitch + id);
    }

    public void setIcon(ViewGroup view) {
        MultiStateToggleButton button = view.findViewById(R.id.mstb_multi_id);
        int newVal = button.getValue() + 1;
        if (newVal > 3) newVal = 0;
        setImageIcon(view, newVal);
        button.setValue(newVal);
    }

    public void setImageIcon(ViewGroup view, int position) {
        ImageView image = view.findViewById(R.id.icon);
        CircularImageView itemRound = view.findViewById(R.id.item_round);
        if (position == 0) {
            image.setImageResource(R.drawable.waterpump);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setGradientRadius(180);
            int[] Colors = new int[2];
            Colors[0] = Color.parseColor("#71ac50");
            Colors[1] = Color.parseColor("#5ea71d");
            shape.setColors(Colors);
            itemRound.setBackgroundDrawable(shape);
        }
        if (position == 1) {
            image.setImageResource(R.drawable.light);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setGradientRadius(180);
            int[] Colors = new int[2];
            Colors[0] = Color.parseColor("#F5DA81");
            Colors[1] = Color.parseColor("#F7D358");
            shape.setColors(Colors);
            itemRound.setBackgroundDrawable(shape);
        }
        if (position == 2) {
            image.setImageResource(R.drawable.light);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setGradientRadius(180);
            int[] Colors = new int[2];
            Colors[0] = Color.parseColor("#FE9A2E");
            Colors[1] = Color.parseColor("#FF8000");
            shape.setColors(Colors);
            itemRound.setBackgroundDrawable(shape);
        }
        if (position == 3) {
            image.setImageResource(R.drawable.fountain);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setGradientRadius(180);
            int[] Colors = new int[3];
            Colors[0] = Color.parseColor("#A9BCF5");
            Colors[1] = Color.parseColor("#819FF7");
            Colors[2] = Color.parseColor("#5882FA");
            shape.setColors(Colors);
            itemRound.setBackgroundDrawable(shape);
        }
        if (position == -1) {
            image.setImageResource(R.drawable.ic_add_black_48dp);
        }
    }

    public void refreshColorState(ImageView view, int id, @Nullable String ownRequest) {
        if (ownRequest == null) {
            new HttpRequest.HttpGetRequest(new HttpRequest.HttpGetRequest.AsyncResponse() {
                @Override
                public void processFinish(String output) {
                    if (output != null) {
                        request = output;
                    }
                }
            }).execute(urlState + id);
        } else {
            request = ownRequest;
        }
        if (request != null) {
            String[] req = request.split(";");
            if (Integer.parseInt(req[id - 1]) == 1) {
                ImageView imageView = view;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imageView.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FF88DD88")));
                }
            } else {
                ImageView imageView = view;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imageView.setImageTintList(ColorStateList.valueOf(Color.parseColor("#565656")));
                }
            }
        }
    }
}