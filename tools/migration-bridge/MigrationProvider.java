package dev.minicard.wallet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Process;
import java.util.Map;

/** Temporary bridge. Only the renamed app with our existing signing key can read settings. */
public final class MigrationProvider extends ContentProvider {
    @Override public boolean onCreate(){return true;}
    @Override public Bundle call(String method,String arg,Bundle extras){
        if(!"migrate".equals(method) || !"top.suzuki.sukawallet".equals(getCallingPackage()) ||
           getContext().getPackageManager().checkSignatures(Binder.getCallingUid(),Process.myUid())!=PackageManager.SIGNATURE_MATCH)
            throw new SecurityException("Migration caller not allowed");
        Bundle result=new Bundle();
        for(String name:new String[]{"appearance","protection"}){
            Bundle values=new Bundle();
            for(Map.Entry<String,?> entry:getContext().getSharedPreferences(name,0).getAll().entrySet()){
                Object value=entry.getValue();String key=entry.getKey();
                if(value instanceof String)values.putString(key,(String)value);
                else if(value instanceof Integer)values.putInt(key,(Integer)value);
                else if(value instanceof Long)values.putLong(key,(Long)value);
                else if(value instanceof Boolean)values.putBoolean(key,(Boolean)value);
            }
            result.putBundle(name,values);
        }
        return result;
    }
    @Override public Cursor query(Uri u,String[] p,String s,String[] a,String o){throw new SecurityException("Unsupported");}
    @Override public String getType(Uri u){return null;}
    @Override public Uri insert(Uri u,ContentValues v){throw new SecurityException("Unsupported");}
    @Override public int update(Uri u,ContentValues v,String s,String[] a){throw new SecurityException("Unsupported");}
    @Override public int delete(Uri u,String s,String[] a){throw new SecurityException("Unsupported");}
}
