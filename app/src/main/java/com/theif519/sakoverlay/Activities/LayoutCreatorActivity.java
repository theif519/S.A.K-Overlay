package com.theif519.sakoverlay.Activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Views.DynamicComponents.BaseComponent;
import com.theif519.sakoverlay.Views.DynamicComponents.ComponentFactory;
import com.theif519.utils.Misc.Callbacks;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by theif519 on 12/26/2015.
 */
public class LayoutCreatorActivity extends Activity {

    public interface IViewCallbacks {
        View createView(Context context);

        Object getResult(View view);

        boolean isResultValid(Object result);

        String getBadResultMessage(Object result);

        void handleResult(Object result);
    }

    private Button mTextButton, mImageViewButton, mLayoutButton, mButtonButton;
    private ViewGroup mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_creater);
        mTextButton = (Button) findViewById(R.id.layout_create_text);
        mImageViewButton = (Button) findViewById(R.id.layout_create_image);
        mLayoutButton = (Button) findViewById(R.id.layout_create_layout);
        mButtonButton = (Button) findViewById(R.id.layout_create_button);
        mLayout = (ViewGroup) findViewById(R.id.layout_view);
        mTextButton.setOnClickListener(this::createItem);
        mImageViewButton.setOnClickListener(this::createItem);
        mLayoutButton.setOnClickListener(this::createItem);
        mButtonButton.setOnClickListener(this::createItem);
        findViewById(R.id.layout_close).setOnClickListener(v -> {
            new Callbacks.CallbackOnRootChildren<View>() {
                @Override
                public void onChild(View child) {
                    if (child != mLayout) {
                        Log.i(getClass().getName(), child.getClass().getSimpleName() + ": { X: " +
                                child.getX() + ", Y: " + child.getY() + ", Width: " + child.getWidth() + ", Height: " +
                                child.getHeight() + " }");
                    }
                }
            }.onChild(mLayout);
            finish();
        });
    }

    private void createItem(View v) {
        BaseComponent component = ComponentFactory.getComponent(this, (String) v.getTag());
        if (component == null) {
            Log.w(getClass().getName(), "ComponentFactory returned null for the tag: \"" + v.getTag() + "\"");
            return;
        }
        mLayout.addView(component);
    }

    private byte[] serialize() {
        JSONArray array = new JSONArray();
        for (int i = 0; i < mLayout.getChildCount(); i++) {
            View v = mLayout.getChildAt(i);
            if (v instanceof BaseComponent) {
                BaseComponent component = (BaseComponent) v;
                array.put(component.serialize());
            }
        }
        return array.toString().getBytes();
    }

    private void deserialize(byte[] data) {
        try {
            JSONArray array = new JSONArray(new String(data));
            // Get Widget from Factory, pass context and JSONObject serialized data.
        } catch (JSONException e) {
            throw new RuntimeException("Error parsing JSONArray: Threw a JSONException with message \"" + e.getMessage() + "\"");
        }
    }
}