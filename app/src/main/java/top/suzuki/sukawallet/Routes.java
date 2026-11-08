package top.suzuki.sukawallet;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

/** Routes verified against Xiaomi Smart Card 26.09.21.3.f. No card data is read. */
final class Routes {
    static final String PACKAGE = "com.miui.tsmclient";
    static final String[] URIS = {
        "tsmclient://card?type=TRANSCARD&action=transit_detail",
        "tsmclient://card?type=MIFARE_ENTRANCE&action=issue",
        "tsmclient://card?type=BANKCARD&action=mipay_list",
        "tsmclient://card?type=DIGITALKEY&action=issue",
        "tsmclient://swiping"
    };

    static Intent intent(int index) {
        if (index < 0 || index >= URIS.length) throw new IllegalArgumentException("Unknown route");
        String activity = index == 4 ? ".ui.quick.DoubleClickActivity" : ".ui.MainActivity";
        return new Intent(Intent.ACTION_VIEW, Uri.parse(URIS[index]))
                .setComponent(new ComponentName(PACKAGE, PACKAGE + activity));
    }
    private Routes() {}
}
