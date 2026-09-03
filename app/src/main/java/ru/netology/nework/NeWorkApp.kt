package ru.netology.nework

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NeWorkApp : Application() {

    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?): Intent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return super.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        }
        return super.registerReceiver(receiver, filter)
    }

    override fun registerReceiver(
        receiver: BroadcastReceiver?,
        filter: IntentFilter?,
        flags: Int
    ): Intent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            var newFlags = flags
            if (flags and Context.RECEIVER_EXPORTED == 0 && flags and Context.RECEIVER_NOT_EXPORTED == 0) {
                newFlags = flags or Context.RECEIVER_NOT_EXPORTED
            }
            return super.registerReceiver(receiver, filter, newFlags)
        }
        return super.registerReceiver(receiver, filter, flags)
    }
}
