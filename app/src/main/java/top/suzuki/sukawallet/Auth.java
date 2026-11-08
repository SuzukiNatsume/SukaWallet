package top.suzuki.sukawallet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.CancellationSignal;

final class Auth {
    static SharedPreferences prefs(Context c) { return c.getSharedPreferences("protection",Context.MODE_PRIVATE); }
    static String mode(Context c) { return prefs(c).getString("mode","off"); }
    static void biometric(Activity a,Runnable success,Runnable cancel) {
        int allowed=BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL;
        BiometricManager manager=a.getSystemService(BiometricManager.class);
        if(manager==null || manager.canAuthenticate(allowed)!=BiometricManager.BIOMETRIC_SUCCESS) {cancel.run();return;}
        BiometricPrompt prompt=new BiometricPrompt.Builder(a).setTitle(a.getString(R.string.unlock))
            .setAllowedAuthenticators(allowed).build();
        prompt.authenticate(new CancellationSignal(),a.getMainExecutor(),new BiometricPrompt.AuthenticationCallback(){
            @Override public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result){success.run();}
            @Override public void onAuthenticationError(int code,CharSequence message){cancel.run();}
        });
    }
    private Auth(){}
}
