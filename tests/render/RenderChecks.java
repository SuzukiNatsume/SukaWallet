package top.suzuki.sukawallet;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;

public final class RenderChecks extends Instrumentation {
    private Bundle args;
    private volatile Activity opened;
    private volatile String expected;
    private volatile java.util.concurrent.CountDownLatch resumed;
    @Override public void callActivityOnResume(Activity activity){
        super.callActivityOnResume(activity);
        java.util.concurrent.CountDownLatch latch=resumed;
        if(latch!=null && activity.getClass().getName().equals(expected)){opened=activity;latch.countDown();}
    }
    private Activity open(Intent intent,Class<?> page)throws Exception{
        opened=null;expected=page.getName();resumed=new java.util.concurrent.CountDownLatch(1);
        runOnMainSync(()->getTargetContext().startActivity(intent));
        if(!resumed.await(15,java.util.concurrent.TimeUnit.SECONDS))throw new IllegalStateException("Activity resume timeout: "+page.getName());
        resumed=null;return opened;
    }
    @Override public void onCreate(Bundle args){this.args=args;super.onCreate(args);start();}
    @Override public void onStart(){
        Bundle result=new Bundle();JSONArray checks=new JSONArray();JSONArray failures=new JSONArray();
        try{
            if("true".equals(args.getString("gestures"))){runGestureChecks(result);finish(-1,result);return;}
            Class<?>[] pages={MainActivity.class,SettingsActivity.class,PatternActivity.class,SettingsActivity.class,PatternActivity.class,AboutActivity.class};
            for(String style:new String[]{"miuix","material"}){
                UiStyle.set(getTargetContext(),style);
                WalletApp.unlocked=true;
                for(int pageIndex=0;pageIndex<pages.length;pageIndex++){
                    Class<?> page=pages[pageIndex];
                    Auth.prefs(getTargetContext()).edit().putString("mode",pageIndex>=3?"pattern":"biometric").commit();
                    Intent intent=new Intent().setComponent(new ComponentName(getTargetContext().getPackageName(),page.getName())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_ANIMATION).putExtra("setup",pageIndex!=4);
                    long started=android.os.SystemClock.elapsedRealtime();
                    Activity activity=open(intent,page);waitForIdleSync();Thread.sleep(70);waitForIdleSync();
                    JSONObject entry=new JSONObject();entry.put("style",style);entry.put("page",page.getSimpleName());entry.put("variant",pageIndex==3?"pattern-enabled":pageIndex==4?"verify-pattern":"default");entry.put("startMs",android.os.SystemClock.elapsedRealtime()-started);
                    runOnMainSync(()->{try{JSONArray errors=new JSONArray();inspect(activity.findViewById(android.R.id.content),errors);
                        if(activity instanceof PatternActivity){
                            java.lang.reflect.Field field=PatternActivity.class.getDeclaredField("draw");field.setAccessible(true);View grid=(View)field.get(activity);
                            java.lang.reflect.Method x=grid.getClass().getDeclaredMethod("x",int.class),y=grid.getClass().getDeclaredMethod("y",int.class);x.setAccessible(true);y.setAccessible(true);
                            float spanX=(Float)x.invoke(grid,2)-(Float)x.invoke(grid,0),spanY=(Float)y.invoke(grid,6)-(Float)y.invoke(grid,0);entry.put("gridWidth",spanX);entry.put("gridHeight",spanY);
                            if(Math.abs(spanX-spanY)>.1f)errors.put("pattern-not-square");
                        }
                        boolean secure=(activity.getWindow().getAttributes().flags&android.view.WindowManager.LayoutParams.FLAG_SECURE)!=0;
                        if(secure!=(activity instanceof PatternActivity))errors.put("unexpected-capture-policy");
                        View back=findDescription(activity.findViewById(android.R.id.content),activity.getString(R.string.back));if(back!=null){int[] location=new int[2];back.getLocationOnScreen(location);entry.put("backBounds",new JSONArray(new int[]{location[0],location[1],back.getWidth(),back.getHeight()}));}
                        if(activity instanceof MainActivity){View menu=findDescription(activity.findViewById(android.R.id.content),activity.getString(R.string.settings));if(menu==null)errors.put("missing-more-button");else{int[] location=new int[2];menu.getLocationOnScreen(location);entry.put("moreBounds",new JSONArray(new int[]{location[0],location[1],menu.getWidth(),menu.getHeight()}));}}
entry.put("errors",errors);if(errors.length()>0)failures.put(entry);checks.put(entry);}catch(Exception e){throw new RuntimeException(e);}});
                    if("true".equals(args.getString("capture")) && page!=PatternActivity.class){
                        android.graphics.Bitmap shot=getUiAutomation().takeScreenshot();
                        if(shot!=null){try(java.io.FileOutputStream file=new java.io.FileOutputStream(new java.io.File(getTargetContext().getFilesDir(),style+"_"+page.getSimpleName()+".png"))){shot.compress(android.graphics.Bitmap.CompressFormat.PNG,100,file);}shot.recycle();}
                    }
                    if(page==SettingsActivity.class){
                        for(boolean appearance:new boolean[]{true,false}){
                            final android.widget.PopupWindow[] popup={null};
                            runOnMainSync(()->{View anchor=activity.findViewById(android.R.id.content).findViewWithTag(appearance?"appearanceChoice":"protectionChoice");popup[0]=ChoicePopup.show(activity,anchor,appearance?new int[]{R.string.style_miuix,R.string.style_material}:new int[]{R.string.off,R.string.biometric,R.string.pattern},appearance?(UiStyle.of(activity).material?1:0):1,index->{});});
                            waitForIdleSync();Thread.sleep(70);waitForIdleSync();
                            runOnMainSync(()->{try{JSONArray errors=entry.getJSONArray("errors");boolean clean=errors.length()==0;inspect(popup[0].getContentView(),errors);if(clean&&errors.length()>0)failures.put(entry);popup[0].dismiss();}catch(Exception e){throw new RuntimeException(e);}});waitForIdleSync();
                        }
                    }
                    runOnMainSync(()->activity.finish());waitForIdleSync();
                }
            }
            result.putString("checks",checks.toString());result.putString("failures",failures.toString());result.putInt("count",checks.length());result.putString("dpi",String.valueOf(getTargetContext().getResources().getConfiguration().densityDpi));result.putString("font",String.valueOf(getTargetContext().getResources().getConfiguration().fontScale));finish(failures.length()==0?-1:0,result);
        }catch(Exception e){result.putString("exception",e.toString());finish(0,result);}
    }
    private void runGestureChecks(Bundle result)throws Exception{
        Auth.prefs(getTargetContext()).edit().clear().commit();WalletApp.unlocked=true;
        Intent setupIntent=new Intent().setComponent(new ComponentName(getTargetContext().getPackageName(),PatternActivity.class.getName())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_ANIMATION).putExtra("setup",true);
        Activity setup=startActivitySync(setupIntent);waitForIdleSync();Thread.sleep(400);
        result.putBoolean("gesture_window_focus",setup.getWindow().getDecorView().hasWindowFocus());
        drawPattern(setup,new int[]{0,1});requireHint(setup,R.string.pattern_short);
        drawPattern(setup,new int[]{0,1,2,5,8});requireHint(setup,R.string.pattern_confirm);
        drawPattern(setup,new int[]{0,3,6,7,8});requireHint(setup,R.string.pattern_mismatch);
        drawPattern(setup,new int[]{0,1,2,5,8});requireHint(setup,R.string.pattern_confirm);
        drawPattern(setup,new int[]{0,1,2,5,8});
        long timeout=android.os.SystemClock.uptimeMillis()+5000;while(!setup.isFinishing() && android.os.SystemClock.uptimeMillis()<timeout)Thread.sleep(100);
        if(!setup.isFinishing() || !Auth.mode(getTargetContext()).equals("pattern"))throw new IllegalStateException("Pattern setup did not finish");
        result.putBoolean("setup_and_confirmation",true);
        WalletApp.unlocked=false;
        Activity verify=startActivitySync(new Intent(setupIntent).putExtra("setup",false));waitForIdleSync();Thread.sleep(400);
        drawPattern(verify,new int[]{0,3,6,7,8});requireHint(verify,R.string.pattern_wrong);result.putBoolean("wrong_pattern_rejected",true);
        drawPattern(verify,new int[]{0,1,2,5,8});timeout=android.os.SystemClock.uptimeMillis()+5000;while(!verify.isFinishing() && android.os.SystemClock.uptimeMillis()<timeout)Thread.sleep(100);
        if(!verify.isFinishing() || !WalletApp.unlocked)throw new IllegalStateException("Correct pattern did not unlock");
        result.putBoolean("correct_pattern_unlocks",true);
        Auth.prefs(getTargetContext()).edit().clear().commit();
    }
    private void requireHint(Activity activity,int id)throws Exception{
        long timeout=android.os.SystemClock.uptimeMillis()+5000;final boolean[] matched={false};
        while(android.os.SystemClock.uptimeMillis()<timeout){
            runOnMainSync(()->{try{java.lang.reflect.Field hint=PatternActivity.class.getDeclaredField("hint");hint.setAccessible(true);matched[0]=((TextView)hint.get(activity)).getText().toString().equals(activity.getString(id));}catch(Exception e){throw new RuntimeException(e);}});
            if(matched[0])return;Thread.sleep(100);
        }
        throw new IllegalStateException("Expected pattern hint did not appear: "+id);
    }
    private void drawPattern(Activity activity,int[] pattern)throws Exception{
        final float[][] positions=new float[pattern.length][2];
        runOnMainSync(()->{try{
            java.lang.reflect.Field field=PatternActivity.class.getDeclaredField("draw");field.setAccessible(true);View grid=(View)field.get(activity);int[] origin=new int[2];grid.getLocationOnScreen(origin);
            java.lang.reflect.Method x=grid.getClass().getDeclaredMethod("x",int.class),y=grid.getClass().getDeclaredMethod("y",int.class);x.setAccessible(true);y.setAccessible(true);
            for(int i=0;i<pattern.length;i++){positions[i][0]=origin[0]+(Float)x.invoke(grid,pattern[i]);positions[i][1]=origin[1]+(Float)y.invoke(grid,pattern[i]);}
        }catch(Exception e){throw new RuntimeException(e);}});
        long down=android.os.SystemClock.uptimeMillis();
        for(int i=0;i<positions.length;i++){
            android.view.MotionEvent event=android.view.MotionEvent.obtain(down,android.os.SystemClock.uptimeMillis(),i==0?android.view.MotionEvent.ACTION_DOWN:android.view.MotionEvent.ACTION_MOVE,positions[i][0],positions[i][1],0);event.setSource(android.view.InputDevice.SOURCE_TOUCHSCREEN);runOnMainSync(()->activity.dispatchTouchEvent(event));event.recycle();Thread.sleep(40);
        }
        android.view.MotionEvent event=android.view.MotionEvent.obtain(down,android.os.SystemClock.uptimeMillis(),android.view.MotionEvent.ACTION_UP,positions[positions.length-1][0],positions[positions.length-1][1],0);event.setSource(android.view.InputDevice.SOURCE_TOUCHSCREEN);runOnMainSync(()->activity.dispatchTouchEvent(event));event.recycle();waitForIdleSync();Thread.sleep(100);
    }

    private View findDescription(View view,String label){
        if(label.contentEquals(view.getContentDescription()==null?"":view.getContentDescription()))return view;
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){View result=findDescription(((ViewGroup)view).getChildAt(i),label);if(result!=null)return result;}return null;
    }
    private void inspect(View view,JSONArray errors)throws Exception{
        if(view==null || view.getVisibility()!=View.VISIBLE)return;
        if(view instanceof TextView){
            TextView text=(TextView)view;android.text.Layout layout=text.getLayout();
            if(text.getText().length()>0 && layout!=null){
                int space=text.getHeight()-text.getCompoundPaddingTop()-text.getCompoundPaddingBottom();
                if(layout.getLineCount()>0 && layout.getLineBottom(layout.getLineCount()-1)>space+1)errors.put("clipped-text:"+text.getText());
                for(int i=0;i<layout.getLineCount();i++)if(layout.getEllipsisCount(i)>0)errors.put("ellipsized:"+text.getText());
            }
        }
        if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++){
            View child=group.getChildAt(i);if(child.getVisibility()!=View.VISIBLE)continue;
            if(child.getLeft()<-1 || child.getRight()>group.getWidth()+1)errors.put("horizontal-overflow:"+child.getClass().getSimpleName());
            if(!(group instanceof android.widget.ScrollView) && child.getBottom()>group.getHeight()+1)errors.put("vertical-overflow:"+child.getClass().getSimpleName());
            inspect(child,errors);
        }}
    }
}
