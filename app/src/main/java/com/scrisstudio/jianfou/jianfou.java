package com.scrisstudio.jianfou;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;

public class jianfou extends Application {
	public static final Void voided = null;
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

	public static String getRuleTypeRealName(int type) {
		switch (type) {
			case 0:
				return "常规遮罩";
			case 1:
				return "简单返回";
			case 2:
				return "动态遮罩";
			case 3:
				return "比例遮罩";
			default:
				return "未知类型";
		}
	}

	public void onCreate() {
		super.onCreate();
		jianfou.context = getApplicationContext();
	}
}
