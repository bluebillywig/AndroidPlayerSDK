package com.bluebillywig;

import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;

public class WebChromeClientFullscreen extends WebChromeClient {
    private View customView;
    private WebView webView;

    private FrameLayout customViewContainer = null;
    private android.webkit.WebChromeClient.CustomViewCallback customViewCallback;
    private BBPlayer.JavascriptAppInterface javascriptAppInterface = null;

    WebChromeClientFullscreen(FrameLayout customViewContainer, WebView webView, BBPlayer.JavascriptAppInterface javascriptAppInterface) {
        this.customViewContainer = customViewContainer;
        this.webView = webView;
        this.javascriptAppInterface = javascriptAppInterface;
    }

    public void onHideCustomView() {
        Log.d("setWebViewClient", "on hide custom view");
        super.onHideCustomView();

        if (customView == null) {
            return;
        }


        if (javascriptAppInterface != null) {
            javascriptAppInterface.callParent("retractFullscreenView", null);
        }

        webView.setVisibility(View.VISIBLE);
        customViewContainer.setVisibility(View.GONE);

        // Hide the custom view.
        customView.setVisibility(View.GONE);

        // Remove the custom view from its container.
        customViewContainer.removeView(customView);
        customViewCallback.onCustomViewHidden();

        customView = null;
    }

    public void onShowCustomView(View view, android.webkit.WebChromeClient.CustomViewCallback callback) {
        Log.d("setWebViewClient", "on show custom view");
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }

        if (javascriptAppInterface != null) {
            javascriptAppInterface.callParent("fullscreenView", null);
        }

        customView = view;
        webView.setVisibility(View.GONE);
        customViewContainer.setVisibility(View.VISIBLE);
        customViewContainer.addView(view);
        customViewCallback = callback;
    }
}
