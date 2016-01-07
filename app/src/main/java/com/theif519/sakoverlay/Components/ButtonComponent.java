package com.theif519.sakoverlay.Components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.theif519.sakoverlay.Components.Misc.ConstructHelper;
import com.theif519.sakoverlay.R;

import org.json.JSONObject;

/**
 * Created by theif519 on 1/2/2016.
 */
public class ButtonComponent extends TextComponent {

    public static final String IDENTIFIER = "Button";

    private Button mOnClickButton;

    public ButtonComponent(Context context) {
        super(context);
    }

    public ButtonComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View createView(Context context) {
        TEXT_VIEW = new Button(context);
        TEXT_VIEW.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.black)));
        TEXT_VIEW.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return TEXT_VIEW;
    }

    @Override
    protected void addOptionDialog(ViewGroup layout) {
        super.addOptionDialog(layout);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.component_text_button, null);
        layout.addView(createCategory("Button Component", v));
        layout.addView(v);
        mOnClickButton = (Button) layout.findViewById(R.id.component_text_button_onclick);
        mOnClickButton.setOnClickListener(v1 -> new AlertDialog.Builder(getContext())
                .setTitle("Button onClick")
                .setView(new ConstructHelper(getContext(), getHelper()).getView())
                .setPositiveButton("OK!", (dialog1, which) -> {
                    dialog1.dismiss();
                })
                .setNegativeButton("NO!", (dialog2, which) -> {
                    dialog2.dismiss();
                })
                .show());
    }

    @Override
    public JSONObject serialize() {
        return super.serialize();
    }

    @Override
    public void deserialize(JSONObject obj) {
        super.deserialize(obj);
    }
}