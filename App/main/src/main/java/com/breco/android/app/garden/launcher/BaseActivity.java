package com.breco.android.app.garden.launcher;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.breco.android.app.garden.BubbleCloudView.BubbleCloudView;
import com.breco.android.app.garden.BubbleCloudView.FileManagerImageLoader;
import com.breco.android.app.garden.R;
import com.breco.android.app.garden.UrlRequest.HttpRequest;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.pkmmte.view.CircularImageView;
import com.seekcircle.SeekCircle;
import com.vlstr.blurdialog.BlurDialog;

import java.util.ArrayList;
import java.util.Objects;

import me.yugy.github.reveallayout.RevealLayout;

/**
 * Created by stormset on 2016. 04. 24.
 */

public class BaseActivity extends AppCompatActivity {
    boolean isCorrupted = false;
    int connectionError = 0;
    String getInstanceURL;
    String resetURL;
    String stateURL;
    String switchStateURL;
    String timerSetURL;
    String timerSetDurationURL;
    //response
    String[] states = null;
    String[] runtimes = null;
    String[] timers = null;
    String temperature = null;
    int fragmentID;
    int HOUR;
    int MINUTE;
    ArrayList<Integer> dataPackage = new ArrayList<>();
    ArrayList<String> namePackage = new ArrayList<>();
    String state;
    int lastTouchX;
    int lastTouchY;
    boolean savedCenter;
    BlurDialog blurDialogFragment;
    RevealLayout mRevealLayout;
    SeekCircle mSeekCircle;
    TextView setTempText1;
    TextView format;
    TextView setHour;
    TextView setMinute;
    TextView deviceName;
    TextView fRuntime;
    TextView fTimer;
    Toolbar toolbar;
    AppBarLayout appBarLayout;
    LinearLayout saveTimer;
    FrameLayout timePicker;
    private BubbleCloudView mListView;
    private Handler stateHandler;
    private final Runnable stateRunnable = new Runnable() {
        @Override
        public void run() {
            refreshStates(null);
            stateHandler.postDelayed(this, 1000);
        }
    };

    public static Bitmap loadBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        view.draw(canvas);
        return returnedBitmap;
    }

    private static String timeConversion(int totalSeconds) {
        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        int totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
        int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
        int hours = totalMinutes / MINUTES_IN_AN_HOUR;
        if (minutes == 0) {
            minutes = 1;
        }
        return hours + "h " + minutes + "m";
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.breco.android.app.garden.R.layout.screen_layout);
        connectionError = 0;
        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appBar);
        setSupportActionBar(toolbar);
        mListView = findViewById(R.id.my_list);
        blurDialogFragment = findViewById(R.id.blurViewFragment);
        mRevealLayout = findViewById(R.id.reveal_layout);
        setTempText1 = findViewById(R.id.setTempText1);
        format = findViewById(R.id.format);
        setHour = findViewById(R.id.setHour);
        setMinute = findViewById(R.id.setMinute);
        mSeekCircle = findViewById(com.breco.android.app.garden.R.id.seekCircle);
        saveTimer = findViewById(R.id.saveTimer);
        timePicker = findViewById(R.id.timePicker);
        deviceName = findViewById(R.id.deviceName);
        fRuntime = findViewById(R.id.fragmentRuntime);
        fTimer = findViewById(R.id.fragmentTimer);
        TextView forceOFF = findViewById(R.id.forceOFF);
        FileManagerImageLoader.prepare(this.getApplication());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String url = prefs.getString("web", null) + "/?deviceAuth=" + prefs.getString("auth", null);
        getInstanceURL = url + "&getSavedInstance=";
        resetURL = url + "&deleteSavedInstance=";
        stateURL = url + "&stateRequest=";
        switchStateURL = url + "&switchStateRequest=";
        timerSetURL = url + "&setTimer=";
        timerSetDurationURL = "&setTimerTime=";
        new HttpRequest.HttpGetRequest(output -> {
            if (output != null) {
                stateHandler = new Handler();
                stateHandler.postDelayed(stateRunnable, 0);
                dataPackage = new ArrayList<>();
                namePackage = new ArrayList<>();
                dataPackage.add(5);
                output = output.replaceAll("\\s+", "");
                String[] s1 = output.split("\\|");
                String[] s3 = s1[0].split(";");
                String[] s2 = s1[1].split(";");

                for (int i = 0; i < s2.length; i++) {
                    dataPackage.add(Integer.parseInt(s2[i]));
                    namePackage.add(s3[i]);
                }
                state = "";
                HOUR = 0;
                MINUTE = 0;
                lastTouchX = 0;
                lastTouchY = 0;
                savedCenter = false;
                final MyAdapter adapter = new MyAdapter(BaseActivity.this, createDeviceList());
                mListView.setAdapter(adapter);
                mRevealLayout.hide();
                saveTimer.setOnClickListener(v -> setState());
                forceOFF.setOnClickListener(v -> {
                    if (fragmentID > 0 && mRevealLayout.isContentShown()) {
                        switchRelay(fragmentID);
                        closeFragment();
                    }
                });
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    addListeners();
                    ViewGroup view = (ViewGroup) mListView.getChildAt(0);
                    ImageView imageView = view.findViewById(R.id.listImage);
                    TextView textView = view.findViewById(R.id.remainTime);
                    TextView runtimeText = view.findViewById(R.id.runTime);
                    imageView.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                    runtimeText.setVisibility(View.GONE);
                }, 200);
            } else {
                Snackbar.make(mListView, "Ellenőrizze az internetkapcsolatot!", Snackbar.LENGTH_LONG).show();
            }
        }).execute(getInstanceURL);
    }

    private String getAvgColor(View view) {
        Bitmap bitmap = loadBitmapFromView(view);
        int redColors = 0;
        int greenColors = 0;
        int blueColors = 0;
        int pixelCount = 0;

        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int c = bitmap.getPixel(x, y);
                pixelCount++;
                redColors += Color.red(c);
                greenColors += Color.green(c);
                blueColors += Color.blue(c);
            }
        }
        int r = (redColors / pixelCount);
        int g = (greenColors / pixelCount);
        int b = (blueColors / pixelCount);
        return String.format("#%02x%02x%02x", r, g, b);
    }

    private ArrayList<String> createDeviceList() {
        final ArrayList<String> deviceIds = new ArrayList<>();
        for (int i = 0; i < dataPackage.size(); i++) {
            deviceIds.add(String.valueOf(dataPackage.get(i)));
        }

        return deviceIds;
    }


    private void closeFragment() {
        if (mRevealLayout.isContentShown()) {
            appBarLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
            setEnabledDisabledTouch(false);
            mRevealLayout.hide(lastTouchX, lastTouchY);
            mRevealLayout.setVisibility(View.GONE);
            fragmentID = -1;
        }
    }

    public void hourSet() {
        format.setText("h");
        mSeekCircle.setMax(48);
        mSeekCircle.setProgress(0);
        setHourText(0);
        mSeekCircle.setOnSeekCircleChangeListener(new SeekCircle.OnSeekCircleChangeListener() {
            @Override
            public void onProgressChanged(SeekCircle seekCircle, int progress, boolean fromUser) {
                if (progress > 48) {
                    progress = 0;
                    mSeekCircle.setProgress(0);
                }
                setHourText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekCircle seekCircle) {

            }

            @Override
            public void onStopTrackingTouch(SeekCircle seekCircle) {

            }
        });
    }

    public void minuteSet() {
        format.setText("m");
        mSeekCircle.setMax(60);
        mSeekCircle.setProgress(0);
        setMinuteText(0);
        mSeekCircle.setOnSeekCircleChangeListener(new SeekCircle.OnSeekCircleChangeListener() {
            @Override
            public void onProgressChanged(SeekCircle seekCircle, int progress, boolean fromUser) {
                if (progress > 60) {
                    progress = 0;
                    mSeekCircle.setProgress(0);
                }
                setMinuteText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekCircle seekCircle) {

            }

            @Override
            public void onStopTrackingTouch(SeekCircle seekCircle) {

            }
        });
    }

    public void setHourText(int progress) {
        setTempText1.setText((String.valueOf(progress / 2)));
        setHour.setText((String.valueOf(progress / 2)));
    }

    public void setMinuteText(int progress) {
        setTempText1.setText((String.valueOf(progress)));
        setHour.setText((String.valueOf(HOUR)));
        setMinute.setText((String.valueOf(progress)));
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setEnabledDisabledTouch(boolean isDisabled) {
        mListView.setOnTouchListener((View v, MotionEvent event) -> {
            lastTouchX = (int) event.getX();
            lastTouchY = (int) event.getY();
            return isDisabled;
        });

        if (isDisabled) {
            for (int i = 1; i < mListView.getChildCount(); i++) {
                mListView.getChildAt(i).setLongClickable(true);
                mListView.getChildAt(i).setOnLongClickListener(v -> false);
                mListView.getChildAt(i).setOnClickListener(v -> {
                });
            }
        } else {
            addListeners();
        }
    }

    public void setState() {
        if (Objects.equals(state, "hour")) {
            HOUR = mSeekCircle.getProgress() / 2;
            minuteSet();
            state = "minute";
        } else if (Objects.equals(state, "minute")) {
            int duration = (Integer.parseInt(setHour.getText().toString()) * 3600) + (Integer.parseInt(setMinute.getText().toString()) * 60);
            new HttpRequest.HttpGetRequest(output -> {
                if (output == null) {
                    Snackbar.make(mListView, "Ellenőrizze az internetkapcsolatot!", Snackbar.LENGTH_LONG).show();
                }
            }).execute(timerSetURL + fragmentID + timerSetDurationURL + duration);
            closeFragment();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void addListeners() {
        for (int i = 1; i < mListView.getChildCount(); i++) {
            mListView.getChildAt(i).setLongClickable(true);
            mListView.getChildAt(i).setOnLongClickListener(v -> {
                openFragment(v, mListView.indexOfChild(v));
                return false;
            });
            mListView.getChildAt(i).setOnClickListener(v -> switchRelay(mListView.indexOfChild(v)));
            mListView.getChildAt(i).setOnTouchListener((v, event) -> {
                lastTouchX = (int) event.getX();
                lastTouchY = (int) event.getY();
                return false;
            });
        }
    }

    private void openFragment(View view, int id) {
        ViewGroup parentView = (ViewGroup) mListView.getChildAt(id);
        CircularImageView itemRound = parentView.findViewById(com.breco.android.app.garden.R.id.item_round);
        TextView runTime = parentView.findViewById(com.breco.android.app.garden.R.id.runTime);
        TextView remainTime = parentView.findViewById(com.breco.android.app.garden.R.id.remainTime);
        fragmentID = id;
        if (itemRound.getAlpha() == 1f) {
            appBarLayout.animate().translationY(-appBarLayout.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
            setEnabledDisabledTouch(true);
            mRevealLayout.setVisibility(View.VISIBLE);
            fRuntime.setText(runTime.getText());
            fTimer.setText(remainTime.getText());
            hourSet();
            state = "hour";
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (id != 0) {
                    deviceName.setText(namePackage.get(id - 1));
                    blurDialogFragment.setBackgroundColor(Color.parseColor("#63" + getAvgColor(view).substring(1)));
                    mRevealLayout.show(lastTouchX, lastTouchY, 1000);
                    blurDialogFragment.create(getWindow().getDecorView(), 25);
                }
            }, 10);
        }
    }

    private void switchRelay(int id) {
        if (!isCorrupted) {
            new HttpRequest.HttpGetRequest(this::refreshStates).execute(switchStateURL + id);
        }
    }

    public void refreshStates(String switchRequest) {
        if (!isOnline()) {
            connectionError++;
            if (connectionError > 10) {
                connectionError = 0;
                isCorrupted = true;
                Snackbar.make(mListView, "Nincs internetkapcsolat.", Snackbar.LENGTH_LONG).show();
            }
        } else {
            if (switchRequest == null) {
                new HttpRequest.HttpGetRequest(output -> {
                    if (output != null) {
                        String[] response = output.split("\\|");
                        states = response[0].split(";");
                        timers = response[1].split(";");
                        runtimes = response[2].split(";");
                        temperature = response[3];
                        isCorrupted = false;
                    } else {
                        isCorrupted = true;
                        Snackbar.make(mListView, "Ellenőrizze az internetkapcsolatot!", Snackbar.LENGTH_LONG).show();
                    }
                }).execute(stateURL);
            } else {
                String[] response = switchRequest.split("\\|");
                states = response[0].split(";");
                timers = response[1].split(";");
                runtimes = response[2].split(";");
                temperature = response[3];
            }
            if (states != null && timers != null && runtimes != null && temperature != null) {
                //temperature
                ViewGroup view = (ViewGroup) mListView.getChildAt(0);
                CircularImageView tItemRound = view.findViewById(R.id.item_round);
                LinearLayout tempHolder = view.findViewById(R.id.tempHolder);
                TextView textView = view.findViewById(R.id.tempHolderText);
                TextView signature = view.findViewById(R.id.signature);
                tempHolder.setVisibility(View.VISIBLE);
                signature.setVisibility(View.VISIBLE);
                textView.setText(temperature);
                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.OVAL);
                shape.setGradientRadius(180);
                shape.setColors(setGradient(Integer.parseInt(temperature)));
                tItemRound.setBackgroundDrawable(shape);
                //fragment
                if (fragmentID > 0) {
                    fRuntime.setText(timeConversion(Integer.parseInt(runtimes[fragmentID - 1])));
                    if (Integer.parseInt(timers[fragmentID - 1]) > 0) {
                        fTimer.setText(timeConversion(Integer.parseInt(timers[fragmentID - 1])));
                    }
                }
                //states
                for (int i = 0; i < states.length; i++) {
                    ViewGroup deviceView = (ViewGroup) mListView.getChildAt(i + 1);
                    CircularImageView itemRound = deviceView.findViewById(R.id.item_round);
                    TextView remainText = deviceView.findViewById(R.id.remainTime);
                    TextView runtimeText = deviceView.findViewById(R.id.runTime);
                    if (Integer.parseInt(timers[i]) != 0) {
                        remainText.setText(timeConversion(Integer.parseInt(timers[i])));
                    }

                    if (Integer.parseInt(runtimes[i]) != 0) {
                        if (Integer.parseInt(runtimes[i]) != 0)
                            runtimeText.setText(timeConversion(Integer.parseInt(runtimes[i])));
                    }
                    int state = Integer.parseInt(states[i]);
                    if (state == 1) {
                        itemRound.setAlpha(1f);
                    } else {
                        itemRound.setAlpha(0.3f);
                    }
                    if (itemRound.getAlpha() != 1f) {
                        remainText.setText("");
                        runtimeText.setText("");
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        closeFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.breco.android.app.garden.R.menu.options_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.breco.android.app.garden.R.id.reset:
                if (!isOnline()) {
                    Snackbar.make(mListView, "Nincs internetkapcsolatot!", Snackbar.LENGTH_LONG).show();
                } else if (isCorrupted) {
                    Snackbar.make(mListView, "Ellenőrizze az internetkapcsolatot!", Snackbar.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("A művelet minden beállítást törölni fog!")
                            .setPositiveButton("VISSZAÁLLÍTÁS", (dialog, id) -> {
                                if (isOnline()) {
                                    new HttpRequest.HttpGetRequest(output -> {
                                        if (output != null) {
                                            Intent intent = new Intent(BaseActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            BaseActivity.this.finish();
                                            startActivity(intent);
                                        } else {
                                            Snackbar.make(mListView, "A visszaállítás sikertelen. Ellenőrizze az internetkapcsolatot!", Snackbar.LENGTH_LONG).show();
                                        }
                                    }).execute(resetURL);
                                } else {
                                    Snackbar.make(mListView, "Nincs internetkapcsolatot!", Snackbar.LENGTH_LONG).show();
                                }
                            })
                            .setNegativeButton("MÉGSEM", (dialog, id) -> {
                                // User cancelled the dialog
                            });
                    builder.show();
                }
                return true;
            case com.breco.android.app.garden.R.id.logout:
                if (!isOnline()) {
                    Snackbar.make(mListView, "Nincs internetkapcsolat!", Snackbar.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder logoutbuilder = new AlertDialog.Builder(this);
                    logoutbuilder.setMessage("Valóban kijelentkezik?")
                            .setPositiveButton("KIJELENTKEZÉS", (dialog, id) -> {
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BaseActivity.this);
                                prefs.edit().putString("web", "").commit();
                                prefs.edit().putString("auth", "").commit();
                                prefs.edit().putBoolean("loggedStatusAUTHORIDD", true).commit();
                                Intent intent = new Intent(BaseActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                BaseActivity.this.finish();
                                startActivity(intent);
                            })
                            .setNegativeButton("MÉGSEM", (dialog, id) -> {
                                // User cancelled the dialog
                            });
                    logoutbuilder.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private float interpolate(float a, float b, float bias) {
        return (a + ((b - a) * bias));
    }

    private int interpolateColor(int colorA, int colorB, float bias) {
        float[] hsvColorA = new float[3];
        Color.colorToHSV(colorA, hsvColorA);

        float[] hsvColorB = new float[3];
        Color.colorToHSV(colorB, hsvColorB);

        hsvColorB[0] = interpolate(hsvColorA[0], hsvColorB[0], bias);
        hsvColorB[1] = interpolate(hsvColorA[1], hsvColorB[1], bias);
        hsvColorB[2] = interpolate(hsvColorA[2], hsvColorB[2], bias);

        return Color.HSVToColor(hsvColorB);
    }

    private int[] setGradient(int temp) {
        int[] Colors = new int[3];
        float bias = ((float) 1 / 40) * temp;

        if (temp > 17) {
            Colors[0] = interpolateColor(Color.parseColor("#F7BE81"), Color.parseColor("#F78181"), bias);
            Colors[1] = interpolateColor(Color.parseColor("#FAAC58"), Color.parseColor("#FA5858"), bias);
            Colors[2] = interpolateColor(Color.parseColor("#FE9A2E"), Color.parseColor("#FE2E2E"), bias);
        } else {
            Colors[0] = interpolateColor(Color.parseColor("#819FF7"), Color.parseColor("#81DAF5"), bias);
            Colors[1] = interpolateColor(Color.parseColor("#5882FA"), Color.parseColor("#58D3F7"), bias);
            Colors[2] = interpolateColor(Color.parseColor("#2E64FE"), Color.parseColor("#2ECCFA"), bias);
        }
        return Colors;
    }

    private class MyAdapter extends ArrayAdapter<String> {

        public MyAdapter(final Context context, final ArrayList<String> contacts) {
            super(context, 0, contacts);
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(com.breco.android.app.garden.R.layout.list_item, null);
            }
            FileManagerImageLoader.getInstance().addTask(dataPackage, view, null, 48, 48, false);
            return view;
        }
    }
}