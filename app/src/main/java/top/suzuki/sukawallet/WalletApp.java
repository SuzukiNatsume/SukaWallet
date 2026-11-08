package top.suzuki.sukawallet;

import android.app.Application;

public final class WalletApp extends Application {
    // Deliberately process-local: ordinary backgrounding keeps the session, process death clears it.
    static boolean unlocked;
    @Override public void onCreate(){super.onCreate();Migration.importOnce(this);}
}
