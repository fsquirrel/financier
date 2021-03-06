package com.fsquirrelsoft.financier.ui;

import android.os.Bundle;
import android.webkit.WebView;

import com.fsquirrelsoft.commons.util.GUIs;
import com.fsquirrelsoft.financier.context.ContextsActivity;
import com.fsquirrelsoft.financier.core.R;

/**
 * @author dennis
 */
public class LocalWebViewActivity extends ContextsActivity {

    public static final String INTENT_URI = "uri";
    public static final String INTENT_URI_ID = "uriid";
    public static final String INTENT_TITLE = "title";

    WebView webView;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.webview);
        initWebView();
        initInit();
    }

    private void initWebView() {
        webView = (WebView) findViewById(R.id.webview);

        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JSCallHandler(), "fsfctrl");
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
    }

    private void initInit() {
        Bundle bundle = getIntentExtras();
        String uri = null;
        int rid = bundle.getInt(INTENT_URI_ID, -1);
        if (rid != -1) {
            uri = getResources().getString(rid);
        } else {
            uri = bundle.getString(INTENT_URI);
        }

        String title = bundle.getString(INTENT_TITLE);
        if (title != null) {
            this.setTitle(title);
            // to late to set here?
            // this.setTheme(android.R.style.Theme_Dialog);
        }

        webView.loadUrl(Constants.LOCAL_URL_PREFIX + uri);
    }

    private void onLinkClicked(final String path) {
        webView.loadUrl(Constants.LOCAL_URL_PREFIX + path);
    }

    class JSCallHandler {
        public void onLinkClicked(final String path) {
            GUIs.post(new Runnable() {
                public void run() {
                    LocalWebViewActivity.this.onLinkClicked(path);
                }
            });
        }
    }
}
