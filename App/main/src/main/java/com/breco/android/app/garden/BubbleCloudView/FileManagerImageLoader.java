package com.breco.android.app.garden.BubbleCloudView;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;

import com.breco.android.app.garden.R;
import com.pkmmte.view.CircularImageView;

import java.util.ArrayList;

/**
 * Created by dodola on 15/7/23.
 * Modified by stormset on 2016. 04. 22.
 */
public class FileManagerImageLoader {
    private static FileManagerImageLoader instance;
    protected Resources mResources;
    int i = 0;

    private FileManagerImageLoader(Context context) {
        mResources = context.getResources();
    }

    public synchronized static FileManagerImageLoader getInstance() {
        return instance;
    }

    public static synchronized void prepare(Application appContext) {
        if (instance == null) {
            instance = new FileManagerImageLoader(appContext);
        }
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

        // NOTE For some reason the method HSVToColor fail in edit mode. Just use the start color for now

        return Color.HSVToColor(hsvColorB);
    }

    private int[] setGradient(int temp, boolean isSummer) {
        int[] Colors = new int[3];
        float bias = ((float) 1 / 40) * temp;

        if (isSummer) {
            temp += 2;
        }
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

    public void addTask(ArrayList<Integer> device, View view, Color color, int width, int height, boolean isBig) {
        final CircularImageView itemRound = (CircularImageView) view.findViewById(R.id.item_round);
        if (device.get(i) == 0) {
            final ImageView image = view.findViewById(R.id.listImage);
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
        if (device.get(i) == 1) {
            final ImageView image = view.findViewById(R.id.listImage);
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
        if (device.get(i) == 2) {
            final ImageView image = view.findViewById(R.id.listImage);
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
        if (device.get(i) == 3) {
            final ImageView image = view.findViewById(R.id.listImage);
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
        if (device.get(i) == 5) {
            final ImageView image = view.findViewById(R.id.listImage);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setGradientRadius(180);
            int[] Colors = new int[3];
            Colors[0] = Color.parseColor("#F781F3");
            Colors[1] = Color.parseColor("#BE81F7");
            Colors[2] = Color.parseColor("#5882FA");
            shape.setColors(Colors);
            itemRound.setBackgroundDrawable(shape);
        }
        i++;
    }
}