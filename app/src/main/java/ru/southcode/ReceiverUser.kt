package ru.southcode

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class ReceiverUser : BroadcastReceiver() {
    lateinit var notificationManager: NotificationManager
    lateinit var builder:Notification.Builder
    private var channelIId = "i.apps.notifications"

    override fun onReceive(context: Context?, intent: Intent?) {
        var db = Firebase.firestore
        notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(context, channelIId, "ОГ Channel", "ОГ")

        val sp = context?.getSharedPreferences("memory", Context.MODE_PRIVATE)
        val editgo = context?.getSharedPreferences("memory", Context.MODE_PRIVATE)?.edit()
        var obrash = sp!!.getString("myobr", "LR").toString()
        db.collection("users").document(sp.getString("id", "nan").toString()).get().addOnSuccessListener {
            if(obrash != "LR"){
                if(obrash != it.getString("minefilds").toString()){
                    editgo?.putString("myobr", it.getString("minefilds").toString())?.apply()
                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
                        builder = Notification.Builder(context, channelIId)
                                    .setContentText(" ")
                                    .setContentTitle("Стасус вашей заявки изменился")
                                    .setSmallIcon(R.drawable.logo)
                    }else{
                                builder = Notification.Builder(context)
                                    .setContentText(" ")
                                    .setContentTitle("Стасус вашей заявки изменился")
                                    .setSmallIcon(R.drawable.logo) }
                    notificationManager.notify(19967, builder.build())




                }
                else{
                    println("Nothing new!")
                }
            }
            else{
                Toast.makeText(context, "Ошибка работы в фоновом режиме!", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun createNotificationChannel(context: Context, channelId: String, channelName: String, channelDescription: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}
