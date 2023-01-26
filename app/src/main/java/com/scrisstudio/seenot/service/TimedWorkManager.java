package com.scrisstudio.seenot.service;

import static com.scrisstudio.seenot.SeeNot.le;
import static com.scrisstudio.seenot.service.ExecutorService.sendSimpleNotification;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class TimedWorkManager extends Worker {

    public TimedWorkManager(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            if (!(ExecutorService.mService != null && ExecutorService.isForegroundServiceRunning))
                sendSimpleNotification("不见君服务可能被系统错误退出了", "请前往系统无障碍设置重新打开它！");
            return Result.success();
        } catch (Exception e) {
            le("Err: " + e + " (Work Manager)");
            return Result.retry();
        }
    }
}
