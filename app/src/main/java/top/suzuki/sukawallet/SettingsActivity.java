package top.suzuki.sukawallet;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Launches official components. Account state stays inside the Xiaomi Account app. */
public final class SettingsActivity extends SecureActivity {
    private boolean dark;
    private UiStyle ui;
    private String renderedProtection;
    private int ink, muted, surface;
    private static final String PASSWORD_URL = "https://m.pay.xiaomi.com/home/passManage?from=webEntry";
    private static final String DEDUCTION_URL = "https://m.pay.xiaomi.com/deduct/recission?from=webEntry";

    @Override public void onCreate(Bundle state) {
        ui=UiStyle.of(this);dark=ui.dark;
        if(dark)setTheme(android.R.style.Theme_Material_NoActionBar);
        super.onCreate(state);ink=ui.ink;muted=ui.muted;surface=ui.surface;renderedProtection=Auth.mode(this);
        if(ui.material)renderPixelSettings();else renderSystemSettings();
        if(getWindow().getInsetsController()!=null){int flags=WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;getWindow().getInsetsController().setSystemBarsAppearance(dark?0:flags,flags);}
    }

    private void renderPixelSettings(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(ui.background);
        root.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return insets;});
        root.addView(Navigation.back(this,()->finish(),0));
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(16),0,dp(16),dp(24));scroll.addView(body);
        TextView title=text(getString(R.string.account_settings),32,ink);title.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));title.setPadding(dp(8),dp(12),dp(8),dp(28));body.addView(title);
        inlineGroup(body,null,true,true);
        pixelGroup(body,new int[]{R.string.account,R.string.payment_password},new int[]{1,2},new Runnable[]{()->openAccount(),()->openWeb(PASSWORD_URL)});
        inlineGroup(body,null,true,false);
        pixelGroup(body,new int[]{R.string.deduction,R.string.smart_settings},new int[]{4,5},new Runnable[]{()->openWeb(DEDUCTION_URL),()->launch(new Intent(Intent.ACTION_VIEW,Uri.parse("tsmclient://card?type=0&action=uni_settings")).setComponent(new ComponentName(Routes.PACKAGE,Routes.PACKAGE+".ui.MainActivity")))});
        pixelGroup(body,new int[]{R.string.about},new int[]{6},new Runnable[]{()->startActivity(new Intent(this,AboutActivity.class))});
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
    }
    private void pixelGroup(LinearLayout body,int[] labels,int[] icons,Runnable[] actions){
        for(int i=0;i<labels.length;i++){
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(16),dp(18),dp(24),dp(18));row.setMinimumHeight(dp(76));
            float top=dp(i==0?24:4),bottom=dp(i==labels.length-1?24:4);float[] radii={top,top,top,top,bottom,bottom,bottom,bottom};
            GradientDrawable bg=new GradientDrawable();bg.setColor(ui.surface);bg.setCornerRadii(radii);GradientDrawable mask=new GradientDrawable();mask.setColor(Color.WHITE);mask.setCornerRadii(radii);
            row.setBackground(new RippleDrawable(ColorStateList.valueOf(ui.dark?0x24FFFFFF:0x16000000),bg,mask));
            row.addView(new PreferenceIcon(icons[i]),new LinearLayout.LayoutParams(dp(40),dp(40)));
            TextView label=text(getString(labels[i]),20,ui.ink);label.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));label.setPadding(dp(16),0,0,0);row.addView(label,new LinearLayout.LayoutParams(0,-2,1));
            row.setContentDescription(getString(labels[i]));row.setFocusable(true);final Runnable action=actions[i];row.setOnClickListener(v->action.run());LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);params.bottomMargin=dp(i==labels.length-1?16:2);body.addView(row,params);
        }
    }
    private final class PreferenceIcon extends View {
        private final int kind;private final android.graphics.Paint paint=new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        PreferenceIcon(int kind){super(SettingsActivity.this);this.kind=kind;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(android.graphics.Canvas c){
            super.onDraw(c);c.save();c.scale(getWidth()/40f,getHeight()/40f);paint.setStyle(android.graphics.Paint.Style.FILL);paint.setColor(ui.accent);paint.setAlpha(ui.dark?40:24);c.drawCircle(20,20,20,paint);paint.setAlpha(255);paint.setStrokeWidth(1.8f);paint.setStrokeCap(android.graphics.Paint.Cap.ROUND);paint.setStrokeJoin(android.graphics.Paint.Join.ROUND);paint.setStyle(android.graphics.Paint.Style.STROKE);
            if(kind==0){c.drawCircle(20,20,10,paint);c.drawCircle(17,16,1,paint);c.drawCircle(23,15,1,paint);c.drawCircle(14,22,1,paint);}
            else if(kind==1){c.drawCircle(20,15,4,paint);c.drawArc(new android.graphics.RectF(12,22,28,33),180,180,false,paint);}
            else if(kind==2){c.drawRoundRect(new android.graphics.RectF(12,18,28,30),3,3,paint);c.drawArc(new android.graphics.RectF(16,10,24,26),180,180,false,paint);c.drawPoint(20,24,paint);}
            else if(kind==3){android.graphics.Path path=new android.graphics.Path();path.moveTo(20,9);path.lineTo(29,13);path.lineTo(28,23);path.quadTo(26,28,20,32);path.quadTo(14,28,12,23);path.lineTo(11,13);path.close();c.drawPath(path,paint);c.drawLine(16,20,19,23,paint);c.drawLine(19,23,25,17,paint);}
            else if(kind==4){c.drawArc(new android.graphics.RectF(10,10,30,30),-70,280,false,paint);c.drawLine(28,12,28,18,paint);c.drawLine(28,18,22,17,paint);}
            else if(kind==5){c.drawRoundRect(new android.graphics.RectF(9,12,31,28),3,3,paint);c.drawLine(10,18,30,18,paint);c.drawLine(14,23,20,23,paint);}
            else{c.drawCircle(20,20,10,paint);c.drawPoint(20,15,paint);c.drawLine(20,19,20,26,paint);}c.restore();
        }
    }

    private void renderSystemSettings(){
        SettingsLook look=new SettingsLook(this);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(ui.dark?0xFF000000:0xFFF7F7F7);
        root.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return insets;});
        root.addView(Navigation.back(this,()->finish(),0));
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipToPadding(false);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(look.px(12),0,look.px(12),look.px(24));scroll.addView(body);
        TextView title=new TextView(this);title.setText(R.string.account_settings);look.title(title);title.setPadding(0,look.px(4),0,look.px(10));
        LinearLayout.LayoutParams titleParams=new LinearLayout.LayoutParams(-1,-2);titleParams.setMarginStart(look.px(14));titleParams.setMarginEnd(look.px(14));titleParams.bottomMargin=look.px(12);body.addView(title,titleParams);
        inlineGroup(body,look,false,true);
        systemGroup(body,look,new int[]{R.string.account,R.string.payment_password},new Runnable[]{()->openAccount(),()->openWeb(PASSWORD_URL)});
        inlineGroup(body,look,false,false);
        systemGroup(body,look,new int[]{R.string.deduction,R.string.smart_settings},new Runnable[]{()->openWeb(DEDUCTION_URL),()->launch(new Intent(Intent.ACTION_VIEW,Uri.parse("tsmclient://card?type=0&action=uni_settings")).setComponent(new ComponentName(Routes.PACKAGE,Routes.PACKAGE+".ui.MainActivity")))});
        systemGroup(body,look,new int[]{R.string.about},new Runnable[]{()->startActivity(new Intent(this,AboutActivity.class))});
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
    }
    private void systemGroup(LinearLayout body,SettingsLook look,int[] labels,Runnable[] actions){
        LinearLayout group=new LinearLayout(this);group.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable background=new GradientDrawable();background.setColor(ui.dark?0xFF242424:0xFFFFFFFF);background.setCornerRadius(look.px(16));group.setBackground(background);group.setClipToOutline(true);
        for(int i=0;i<labels.length;i++){
            LinearLayout item=new LinearLayout(this);item.setGravity(Gravity.CENTER_VERTICAL);item.setMinimumHeight(look.px(56));item.setPadding(look.px(16),look.px(14),look.px(16),look.px(14));
            item.setBackground(new RippleDrawable(ColorStateList.valueOf(ui.dark?0x14FFFFFF:0x10000000),null,null));item.setFocusable(true);item.setContentDescription(getString(labels[i]));final Runnable action=actions[i];item.setOnClickListener(v->action.run());
            TextView label=new TextView(this);label.setText(labels[i]);look.row(label);item.addView(label,new LinearLayout.LayoutParams(0,-2,1));
            android.widget.ImageView arrow=new android.widget.ImageView(this);android.graphics.drawable.Drawable glyph=look.arrow();
            if(glyph!=null)arrow.setImageDrawable(glyph);else{arrow.setImageResource(R.drawable.ic_back);arrow.setRotation(180);}
            arrow.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);LinearLayout.LayoutParams arrowParams=new LinearLayout.LayoutParams(look.px(10),look.px(16));arrowParams.setMarginStart(look.px(12));item.addView(arrow,arrowParams);
            group.addView(item,new LinearLayout.LayoutParams(-1,-2));
        }
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);params.bottomMargin=look.px(12);body.addView(group,params);
    }

    @Override protected void onResume(){
        super.onResume();if(renderedProtection!=null && !renderedProtection.equals(Auth.mode(this)) && !isFinishing())recreate();
    }
    private void inlineGroup(LinearLayout body,SettingsLook look,boolean pixel,boolean appearance){
        LinearLayout group=new LinearLayout(this);group.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg=new GradientDrawable();bg.setColor(pixel?ui.surface:ui.dark?0xFF242424:0xFFFFFFFF);bg.setCornerRadius(pixel?dp(24):look.px(16));group.setBackground(bg);group.setClipToOutline(true);
        LinearLayout row=new LinearLayout(this);row.setTag(appearance?"appearanceChoice":"protectionChoice");row.setGravity(Gravity.CENTER_VERTICAL);row.setMinimumHeight(pixel?dp(76):look.px(56));
        int horizontal=pixel?dp(16):look.px(16),vertical=pixel?dp(18):look.px(14);row.setPadding(horizontal,vertical,horizontal,vertical);row.setBackground(new RippleDrawable(ColorStateList.valueOf(ui.dark?0x24FFFFFF:0x14000000),null,null));
        if(pixel)row.addView(new PreferenceIcon(appearance?0:3),new LinearLayout.LayoutParams(dp(40),dp(40)));
        TextView name=new TextView(this);name.setText(appearance?R.string.appearance:R.string.protection);if(pixel){name.setTextSize(20);name.setTextColor(ui.ink);name.setPadding(dp(16),0,0,0);}else look.row(name);
        row.addView(name,new LinearLayout.LayoutParams(0,-2,1));
        String mode=Auth.mode(this);int current=appearance?(ui.material?R.string.style_material:R.string.style_miuix):mode.equals("pattern")?R.string.pattern:mode.equals("biometric")?R.string.biometric_short:R.string.off;
        TextView value=new TextView(this);value.setText(current);value.setTextColor(ui.muted);value.setTextSize(pixel?16:17);value.setMaxWidth((int)(getResources().getDisplayMetrics().widthPixels*.36f));value.setPadding(dp(12),0,0,0);row.addView(value,new LinearLayout.LayoutParams(-2,-2));
        android.widget.ImageView arrow=new android.widget.ImageView(this);arrow.setImageResource(R.drawable.ic_dropdown);arrow.setImageTintList(ColorStateList.valueOf(ui.muted));arrow.setScaleType(android.widget.ImageView.ScaleType.CENTER);arrow.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams arrowParams=new LinearLayout.LayoutParams(dp(20),dp(20));arrowParams.setMarginStart(dp(8));row.addView(arrow,arrowParams);
        row.setContentDescription(getString(appearance?R.string.appearance:R.string.protection)+", "+getString(current));row.setFocusable(true);
        row.setOnClickListener(v->{
            android.widget.PopupWindow popup;
            if(appearance)popup=ChoicePopup.show(this,row,new int[]{R.string.style_miuix,R.string.style_material},ui.material?1:0,index->{String next=index==1?"material":"miuix";if(!UiStyle.mode(this).equals(next)){UiStyle.set(this,next);recreate();}});
            else popup=ChoicePopup.show(this,row,new int[]{R.string.off,R.string.biometric,R.string.pattern},mode.equals("pattern")?2:mode.equals("biometric")?1:0,index->setProtection(index));
            arrow.setRotation(180);popup.setOnDismissListener(()->arrow.setRotation(0));
        });
        group.addView(row,new LinearLayout.LayoutParams(-1,-2));
        if(!appearance && mode.equals("pattern")){
            TextView change=new TextView(this);change.setText(R.string.change_pattern);if(pixel){change.setTextSize(20);change.setTextColor(ui.ink);}else look.row(change);change.setGravity(Gravity.CENTER_VERTICAL);change.setPadding(horizontal,vertical,horizontal,vertical);change.setMinHeight(pixel?dp(64):look.px(56));change.setFocusable(true);change.setBackground(new RippleDrawable(ColorStateList.valueOf(ui.dark?0x24FFFFFF:0x14000000),null,null));change.setOnClickListener(v->setupPattern());group.addView(change,new LinearLayout.LayoutParams(-1,-2));
        }
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);params.bottomMargin=pixel?dp(16):look.px(12);body.addView(group,params);
    }
    private void setProtection(int index){
        String next=index==0?"off":index==1?"biometric":"pattern";if(Auth.mode(this).equals(next))return;
        if(index==0){Auth.prefs(this).edit().clear().apply();WalletApp.unlocked=true;recreate();}
        else if(index==1)Auth.biometric(this,()->{Auth.prefs(this).edit().putString("mode","biometric").remove("hash").remove("salt").apply();WalletApp.unlocked=true;recreate();},()->{});
        else setupPattern();
    }
    private void setupPattern(){startActivity(new Intent(this,PatternActivity.class).putExtra("setup",true));}

    private void openAccount() {
        launch(new Intent("com.xiaomi.account.action.MI_ACCOUNT_SETTINGS").setPackage("com.xiaomi.account"));
    }
    private void openWeb(String url) {
        launch(new Intent().setComponent(new ComponentName("com.xiaomi.account", "com.xiaomi.account.ui.PassportJsbWebViewActivity"))
                .putExtra("url",url)
                .putExtra("cookie_fill_cookie_set_url","https://m.mipay.com")
                .putExtra("cookie_fill_login_service_id","mipaycom")
                .putExtra("cookie_fill_account_device_params",true)
                .putExtra("action_bar_style","native")
                .putExtra("action_bar_title",PASSWORD_URL.equals(url)?getString(R.string.payment_password):getString(R.string.deduction_title)));
    }
    private void launch(Intent intent) {
        try { startActivity(intent); }
        catch (ActivityNotFoundException | SecurityException e) {
            error(getString(R.string.component_error));
        }
    }
    private void error(String message) {
        new AlertDialog.Builder(this).setTitle(getString(R.string.cannot_open)).setMessage(message).setPositiveButton(getString(R.string.ok), null).show();
    }
    private void row(LinearLayout body, String title, Runnable action) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(18), dp(17), dp(18), dp(17));
        row.setMinimumHeight(dp(83));
        GradientDrawable shape = new GradientDrawable(); shape.setColor(surface); shape.setCornerRadius(dp(ui.material?16:20));
        GradientDrawable mask = new GradientDrawable(); mask.setColor(Color.WHITE); mask.setCornerRadius(dp(ui.material?16:20));
        row.setBackground(new RippleDrawable(ColorStateList.valueOf(dark ? 0x33FFFFFF : 0x184964DF), shape, mask));
        TextView name = text(ui.material?title:title + "  ›", 17, ink); name.setTypeface(null, ui.material?Typeface.NORMAL:Typeface.BOLD); row.addView(name);
        row.setFocusable(true); row.setClickable(true); row.setContentDescription(title);
        row.setOnClickListener(v -> action.run());
        body.addView(row, new LinearLayout.LayoutParams(-1, -2));
        gap(body, ui.material?4:12);
    }
    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color); return t;
    }
    private void gap(LinearLayout body, int height) { body.addView(new View(this), new LinearLayout.LayoutParams(1, dp(height))); }
    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + .5f); }
}
