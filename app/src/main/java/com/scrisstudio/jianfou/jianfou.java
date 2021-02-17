package com.scrisstudio.jianfou;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

public class jianfou extends Application {
	@SuppressLint("StaticFieldLeak")
	private static Context context;

	public static Context getAppContext() {
		return jianfou.context;
	}

	public void onCreate() {
		super.onCreate();
		jianfou.context = getApplicationContext();
	}
}
