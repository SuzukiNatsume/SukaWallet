package top.suzuki.sukawallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

/** One-time, same-signature import from the previous application ID. No account or card data. */
final class Migration {
    static void importOnce(Context c){
        SharedPreferences state=c.getSharedPreferences("migration",0);
        if(state.getBoolean("complete",false))return;
        try{
            c.getPackageManager().getApplicationInfo("dev.minicard.wallet",0);
            if(c.getPackageManager().checkSignatures("dev.minicard.wallet",c.getPackageName())!=PackageManager.SIGNATURE_MATCH)return;
            Bundle data=c.getContentResolver().call(Uri.parse("content://dev.minicard.wallet.migration"),"migrate",null,null);
            if(data==null)return;
            for(String name:new String[]{"appearance","protection"}){
                Bundle values=data.getBundle(name);if(values==null)return;
                SharedPreferences.Editor edit=c.getSharedPreferences(name,0).edit();
                for(String key:values.keySet()){
                    Object value=values.get(key);
                    if(value instanceof String)edit.putString(key,(String)value);
                    else if(value instanceof Integer)edit.putInt(key,(Integer)value);
                    else if(value instanceof Long)edit.putLong(key,(Long)value);
                    else if(value instanceof Boolean)edit.putBoolean(key,(Boolean)value);
                }
                if(!edit.commit())return;
            }
            state.edit().putBoolean("complete",true).commit();
            android.util.Log.i("SukaWalletMigration","Previous app settings imported");
        }catch(PackageManager.NameNotFoundException e){state.edit().putBoolean("complete",true).commit();}
        catch(IllegalArgumentException | SecurityException e){
            // Older installations without the temporary bridge can still start normally.
            android.util.Log.w("SukaWalletMigration","Previous app import unavailable");
        }
    }
    private Migration(){}
}
