package top.suzuki.sukawallet;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.widget.TextView;

/** Settings geometry calibrated against this phone at 320/384/420/480 DPI. */
final class SettingsLook {
    private static Typeface symbols;
    final Activity activity;
    final float unit;
    final Context system;
    SettingsLook(Activity activity) {
        this.activity=activity;
        // Xiaomi Settings AutoDensity runs at approximately 1.03125 times the display override.
        unit=activity.getResources().getDisplayMetrics().density*1.03125f;
        Context context=null;
        try{
            Context original=activity.createPackageContext("com.android.settings",0);
            Configuration config=new Configuration(activity.getResources().getConfiguration());
            config.densityDpi=Math.round(config.densityDpi*1.03125f);
            context=original.createConfigurationContext(config);
        }catch(Exception ignored){}
        system=context;
    }
    int px(float dp){return Math.round(dp*unit);}
    float sp(float sp){
        if(system!=null)return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,sp,system.getResources().getDisplayMetrics());
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,sp,activity.getResources().getDisplayMetrics())*1.03125f;
    }
    Typeface symbols(){
        if(symbols==null && system!=null)try{symbols=Typeface.createFromAsset(system.getAssets(),"fonts/misymbol_vf.ttf");}catch(Exception ignored){}
        return symbols;
    }
    Drawable arrow(){
        if(system!=null)try{
            Resources r=system.getResources();String name=UiStyle.of(activity).dark?"miuix_appcompat_arrow_right_dark":"miuix_appcompat_arrow_right_light";
            int id=r.getIdentifier(name,"drawable","com.android.settings");if(id!=0)return r.getDrawable(id,system.getTheme()).mutate();
        }catch(Exception ignored){}
        return null;
    }
    void title(TextView view){
        if(system!=null)try{
            int id=system.getResources().getIdentifier("Miuix.AppCompat.TextAppearance.WindowTitle.Expand","style","com.android.settings");
            if(id!=0)view.setTextAppearance(system,id);
        }catch(Exception ignored){}
        view.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX,sp(32));
        view.setTextColor(UiStyle.of(activity).dark?0xF2FFFFFF:0xFF000000);
    }
    void row(TextView view){
        view.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));view.setTextSize(TypedValue.COMPLEX_UNIT_PX,sp(17));
        view.setTextColor(UiStyle.of(activity).dark?0xF2FFFFFF:0xFF000000);
    }
}
