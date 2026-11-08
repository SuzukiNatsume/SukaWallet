package top.suzuki.sukawallet;

import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Local about and license page; external profile opens in the user's chosen app. */
public final class AboutActivity extends SecureActivity {
    private UiStyle ui;
    @Override public void onCreate(Bundle state){
        ui=UiStyle.of(this);
        if(ui.dark)setTheme(android.R.style.Theme_Material_NoActionBar);
        super.onCreate(state);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(ui.background);
        root.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return insets;});
        root.addView(Navigation.back(this,()->finish(),0));
        ScrollView scroll=new ScrollView(this);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(26),dp(4),dp(26),dp(32));scroll.addView(body);
        TextView title=text(getString(R.string.about),32,ui.ink);if(!ui.material)new SettingsLook(this).title(title);body.addView(title);
        ImageView icon=new ImageView(this);icon.setImageResource(R.mipmap.ic_launcher);icon.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams iconParams=new LinearLayout.LayoutParams(dp(80),dp(80));iconParams.gravity=Gravity.CENTER_HORIZONTAL;iconParams.topMargin=dp(28);iconParams.bottomMargin=dp(16);body.addView(icon,iconParams);
        TextView name=text("SukaWallet",26,ui.ink);name.setGravity(Gravity.CENTER);body.addView(name);
        String version="";try{version=getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(android.content.pm.PackageManager.NameNotFoundException ignored){}
        TextView versionView=text(version,16,ui.muted);versionView.setGravity(Gravity.CENTER);body.addView(versionView);
        TextView author=text(getString(R.string.author)+" · GitHub",18,ui.ink);author.setPadding(0,dp(32),0,dp(8));body.addView(author);
        TextView profile=text("SuzukiNatsume",20,ui.accent);profile.setPadding(0,dp(12),0,dp(12));profile.setMinHeight(dp(48));profile.setFocusable(true);profile.setContentDescription("SuzukiNatsume, GitHub");
        android.content.res.TypedArray attrs=obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground});profile.setBackground(attrs.getDrawable(0));attrs.recycle();
        profile.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/SuzukiNatsume")));}catch(ActivityNotFoundException e){Toast.makeText(this,R.string.component_error,Toast.LENGTH_SHORT).show();}});body.addView(profile);
        TextView licenseTitle=text("MIT License",22,ui.ink);licenseTitle.setPadding(0,dp(28),0,dp(12));body.addView(licenseTitle);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
        if(getWindow().getInsetsController()!=null){int flags=WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;getWindow().getInsetsController().setSystemBarsAppearance(ui.dark?0:flags,flags);}
    }
    private TextView text(String value,int size,int color){TextView view=new TextView(this);view.setText(value);view.setTextSize(size);view.setTextColor(color);view.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));return view;}
    private int dp(float value){return (int)(value*getResources().getDisplayMetrics().density+.5f);}
}
