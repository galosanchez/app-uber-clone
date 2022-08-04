package com.galosanchez.appuberclone.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.galosanchez.appuberclone.controller.ClientBookingController;

public class CancelReceiver extends BroadcastReceiver {

    private ClientBookingController clientBookingController;

    @Override
    public void onReceive(Context context, Intent intent) {

        clientBookingController = new ClientBookingController();
        clientBookingController.updateStatus(intent.getExtras().getString("idClient"),"cancel");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(2);

    }
}
