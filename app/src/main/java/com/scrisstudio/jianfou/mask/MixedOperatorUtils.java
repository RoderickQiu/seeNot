package com.scrisstudio.jianfou.mask;
import static com.scrisstudio.jianfou.mask.MixedExecutorService.CHANNEL_NORMAL_NOTIFICATION_ID;
import static com.scrisstudio.jianfou.mask.MixedExecutorService.NORMAL_NOTIFICATION_ID;
import static com.scrisstudio.jianfou.mask.MixedExecutorService.TAG;
import static com.scrisstudio.jianfou.mask.MixedExecutorService.normalNotificationManager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.scrisstudio.jianfou.R;
import com.scrisstudio.jianfou.activity.PermissionGrantActivity;

// TODO mix with maskOperatorUtils
public class MixedOperatorUtils {

	//log
	public static void l(Object input) {
		if (input != null)
			Log.w(TAG, input.toString() + " " + System.currentTimeMillis());
		else Log.w(TAG, "NULL" + " " + System.currentTimeMillis());
	}

	//log-error
	public static void le(Object input) {
		if (input != null)
			Log.e(TAG, input.toString() + " " + System.currentTimeMillis());
		else Log.e(TAG, "NULL" + " " + System.currentTimeMillis());
	}

	public static boolean isNightMode(Context context) {
		int currentNightMode = context.getResources().getConfiguration().uiMode &
				Configuration.UI_MODE_NIGHT_MASK;
		return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
	}

	public static void sendSimpleNotification(String title, String content, MixedExecutorService mService) {
		Intent intent = new Intent(mService, PermissionGrantActivity.class);
		PendingIntent pi = PendingIntent.getActivity(mService, 0, intent, 0);
		NotificationCompat.Builder nb = new NotificationCompat.Builder(mService, CHANNEL_NORMAL_NOTIFICATION_ID)
				.setSmallIcon(R.drawable.jianfou_no_bg)
				.setContentTitle(title)
				.setContentText(content)
				.setContentIntent(pi)
				.setAutoCancel(true)
				.setShowWhen(true);
		try {
			normalNotificationManager.notify(NORMAL_NOTIFICATION_ID, nb.build());
		} catch (Exception e) {
			le(e.getLocalizedMessage());
		}
	}

	public static boolean isCapableClass(String className) {
		if (className == null) return false;
		else if (className.contains("Activity")) return true;
		else
			return !className.startsWith("android.widget.") && !className.startsWith("android.view.") && !className.startsWith("androidx.") && !className.startsWith("com.android.systemui");
	}

	public static int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}
}
