package top.suzuki.sukawallet;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.util.function.IntConsumer;

/** Single-choice overlay: whole rows with a trailing checkmark, no platform radio circles. */
final class ChoicePopup {
    static PopupWindow show(Activity activity,View anchor,int[] labels,int selected,IntConsumer choose){
        UiStyle ui=UiStyle.of(activity);float density=activity.getResources().getDisplayMetrics().density;
        LinearLayout panel=new LinearLayout(activity);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(0,(int)(8*density),0,(int)(8*density));
        GradientDrawable bg=new GradientDrawable();bg.setColor(ui.material?ui.surface:ui.dark?0xFF2A2A2A:0xFFFFFFFF);bg.setCornerRadius((ui.material?24:16)*density);panel.setBackground(bg.getConstantState().newDrawable().mutate());panel.setClipToOutline(true);
        int width=Math.min((int)(360*density),activity.getResources().getDisplayMetrics().widthPixels-(int)(32*density));
        android.widget.ScrollView scroll=new android.widget.ScrollView(activity);scroll.setFillViewport(false);scroll.setBackground(bg.getConstantState().newDrawable().mutate());scroll.setClipToOutline(true);scroll.addView(panel,new android.widget.ScrollView.LayoutParams(-1,-2));
        PopupWindow popup=new PopupWindow(scroll,width,android.view.ViewGroup.LayoutParams.WRAP_CONTENT,true);popup.setBackgroundDrawable(bg.getConstantState().newDrawable().mutate());popup.setOutsideTouchable(true);popup.setElevation(12*density);popup.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        for(int i=0;i<labels.length;i++){
            final int index=i;boolean checked=i==selected;
            LinearLayout row=new LinearLayout(activity){@Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(info);info.setClassName("android.widget.RadioButton");info.setCheckable(true);info.setChecked(checked);}};
            row.setGravity(Gravity.CENTER_VERTICAL);row.setMinimumHeight((int)(56*density));row.setPadding((int)(20*density),(int)(14*density),(int)(20*density),(int)(14*density));
            row.setBackground(new RippleDrawable(ColorStateList.valueOf(ui.dark?0x24FFFFFF:0x14000000),null,null));row.setContentDescription(activity.getString(labels[i]));row.setFocusable(true);
            TextView text=new TextView(activity);text.setText(labels[i]);text.setTextSize(ui.material?18:17);text.setTypeface(android.graphics.Typeface.create("sans-serif",0));text.setTextColor(ui.ink);row.addView(text,new LinearLayout.LayoutParams(0,-2,1));
            View check=new View(activity){private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);@Override protected void onDraw(Canvas canvas){if(!checked)return;float size=getWidth()/26f;canvas.save();canvas.scale(size,size);paint.setColor(ui.accent);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2.8f);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);Path path=new Path();path.moveTo(5,13);path.lineTo(10.5f,18.5f);path.lineTo(21,7);canvas.drawPath(path,paint);canvas.restore();}};
            check.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);LinearLayout.LayoutParams checkParams=new LinearLayout.LayoutParams((int)(26*density),(int)(26*density));checkParams.setMarginStart((int)(16*density));row.addView(check,checkParams);
            row.setOnClickListener(v->{popup.dismiss();choose.accept(index);});panel.addView(row,new LinearLayout.LayoutParams(-1,-2));
        }
        panel.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
        android.graphics.Insets bars=android.graphics.Insets.NONE;
        if(activity.getWindow().getDecorView().getRootWindowInsets()!=null)bars=activity.getWindow().getDecorView().getRootWindowInsets().getInsets(android.view.WindowInsets.Type.systemBars()|android.view.WindowInsets.Type.displayCutout());
        int margin=(int)(8*density),screenHeight=activity.getResources().getDisplayMetrics().heightPixels,screenWidth=activity.getResources().getDisplayMetrics().widthPixels;
        int top=bars.top+margin,bottom=screenHeight-bars.bottom-margin,height=Math.min(panel.getMeasuredHeight(),Math.max(1,bottom-top));popup.setHeight(height);
        int[] location=new int[2];anchor.getLocationOnScreen(location);int x=location[0]+anchor.getWidth()-width,y=location[1]+anchor.getHeight()+margin;
        if(y+height>bottom)y=location[1]-height-margin;y=Math.max(top,Math.min(y,bottom-height));x=Math.max(bars.left+margin,Math.min(x,screenWidth-bars.right-margin-width));
        popup.showAtLocation(activity.getWindow().getDecorView(),Gravity.TOP|Gravity.START,x,y);return popup;
    }
    private ChoicePopup(){}
}
