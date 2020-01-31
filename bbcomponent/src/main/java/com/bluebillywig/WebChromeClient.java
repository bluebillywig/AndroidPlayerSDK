package com.bluebillywig;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.ConsoleMessage;

public class WebChromeClient extends android.webkit.WebChromeClient {
    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        Log.d("console " + consoleMessage.messageLevel() + " message", consoleMessage.message());
        return true;
    }

    @Override
    public Bitmap getDefaultVideoPoster() {
        return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    }
}
