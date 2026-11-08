package top.suzuki.sukawallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class SecureActivity extends Activity {
    private static final int UNLOCK_PATTERN=831;
    private boolean checking;
    private String createdStyle;
    @Override protected void onCreate(Bundle state) {
        createdStyle=UiStyle.mode(this);
        super.onCreate(state);
        if(!Auth.mode(this).equals("off") && !WalletApp.unlocked)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }
    @Override protected void onResume() {
        super.onResume();
        if(!UiStyle.mode(this).equals(createdStyle)){recreate();return;}
        View content=findViewById(android.R.id.content);
        if(Auth.mode(this).equals("off") || WalletApp.unlocked){showUnlockedContent();return;}
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        content.setVisibility(View.INVISIBLE);
        if(checking)return;
        checking=true;
        if(Auth.mode(this).equals("pattern"))startActivityForResult(new Intent(this,PatternActivity.class),UNLOCK_PATTERN);
        else Auth.biometric(this,()->{checking=false;WalletApp.unlocked=true;showUnlockedContent();},()->{checking=false;finishAffinity();});
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request==UNLOCK_PATTERN){checking=false;if(result==RESULT_OK){WalletApp.unlocked=true;showUnlockedContent();}else finishAffinity();}
    }
    private void showUnlockedContent(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        findViewById(android.R.id.content).setVisibility(View.VISIBLE);
    }
}
