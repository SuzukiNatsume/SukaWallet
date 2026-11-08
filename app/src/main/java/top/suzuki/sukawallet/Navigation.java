package top.suzuki.sukawallet;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;
import android.widget.LinearLayout;

final class Navigation {
    static View back(Activity activity,Runnable action){return back(activity,action,26);}
    static View back(Activity activity,Runnable action,float parentPaddingDp){
        SettingsLook look=new SettingsLook(activity);
        View button=new BackGlyph(activity,look,false);
        button.setContentDescription(activity.getString(R.string.back));button.setClickable(true);button.setFocusable(true);
        TypedArray attrs=activity.obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackgroundBorderless});
        button.setBackground(attrs.getDrawable(0));attrs.recycle();
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams((int)(48*look.unit),look.px(40));
        params.topMargin=look.px(8);params.bottomMargin=look.px(8);
        params.setMarginStart(look.px(20)-(int)(parentPaddingDp*activity.getResources().getDisplayMetrics().density+.5f));
        button.setLayoutParams(params);button.setOnClickListener(v->action.run());return button;
    }
    static View more(Activity activity){
        View button=new BackGlyph(activity,new SettingsLook(activity),true);
        button.setContentDescription(activity.getString(R.string.more_options));button.setClickable(true);button.setFocusable(true);
        TypedArray attrs=activity.obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackgroundBorderless});button.setBackground(attrs.getDrawable(0));attrs.recycle();return button;
    }
    private static final class BackGlyph extends View {
        private final SettingsLook look;
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final String glyph;
        private final boolean more;
        private final android.graphics.drawable.Drawable fallback;
        BackGlyph(Activity activity,SettingsLook look,boolean more){
            super(activity);this.look=look;this.more=more;glyph=new String(Character.toChars(more?0xF0003:0xF000A));paint.setTypeface(look.symbols());paint.setTextSize(21*look.unit*(more?796f/869f:1f));
            paint.setColor(UiStyle.of(activity).dark?0xF2FFFFFF:0xFF000000);
            fallback=activity.getDrawable(R.drawable.ic_back);fallback.setTint(UiStyle.of(activity).ink);
        }
        @Override public void onInitializeAccessibilityNodeInfo(android.view.accessibility.AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(info);info.setClassName("android.widget.ImageButton");}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);float width=look.px(40);
            if(look.symbols()!=null){
                c.save();if(more)c.translate(getWidth()-width,0);if(!more && getLayoutDirection()==View.LAYOUT_DIRECTION_RTL)c.scale(-1,1,width/2,getHeight()/2f);
                Paint.FontMetrics metrics=paint.getFontMetrics();c.drawText(glyph,(width-paint.measureText(glyph))/2,(getHeight()-metrics.ascent-metrics.descent)/2,paint);c.restore();
            }else if(more){paint.setColor(UiStyle.of((Activity)getContext()).ink);float x=look.px(28),y=getHeight()/2f;for(int i=-1;i<=1;i++)c.drawCircle(x,y+i*6.858f*look.unit,1.5f*look.unit,paint);}
            else{int side=look.px(24);int x=(int)((width-side)/2),y=(getHeight()-side)/2;fallback.setBounds(x,y,x+side,y+side);fallback.draw(c);}
        }
    }
    private Navigation(){}
}
