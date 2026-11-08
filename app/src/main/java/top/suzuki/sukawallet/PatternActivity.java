package top.suzuki.sukawallet;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Full-screen local pattern setup/verification. Stores a salted derived hash, never the pattern. */
public final class PatternActivity extends Activity {
    private boolean setup;
    private String first;
    private TextView hint;
    private PatternView draw;
    @Override public void onCreate(Bundle state){
        UiStyle ui=UiStyle.of(this);
        boolean dark=ui.dark;
        if(dark)setTheme(android.R.style.Theme_Material_NoActionBar);
        super.onCreate(state);getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setup=getIntent().getBooleanExtra("setup",false);
        if(setup && !Auth.mode(this).equals("off") && !WalletApp.unlocked){finish();return;}
        if(!setup && !Auth.mode(this).equals("pattern")){finish();return;}
        int ink=ui.ink,muted=ui.muted;
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setBackgroundColor(ui.background);
        body.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets bars=insets.getInsets(android.view.WindowInsets.Type.systemBars()|android.view.WindowInsets.Type.displayCutout());v.setPadding(dp(26)+bars.left,bars.top,dp(26)+bars.right,bars.bottom);return insets;});
        body.addView(Navigation.back(this, () -> finish()));
        TextView title=new TextView(this);title.setText(setup?getString(R.string.setup_pattern):getString(R.string.unlock));title.setTextSize(ui.material?28:30);title.setTextColor(ink);title.setTypeface(null,ui.material?Typeface.NORMAL:Typeface.BOLD);body.addView(title);
        hint=new TextView(this);hint.setText(setup?getString(R.string.pattern_start):getString(R.string.pattern_draw));hint.setTextSize(18);hint.setTextColor(muted);hint.setMinHeight(dp(85));hint.setPadding(0,dp(20),0,0);body.addView(hint);
        draw=new PatternView(this);draw.listener=value->submit(value);body.addView(draw,new LinearLayout.LayoutParams(-1,dp(320)));
        setContentView(body);
    }
    private static byte[] hash(String pattern,byte[] salt)throws Exception{
        PBEKeySpec spec=new PBEKeySpec(pattern.toCharArray(),salt,120000,256);
        try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}finally{spec.clearPassword();}
    }
    private void submit(String value){
        if(value.length()<4){hint.setText(getString(R.string.pattern_short));return;}
        if(setup && first==null){first=value;hint.setText(getString(R.string.pattern_confirm));return;}
        if(setup && !first.equals(value)){first=null;hint.setText(getString(R.string.pattern_mismatch));return;}
        SharedPreferences p=Auth.prefs(this);
        if(!setup && p.getLong("blockedUntil",0)>System.currentTimeMillis()){hint.setText(getString(R.string.pattern_wait));return;}
        draw.setEnabled(false);hint.setText(getString(R.string.verifying));
        new Thread(()->{
            try{
                byte[] salt=setup?new byte[16]:Base64.decode(p.getString("salt",""),Base64.NO_WRAP);
                if(setup)new SecureRandom().nextBytes(salt);
                byte[] digest=hash(value,salt);
                boolean passed=setup || MessageDigest.isEqual(digest,Base64.decode(p.getString("hash",""),Base64.NO_WRAP));
                // Commit only while the page is still active, so cancelling setup cannot silently enable a lock.
                runOnUiThread(()->{
                    if(isFinishing() || isDestroyed())return;
                    if(passed){
                        SharedPreferences.Editor edit=p.edit().putInt("failures",0).putLong("blockedUntil",0);
                        if(setup)edit.putString("salt",Base64.encodeToString(salt,Base64.NO_WRAP)).putString("hash",Base64.encodeToString(digest,Base64.NO_WRAP)).putString("mode","pattern");
                        if(!edit.commit()){hint.setText(getString(R.string.save_error));draw.setEnabled(true);return;}
                        first=null;WalletApp.unlocked=true;setResult(RESULT_OK);finish();
                    }else{
                        int failures=p.getInt("failures",0)+1;
                        p.edit().putInt("failures",failures%5).putLong("blockedUntil",failures>=5?System.currentTimeMillis()+30000:0).apply();
                        hint.setText(failures>=5?getString(R.string.pattern_timeout):getString(R.string.pattern_wrong));draw.setEnabled(true);
                    }
                });
            }catch(Exception e){runOnUiThread(()->{if(!isFinishing()){hint.setText(getString(R.string.pattern_error));draw.setEnabled(true);}});}
        },"pattern-verification").start();
    }
    private int dp(float n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
    private interface PatternListener{void done(String value);}
    private static final class PatternView extends View{
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final StringBuilder points=new StringBuilder();
        private final int accent;
        private final float density;
        PatternListener listener;
        PatternView(Context c){super(c);density=c.getResources().getDisplayMetrics().density;accent=UiStyle.of(c).accent;setContentDescription(c.getString(R.string.pattern_area));}
        private float side(){return Math.min(getWidth(),getHeight());}
        private float x(int p){float size=side();return (getWidth()-size)/2f+size*(.2f+(p%3)*.3f);}
        private float y(int p){float size=side();return (getHeight()-size)/2f+size*(.2f+(p/3)*.3f);}
        private void add(float px,float py){
            for(int i=0;i<9;i++)if(Math.hypot(px-x(i),py-y(i))<side()*.11f && points.indexOf(""+(char)('0'+i))<0){
                if(points.length()>0){int last=points.charAt(points.length()-1)-'0',dx=i%3-last%3,dy=i/3-last/3;
                    if((Math.abs(dx)==2 && (dy==0 || Math.abs(dy)==2)) || (Math.abs(dy)==2 && dx==0)){
                        int middle=(last+i)/2;if(points.indexOf(""+(char)('0'+middle))<0)points.append((char)('0'+middle));
                    }}
                points.append((char)('0'+i));invalidate();break;
            }
        }
        @Override public boolean onTouchEvent(MotionEvent e){
            if(!isEnabled())return false;
            int action=e.getActionMasked();
            if(action==MotionEvent.ACTION_DOWN){points.setLength(0);getParent().requestDisallowInterceptTouchEvent(true);}
            if(action==MotionEvent.ACTION_DOWN || action==MotionEvent.ACTION_MOVE){for(int i=0;i<e.getHistorySize();i++)add(e.getHistoricalX(i),e.getHistoricalY(i));add(e.getX(),e.getY());return true;}
            if(action==MotionEvent.ACTION_UP){String result=points.toString();points.setLength(0);invalidate();if(listener!=null)listener.done(result);performClick();return true;}
            if(action==MotionEvent.ACTION_CANCEL){points.setLength(0);invalidate();return true;}return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
        @Override protected void onDraw(Canvas c){super.onDraw(c);paint.setStrokeWidth(3*density);paint.setColor(accent);
            for(int i=1;i<points.length();i++){int a=points.charAt(i-1)-'0',b=points.charAt(i)-'0';c.drawLine(x(a),y(a),x(b),y(b),paint);}
            for(int i=0;i<9;i++){paint.setColor(points.indexOf(""+(char)('0'+i))>=0?accent:0xFF9199AC);c.drawCircle(x(i),y(i),5*density,paint);}
        }
    }
}
