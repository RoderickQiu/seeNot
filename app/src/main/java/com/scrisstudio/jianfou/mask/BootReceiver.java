package com.scrisstudio.jianfou.mask;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.scrisstudio.jianfou.activity.SplashActivity;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//software auto start
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") || intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON")) {
			Intent it = new Intent(context, SplashActivity.class);
			it.setAction("android.intent.action.MAIN");
			it.addCategory("android.intent.category.LAUNCHER");
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(it);
			Toast.makeText(context, "见否正在自启动...", Toast.LENGTH_LONG).show();
		}
	}

}