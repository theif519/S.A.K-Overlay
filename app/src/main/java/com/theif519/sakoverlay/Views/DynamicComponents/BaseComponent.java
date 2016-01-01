package com.theif519.sakoverlay.Views.DynamicComponents;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.theif519.sakoverlay.Animations.ResizeAnimation;
import com.theif519.sakoverlay.Listeners.OnAnimationEndListener;
import com.theif519.sakoverlay.Listeners.OnAnimationStartListener;
import com.theif519.sakoverlay.Misc.Globals;
import com.theif519.sakoverlay.R;
import com.theif519.sakoverlay.Views.AutoResizeTextView;
import com.theif519.utils.Misc.MutableObject;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by theif519 on 12/27/2015.
 */
public abstract class BaseComponent extends FrameLayout {

    class LongPressComponentMenuGesture extends  GestureDetector.SimpleOnGestureListener {

        private ViewGroup mLayout;

        protected LongPressComponentMenuGesture(ViewGroup layout){
            mLayout = layout;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            clearResults(mLayout);
            mOptionsMenu.show();
        }
    }

    private GestureDetector mComponentMenuGesture;

    private Button mResizeButton, mMoveButton;
    private FrameLayout mContainer, mRoot;
    private RadioButton mWidthWrapContent, mHeightWrapContent, mWidthFillParent, mHeightFillParent, mWidthCustom, mHeightCustom;
    private EditText mWidth, mHeight, mX, mY;
    private View mContentView;
    private AlertDialog mOptionsMenu;

    public BaseComponent(Context context) {
        this(context, null);
    }

    public BaseComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.dynamic_component, this);
        mResizeButton = (Button) findViewById(R.id.component_wrapper_resize);
        mResizeButton.setOnTouchListener(this::resize);
        mMoveButton = (Button) findViewById(R.id.component_wrapper_move);
        mMoveButton.setOnTouchListener(this::move);
        mContainer = (FrameLayout) findViewById(R.id.component_wrapper_container);
        mContainer.addView(mContentView = createView(context));
        mRoot = (FrameLayout) findViewById(R.id.component_wrapper_root);
        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mOptionsMenu = createOptions();
        mOptionsMenu.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setup();
    }

    private float tmpX, tmpY;

    private boolean resize(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                tmpX = mRoot.getX();
                tmpY = mRoot.getY();
                return false;
            case MotionEvent.ACTION_MOVE:
                mRoot.getLayoutParams().width = (int) Math.abs(event.getRawX() - tmpX);
                mRoot.getLayoutParams().height = (int) Math.abs(event.getRawY() - tmpY);
                mRoot.invalidate();
                mRoot.requestLayout();
                return false;
            case MotionEvent.ACTION_UP:
                return true;
            default:
                return false;
        }
    }

    private float touchXOffset, touchYOffset;

    private boolean move(View v, MotionEvent event) {
        if (mComponentMenuGesture.onTouchEvent(event)) return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchXOffset = (int) event.getRawX() - getX();
                touchYOffset = (int) event.getRawY() - getY();
                return false;
            case MotionEvent.ACTION_MOVE:
                setX(event.getRawX() - touchXOffset);
                setY(event.getRawY() - touchYOffset);
                return false;
            case MotionEvent.ACTION_UP:
                return true;
            default:
                return false;
        }
    }

    abstract protected View createView(Context context);

    private AlertDialog createOptions() {
        ScrollView scrollView = new ScrollView(getContext());
        LinearLayout layout = new LinearLayout(getContext());
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(layout);
        addOptionDialog(layout);
        mComponentMenuGesture = new GestureDetector(new LongPressComponentMenuGesture(layout));
        return new AlertDialog.Builder(getContext())
                .setTitle("Component Options")
                .setView(scrollView)
                .setPositiveButton("OK!", (thisDialog, which) -> {
                    StringBuilder errMsg = new StringBuilder();
                    sanitizeResults(layout, errMsg);
                    if (errMsg.toString().isEmpty()) {
                        handleResults(layout);
                        thisDialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Error: \"" + errMsg.toString() + "\"", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("NO!", (thisDialog, which) -> {
                    thisDialog.dismiss();
                })
                .setOnDismissListener((thisDialog) -> clearResults(layout))
                .create();
    }

    protected void addOptionDialog(ViewGroup layout) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.component_base, null);
        layout.addView(createCategory("Base Component", v));
        layout.addView(v);
        mX = (EditText) layout.findViewById(R.id.component_base_x);
        mY = (EditText) layout.findViewById(R.id.component_base_y);
        mWidth = (EditText) layout.findViewById(R.id.component_base_width);
        mWidthWrapContent = (RadioButton) layout.findViewById(R.id.component_base_width_wrap);
        mWidthFillParent = (RadioButton) layout.findViewById(R.id.component_base_width_fill);
        mWidthCustom = (RadioButton) layout.findViewById(R.id.component_base_width_custom);
        mHeight = (EditText) layout.findViewById(R.id.component_base_height);
        mHeightWrapContent = (RadioButton) layout.findViewById(R.id.component_base_height_wrap);
        mHeightFillParent = (RadioButton) layout.findViewById(R.id.component_base_height_fill);
        mHeightCustom = (RadioButton) layout.findViewById(R.id.component_base_height_custom);
        mWidthCustom.setOnCheckedChangeListener((button, isChecked) -> {
            ViewGroup parent = (ViewGroup) mWidth.getParent();
            if (isChecked) {
                parent.setVisibility(VISIBLE);
            } else parent.setVisibility(INVISIBLE);
        });
        mHeightCustom.setOnCheckedChangeListener((button, isChecked) -> {
            ViewGroup parent = (ViewGroup) mHeight.getParent();
            if (isChecked) {
                parent.setVisibility(VISIBLE);
            } else parent.setVisibility(INVISIBLE);
        });
    }

    protected void sanitizeResults(ViewGroup layout, StringBuilder errMsg) {
        ViewGroup parent = (ViewGroup) getParent();
        if (mWidthCustom.isChecked()) {
            if (Integer.parseInt(mWidth.getText().toString()) > parent.getWidth()) {
                errMsg.append("Width must be less than ");
                errMsg.append(parent.getWidth());
                errMsg.append("\n");
            }
        }
        if (mHeightCustom.isChecked()) {
            if (Integer.parseInt(mHeight.getText().toString()) > parent.getHeight()) {
                errMsg.append("Height must be less than ");
                errMsg.append(parent.getHeight());
                errMsg.append("\n");
            }
        }
        if (Integer.parseInt(mX.getText().toString()) > parent.getWidth()) {
            errMsg.append("X-Coordinate must be less than ");
            errMsg.append(parent.getWidth());
            errMsg.append("\n");
        }
        if (Integer.parseInt(mY.getText().toString()) > parent.getHeight()) {
            errMsg.append("Y-Coordinate must be less than ");
            errMsg.append(parent.getHeight());
            errMsg.append("\n");
        }
    }

    protected void setup(){
        mWidthWrapContent.setChecked(true);
        mHeightWrapContent.setChecked(true);
    }

    protected void clearResults(ViewGroup layout) {
        mWidth.setText("" + getWidth());
        mHeight.setText("" + getHeight());
        mX.setText("" + (int) getX());
        mY.setText("" + (int) getY());
        ((ViewGroup) mWidth.getParent()).setVisibility(INVISIBLE);
        switch(mRoot.getLayoutParams().width){
            case ViewGroup.LayoutParams.MATCH_PARENT:
                mWidthFillParent.setChecked(true);
                break;
            case ViewGroup.LayoutParams.WRAP_CONTENT:
                mWidthWrapContent.setChecked(true);
                break;
            default:
                mWidthCustom.setChecked(true);
                ((ViewGroup) mWidth.getParent()).setVisibility(VISIBLE);
                break;
        }
        ((ViewGroup) mHeight.getParent()).setVisibility(INVISIBLE);
        switch(mRoot.getLayoutParams().height){
            case ViewGroup.LayoutParams.MATCH_PARENT:
                mHeightFillParent.setChecked(true);
                break;
            case ViewGroup.LayoutParams.WRAP_CONTENT:
                mHeightWrapContent.setChecked(true);
                break;
            default:
                mHeightCustom.setChecked(true);
                ((ViewGroup) mHeight.getParent()).setVisibility(VISIBLE);
                break;
        }
    }

    protected void handleResults(ViewGroup layout) {
        if (mWidthFillParent.isChecked()) {
            mRoot.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        } else if (mWidthWrapContent.isChecked()) {
            mRoot.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
        } else {
            mRoot.getLayoutParams().width = Integer.parseInt(mWidth.getText().toString());
        }
        if (mHeightFillParent.isChecked()) {
            mRoot.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else if (mHeightWrapContent.isChecked()) {
            mRoot.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else {
            mRoot.getLayoutParams().height = Integer.parseInt(mHeight.getText().toString());
        }
        setX(Integer.parseInt(mX.getText().toString()));
        setY(Integer.parseInt(mY.getText().toString()));
        mRoot.invalidate();
        mRoot.requestLayout();
    }

    protected View createCategory(String category, final ViewGroup layout){
        TextView button = new AutoResizeTextView(getContext());
        button.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics()));
        button.setGravity(Gravity.CENTER);
        button.setText(category);
        button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics())));
        button.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        button.setTypeface(Typeface.DEFAULT_BOLD);
        final MutableObject<Integer> width = new MutableObject<>(0), height = new MutableObject<>(0);
        button.setOnClickListener(v -> {
            if(layout.getVisibility() == VISIBLE){
                if(width.get() == 0 && height.get() == 0) {
                    width.set(layout.getWidth());
                    height.set(layout.getHeight());
                }
                ResizeAnimation animation = new ResizeAnimation(layout, 0, 0);
                animation.setDuration(500);
                animation.setAnimationListener(new OnAnimationEndListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        layout.setVisibility(INVISIBLE);
                    }
                });
                layout.startAnimation(animation);
            } else {
                ResizeAnimation animation = new ResizeAnimation(layout, width.get(), height.get());
                animation.setDuration(500);
                animation.setAnimationListener(new OnAnimationStartListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        layout.setVisibility(VISIBLE);
                    }
                });
                layout.startAnimation(animation);
            }
        });
        return button;
    }

    public JSONObject serialize() {
        try {
            return new JSONObject()
                    .put(Globals.Keys.X, getX())
                    .put(Globals.Keys.Y, getY())
                    .put(Globals.Keys.WIDTH, getWidth())
                    .put(Globals.Keys.HEIGHT, getHeight());
        } catch (JSONException e) {
            throw new RuntimeException("Error serializing BaseComponent: Threw a JSONException with message \"" + e.getMessage() + "\"");
        }
    }

    public void deserialize(JSONObject obj) {
        try {
            setX((float) obj.getDouble(Globals.Keys.X));
            setY((float) obj.getDouble(Globals.Keys.Y));
            mRoot.getLayoutParams().width = obj.getInt(Globals.Keys.WIDTH);
            mRoot.getLayoutParams().height = obj.getInt(Globals.Keys.HEIGHT);
            mRoot.invalidate();
            mRoot.requestLayout();
        } catch (JSONException e) {
            throw new RuntimeException("Error deserializing BaseComponent: Threw a JSONException with message \"" + e.getMessage() + "\"");
        }
    }
}
