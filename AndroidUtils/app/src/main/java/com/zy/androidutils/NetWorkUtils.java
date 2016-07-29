package com.zy.androidutils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;

public class NetWorkUtils {
	private NetWorkUtils() {
		/* cannot be instantiated */
		throw new UnsupportedOperationException("cannot be instantiated");
	}

	public static boolean isConnected(Context context) {

		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (null != connectivity) {

			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (null != info && info.isConnected()) {
				if (info.getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isWifi(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm == null)
			return false;
		return cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;

	}

	public static void openSetting(Activity activity) {
		Intent intent = new Intent("/");
		ComponentName cm = new ComponentName("com.android.settings",
				"com.android.settings.WirelessSettings");
		intent.setComponent(cm);
		intent.setAction("android.intent.action.VIEW");
		activity.startActivityForResult(intent, 0);
	}
	/**
	 * 判断WIFI信号强弱
	 *
	 * @param context
	 * @return
	 */
	public static String getWIFIState(Context context) {
		int level = Math.abs(((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getRssi());
		if (level >= 0 && level <= 50) {
			return "WIFI信号很强";
		} else if (level > 50 && level <= 70) {
			return "WIFI信号偏差";
		} else {
			return "WIFI信号很差";
		}}
	/**
	 * 当wifi不能访问网络时，mobile才会起作用
	 *
	 * @return GPRS是否连接可用
	 */
	public static boolean isMobileConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mMobile = cm
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mMobile != null) {
			return mMobile.isConnected();
		}
		return false;
	}

	/**
	 * GPRS网络开关 反射ConnectivityManager中hide的方法setMobileDataEnabled 可以开启和关闭GPRS网络
	 *
	 * @param isEnable
	 * @throws Exception
	 */
	public static void toggleGprs(Context context, boolean isEnable) {
		try {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			Class<?> cmClass = cm.getClass();
			Class<?>[] argClasses = new Class[1];
			argClasses[0] = boolean.class;
			// 反射ConnectivityManager中hide的方法setMobileDataEnabled，可以开启和关闭GPRS网络
			Method method = cmClass.getMethod("setMobileDataEnabled",
					argClasses);
			method.invoke(cm, isEnable);
		} catch (Exception e) {
		}
	}

	/**
	 * WIFI网络开关
	 *
	 * @param enabled
	 * @return 设置是否success
	 */
	public boolean toggleWiFi(Context context, boolean enabled) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		return wm.setWifiEnabled(enabled);
	}

	/**
	 *
	 * @return 是否处于飞行模式
	 */
	public boolean isAirplaneModeOn(Context context) {
		// 返回值是1时表示处于飞行模式
		int modeIdx = Settings.System.getInt(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0);
		boolean isEnabled = (modeIdx == 1);
		return isEnabled;
	}

	/**
	 * 飞行模式开关
	 *
	 * @param setAirPlane
	 */
	public void toggleAirplaneMode(Context context, boolean setAirPlane) {
		Settings.System.putInt(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, setAirPlane ? 1 : 0);
		// 广播飞行模式信号的改变，让相应的程序可以处理。
		// 不发送广播时，在非飞行模式下，Android 2.2.1上测试关闭了Wifi,不关闭正常的通话网络(如GMS/GPRS等)。
		// 不发送广播时，在飞行模式下，Android 2.2.1上测试无法关闭飞行模式。
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		// intent.putExtra("Sponsor", "Sodino");
		// 2.3及以后，需设置此状态，否则会一直处于与运营商断连的情况
		intent.putExtra("state", setAirPlane);
		context.sendBroadcast(intent);
	}

	/**
	 * 执行ping命令
	 *
	 * @param ip
	 * @return
	 */
	public static boolean pingIP(String ip) {
		boolean result;
		Runtime runtime = Runtime.getRuntime();
		try {
			Process process = runtime.exec("ping -c 1 -w 1 " + ip);
			if (process.waitFor() == 0) {
				result = true;
			} else {
				result = false;
			}
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	/**
	 * 判断android SDK版本
	 *
	 * @return
	 */

	public static int getSDKVersionNumber() {
		int sdkVersion;
		try {
			sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
		} catch (NumberFormatException e) {
			sdkVersion = 0;
		}
		return sdkVersion;
	}

	/**
	 * 获取APP版本号
	 *
	 * @param context
	 * @return
	 */
	public static String getAppVersionName(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					"com.bredpouV2", 0);
			return packageInfo.versionName;
		} catch (Exception e) {
			return "";
		}
	}

	public static String getNetWorkType(Context context) {
		if (null != context) {
			String net_work_type = null;
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cm.getActiveNetworkInfo();
			if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
				net_work_type = "WIFI";
			} else if (info != null
					&& info.getType() == ConnectivityManager.TYPE_MOBILE) {
				int networkType = info.getSubtype();
				switch (networkType) {
					case TelephonyManager.NETWORK_TYPE_GPRS:
					case TelephonyManager.NETWORK_TYPE_EDGE:
					case TelephonyManager.NETWORK_TYPE_CDMA:
					case TelephonyManager.NETWORK_TYPE_1xRTT:
					case TelephonyManager.NETWORK_TYPE_IDEN: // api<8 : replace by
						// 11
						net_work_type = "2G";
						break;
					case TelephonyManager.NETWORK_TYPE_UMTS:
					case TelephonyManager.NETWORK_TYPE_EVDO_0:
					case TelephonyManager.NETWORK_TYPE_EVDO_A:
					case TelephonyManager.NETWORK_TYPE_HSDPA:
					case TelephonyManager.NETWORK_TYPE_HSUPA:
					case TelephonyManager.NETWORK_TYPE_HSPA:
					case TelephonyManager.NETWORK_TYPE_EVDO_B: // api<9 : replace by
						// 14
					case TelephonyManager.NETWORK_TYPE_EHRPD: // api<11 : replace by
						// 12
					case TelephonyManager.NETWORK_TYPE_HSPAP: // api<13 : replace by
						// 15
						net_work_type = "3G";
						break;
					case TelephonyManager.NETWORK_TYPE_LTE: // api<11 : replace by
						// 13
						net_work_type = "4G";
						break;
					default:

						break;
				}
//				Toast.makeText(context, net_work_type + "网络连接成功",
//						Toast.LENGTH_SHORT).show();
			} else {
//				Toast.makeText(context, "网络连接失败，请检查网络", Toast.LENGTH_SHORT)
//						.show();
			}

			return net_work_type;
		}
		return "";
	}
}
