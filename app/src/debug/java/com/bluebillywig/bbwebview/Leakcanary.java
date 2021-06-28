package com.bluebillywig.bbwebview;

import leakcanary.AppWatcher;

public class Leakcanary {
    public static void expectWeaklyReachable(Object object, String description) {
        AppWatcher.INSTANCE.getObjectWatcher().expectWeaklyReachable(object, "description");
    }
}
