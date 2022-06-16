package com.breco.android.app.garden.launcher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.breco.android.app.garden.R;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;

/**
 * Created by stormset on 2016. 04. 30.
 */

public class ConfigSlide extends Fragment {
    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    public int id;
    Configuration configuration = new Configuration();
    private int layoutResId;

    public static ConfigSlide newInstance(int layoutResId, int id) {
        ConfigSlide sampleSlide = new ConfigSlide();
        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        args.putInt("id", id);
        sampleSlide.setArguments(args);
        return sampleSlide;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID)) {
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
            id = getArguments().getInt("id");
        }
        configuration = new Configuration();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(layoutResId, container, false);
        EditText editText = view.findViewById(R.id.editText);
        editText.setText(String.valueOf(id));
        editText.setOnClickListener(v -> editText.setText(""));
        view.findViewById(R.id.testDevice).setOnClickListener(v -> configuration.clickTest(v, id));
        TextView tv = view.findViewById(R.id.idHolder);
        if (tv.getText() == "") {
            tv.setText(String.valueOf(id));
        }
        MultiStateToggleButton button = view.findViewById(R.id.mstb_multi_id);
        if (button != null && button.getValue() < 0) {
            button.setValue(0);
            configuration.setImageIcon((ViewGroup) view, 0);
        }
        ImageView imageView = view.findViewById(R.id.icon);
        imageView.setOnClickListener(v -> configuration.setIcon((ViewGroup) view));
        return view;
    }
}