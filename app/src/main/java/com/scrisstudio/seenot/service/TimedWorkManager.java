package com.scrisstudio.seenot.service;

import static com.scrisstudio.seenot.SeeNot.le;
import static com.scrisstudio.seenot.service.ExecutorService.sendSimpleNotification;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.scrisstudio.seenot.R;

public class TimedWorkManager extends Worker {

    Context context;

    public TimedWorkManager(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            if (!(ExecutorService.mService != null && ExecutorService.isForegroundServiceRunning))
                sendSimpleNotification(context.getResources().getString(R.string.service_destroyed),
                        context.getResources().getString(R.string.go_accessib_for_reopen));
            return Result.success();
        } catch (Exception e) {
            le("Err: " + e + " (Work Manager)");
            return Result.retry();
        }
    }
}
