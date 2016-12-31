package com.fsquirrelsoft.financier.ui;

import android.os.Bundle;

import com.fsquirrelsoft.financier.context.ContextsActivity;

/**
 * a dummy activity, by default, it do nothing thing and quit.
 * 
 * @author dennis
 *
 */
public class DummyActivity extends ContextsActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        finish();
    }

    @Override
    protected void onResume() {
        finish();
    }
}
