package top.suzuki.sukawallet;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

/** Original View-based styling inspired by the Miuix / Material split in KernelSU. */
final class UiStyle {
    final boolean material, dark;
    final int background, surface, ink, muted, accent, onAccent;
    static String mode(Context c) { return c.getSharedPreferences("appearance",0).getString("style","miuix"); }
    static void set(Context c,String mode) { c.getSharedPreferences("appearance",0).edit().putString("style",mode).apply(); }
    static UiStyle of(Context c) { return new UiStyle(c); }
    private UiStyle(Context c) {
        material=mode(c).equals("material");
        dark=(c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
        if(material && Build.VERSION.SDK_INT>=31) {
            background=c.getColor(dark?android.R.color.system_neutral1_900:android.R.color.system_neutral1_10);
            surface=c.getColor(dark?android.R.color.system_neutral2_800:android.R.color.system_neutral2_50);
            ink=c.getColor(dark?android.R.color.system_neutral1_50:android.R.color.system_neutral1_900);
            muted=c.getColor(dark?android.R.color.system_neutral2_200:android.R.color.system_neutral2_600);
            accent=c.getColor(dark?android.R.color.system_accent1_200:android.R.color.system_accent1_600);
            onAccent=c.getColor(dark?android.R.color.system_accent1_800:android.R.color.system_accent1_0);
        } else {
            background=dark?(material?0xFF11141C:0xFF000000):0xFFF6F7FB;
            surface=dark?(material?0xFF1D2230:0xFF1C1C1E):0xFFFFFFFF;
            ink=dark?(material?0xFFF0F2FA:0xFFF5F5F5):0xFF202738;
            muted=dark?(material?0xFFA7B0C3:0xFF999999):0xFF788195;
            accent=material?(dark?0xFFD0BCFF:0xFF6750A4):0xFF4964DF;
            onAccent=material&&dark?0xFF381E72:0xFFFFFFFF;
        }
    }
}
