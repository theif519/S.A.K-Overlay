package com.theif519.sakoverlay.Components.Types.Conditionals.Impl;

import android.view.View;

/**
 * Created by theif519 on 1/6/2016.
 *
 * Base class for Conditionals, it requires a View for the constructor as this view is the one passed in at
 * runtime, and therefore guarantees that it is abstracted away from the particular component that
 * implements this.
 */
public class Conditionals {
    public Conditionals(View v){}
}
