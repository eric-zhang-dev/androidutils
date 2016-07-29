package com.zy.androidutils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AppUtil {
	private AppUtil() { 
        /* cannot be instantiated*/
        throw new UnsupportedOperationException("cannot be instantiated");
    } 
  
    /** 
     * ��ȡӦ�ó������� 
     * 
     * @param context 
     * @return 
     */
    public static String getAppName(Context context) {
  
        PackageManager packageManager = context.getPackageManager();
        try { 
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } 
        return null; 
    } 
  
    /** 
     * ��ȡӦ�ó���汾������Ϣ 
     * 
     * @param context 
     * @return ��ǰӦ�õİ汾���� 
     */
    public static String getVersionName(Context context) {
        try { 
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } 
        return null; 
    } 
  
    /** 
     * ��ȡӦ�ó���İ汾Code��Ϣ 
     * @param context 
     * @return �汾code 
     */
    public static int getVersionCode(Context context) {
        try { 
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } 
        return 0; 
    } 
}
