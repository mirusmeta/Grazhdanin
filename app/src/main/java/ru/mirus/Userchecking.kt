package ru.mirus

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock

class Userchecking : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        task10minutes()
        return START_STICKY
    }

    private fun task10minutes() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReceiverUser::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val alarmTime = SystemClock.elapsedRealtime() + (1 * 30 * 1000)
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, (1 * 30 * 1000), pendingIntent)
    }
}