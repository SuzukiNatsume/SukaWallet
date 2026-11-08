package top.suzuki.sukawallet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.provider.Settings;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class MainActivity extends SecureActivity {
    private int ink, muted, background, surface;
    private boolean dark;
    private UiStyle ui;
    private static final int[] TITLES = {R.string.transit, R.string.door, R.string.bank, R.string.car};
    private static final int[] ACCENTS = {0xFF426BDF, 0xFF20866F, 0xFF9B6B27, 0xFF8557C7};

    @Override public void onCreate(Bundle state) {
        ui=UiStyle.of(this);
        dark=ui.dark;
        if (dark) setTheme(android.R.style.Theme_Material_NoActionBar);
        super.onCreate(state);
        ink=ui.ink;
        muted=ui.muted;
        background=ui.background;
        surface=ui.surface;
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(background);
        scroll.setClipToPadding(false);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(26),new SettingsLook(this).px(56)+dp(4),dp(26),dp(20));
        scroll.addView(body, new ScrollView.LayoutParams(-1, -1));
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text(getString(R.string.app_name), ui.material?32:34, ink);
        title.setTypeface(null, ui.material?Typeface.NORMAL:Typeface.BOLD);
        title.setMinHeight(dp(65));
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        body.addView(header);
        space(body, 26);
        for (int i = 0; i < TITLES.length; i++) {
            final int route = i;
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(18), dp(18), dp(18), dp(18));
            row.setMinimumHeight(dp(ui.material?88:99));
            row.setBackground(ripple(surface, ui.material?28:22));
            row.setClickable(true);
            row.setFocusable(true);
            row.setContentDescription(getString(TITLES[i]));
            row.setOnClickListener(v -> launch(route));
            Icon icon = new Icon(i, ui.material?ui.accent:ACCENTS[i]);
            row.addView(icon, new LinearLayout.LayoutParams(dp(50), dp(50)));
            LinearLayout label = new LinearLayout(this);
            label.setOrientation(LinearLayout.VERTICAL);
            label.setPadding(dp(16), 0, 0, 0);
            TextView name = text(getString(TITLES[i]), ui.material?20:19, ink);
            name.setTypeface(null, ui.material?Typeface.NORMAL:Typeface.BOLD);
            label.addView(name);
            row.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
            TextView arrow = text("›", 26, muted);
            arrow.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            if(!ui.material)row.addView(arrow);
            body.addView(row, new LinearLayout.LayoutParams(-1, -2));
            space(body, ui.material?8:12);
        }
        space(body, 8);
        Button swipe = new Button(this);
        swipe.setText(getString(R.string.swipe));
        swipe.setTextSize(16);
        swipe.setAllCaps(false);
        swipe.setTextColor(ui.onAccent);
        swipe.setBackground(ripple(ui.accent, ui.material?29:18));
        swipe.setOnClickListener(v -> launch(4));
        swipe.setMinHeight(dp(58));
        android.widget.FrameLayout root=new android.widget.FrameLayout(this);
        root.addView(scroll,new android.widget.FrameLayout.LayoutParams(-1,-1));
        android.widget.FrameLayout.LayoutParams swipeParams=new android.widget.FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);swipeParams.setMarginStart(dp(26));swipeParams.setMarginEnd(dp(26));swipeParams.bottomMargin=dp(20);root.addView(swipe,swipeParams);
        SettingsLook navigationLook=new SettingsLook(this);
        View menu=Navigation.more(this);
        android.widget.FrameLayout.LayoutParams menuParams=new android.widget.FrameLayout.LayoutParams((int)(48*navigationLook.unit),navigationLook.px(40),Gravity.TOP|Gravity.END);menuParams.topMargin=navigationLook.px(8);menuParams.setMarginEnd(navigationLook.px(20));root.addView(menu,menuParams);
        menu.setContentDescription(getString(R.string.settings));
        menu.setOnClickListener(v->startActivity(new Intent(this,SettingsActivity.class)));
        final int[] bottomInset={0};
        root.setOnApplyWindowInsetsListener((v,insets)->{
            android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());bottomInset[0]=bars.bottom;menuParams.topMargin=bars.top+navigationLook.px(8);menuParams.setMarginEnd(bars.right+navigationLook.px(20));menu.setLayoutParams(menuParams);
            swipeParams.bottomMargin=bars.bottom+dp(20);swipeParams.setMarginStart(bars.left+dp(26));swipeParams.setMarginEnd(bars.right+dp(26));swipe.setLayoutParams(swipeParams);
            scroll.setPadding(bars.left,bars.top,bars.right,bars.bottom+Math.max(swipe.getHeight(),dp(58))+dp(40));return insets;
        });
        swipe.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{if(b-t!=ob-ot)scroll.setPadding(scroll.getPaddingLeft(),scroll.getPaddingTop(),scroll.getPaddingRight(),bottomInset[0]+b-t+dp(40));});
        setContentView(root);
        if (getWindow().getInsetsController() != null) {
            getWindow().getInsetsController().setSystemBarsAppearance(dark ? 0 : android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
        }
    }

    private boolean installed() {
        try { return getPackageManager().getApplicationInfo(Routes.PACKAGE, 0).enabled; }
        catch (PackageManager.NameNotFoundException e) { return false; }
    }
    private void launch(int index) {
        if (!installed()) {
            new AlertDialog.Builder(this).setTitle(getString(R.string.smart_unavailable))
                .setMessage(getString(R.string.smart_install))
                .setPositiveButton(getString(R.string.ok), null).show();
            return;
        }
        try { startActivity(Routes.intent(index)); }
        catch (ActivityNotFoundException | SecurityException e) {
            new AlertDialog.Builder(this).setTitle(getString(R.string.cannot_open))
                .setMessage(getString(R.string.smart_route_error))
                .setPositiveButton(getString(R.string.app_info), (d, w) -> {
                    try { startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + Routes.PACKAGE))); }
                    catch (ActivityNotFoundException ignored) { }
                }).setNegativeButton(getString(R.string.close), null).show();
        }
    }
    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + .5f); }
    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color); return t;
    }
    private void space(LinearLayout layout, int height) { layout.addView(new View(this), new LinearLayout.LayoutParams(1, dp(height))); }
    private RippleDrawable ripple(int color, int radius) {
        GradientDrawable shape = new GradientDrawable(); shape.setColor(color); shape.setCornerRadius(dp(radius));
        GradientDrawable mask = new GradientDrawable(); mask.setColor(Color.WHITE); mask.setCornerRadius(dp(radius));
        return new RippleDrawable(ColorStateList.valueOf(dark ? 0x33FFFFFF : 0x184964DF), shape, mask);
    }
    private final class Icon extends View {
        private final int kind, color;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Icon(int kind, int color) { super(MainActivity.this); this.kind = kind; this.color = color; setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO); }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            c.save(); c.scale(getWidth()/50f, getHeight()/50f);
            paint.setStyle(Paint.Style.FILL); paint.setColor(color); paint.setAlpha(dark ? 50 : 22);
            c.drawRoundRect(new RectF(0, 0, 50, 50), ui.material?25:15, ui.material?25:15, paint);
            paint.setAlpha(255); paint.setStrokeWidth(2); paint.setStrokeCap(Paint.Cap.ROUND); paint.setStrokeJoin(Paint.Join.ROUND); paint.setStyle(Paint.Style.STROKE);
            if (kind == 0) {
                c.drawRoundRect(new RectF(15, 11, 35, 36), 5, 5, paint);
                c.drawRoundRect(new RectF(18, 16, 32, 25), 2, 2, paint);
                c.drawLine(19, 36, 17, 39, paint); c.drawLine(31, 36, 33, 39, paint);
                c.drawPoint(20, 31, paint); c.drawPoint(30, 31, paint);
            } else if (kind == 1) {
                c.drawRoundRect(new RectF(15, 11, 35, 39), 3, 3, paint);
                c.drawCircle(29, 26, 1, paint); c.drawLine(20, 16, 20, 34, paint);
            } else if (kind == 2) {
                c.drawRoundRect(new RectF(10, 15, 40, 35), 4, 4, paint);
                c.drawLine(11, 22, 39, 22, paint); c.drawLine(16, 29, 23, 29, paint);
            } else {
                c.drawCircle(20, 20, 7, paint); c.drawLine(25, 25, 37, 37, paint);
                c.drawLine(32, 32, 36, 28, paint); c.drawLine(36, 36, 40, 32, paint);
            }
            c.restore();
        }
    }
}
