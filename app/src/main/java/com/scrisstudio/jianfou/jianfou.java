package com.scrisstudio.jianfou;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;

public class jianfou extends Application {
	@SuppressLint("StaticFieldLeak")
	private static Context context;

	public static Context getAppContext() {
		return jianfou.context;
	}

	public static boolean isDebugApp() {
		try {
			ApplicationInfo info = jianfou.context.getApplicationInfo();
			return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
		} catch (Exception x) {
			return false;
		}
	}

	public void onCreate() {
		super.onCreate();
		jianfou.context = getApplicationContext();
	}
}
