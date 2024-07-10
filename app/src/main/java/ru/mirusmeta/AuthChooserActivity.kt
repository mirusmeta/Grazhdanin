package ru.mirusmeta

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.vk.id.AccessToken
import com.vk.id.VKID
import com.vk.id.VKIDAuthFail
import com.vk.id.auth.VKIDAuthCallback
import com.vk.id.onetap.xml.OneTap

class AuthChooserActivity : AppCompatActivity() {
    companion object {
        @JvmStatic
        var KEY = "FyKg7for498App&L"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_chooser)

        //ВК Авторизаиця
        var oneTap:OneTap = findViewById(R.id.oneTap)
        oneTap.setCallbacks(onAuth = {oAuth, accessToken ->
            goToHello()
        }, onFail = {oAuth, fail ->
            when(fail){
                is VKIDAuthFail.Canceled -> {
                    Snackbar.make(findViewById(R.id.back), "Авторизация отклонена!", Snackbar.LENGTH_LONG).show()
                }
                is VKIDAuthFail.NoBrowserAvailable -> {
                    Snackbar.make(findViewById(R.id.back), "Отсутствует бразер!", Snackbar.LENGTH_LONG).show()
                }

                else -> {
                    Snackbar.make(findViewById(R.id.back), "Ошибка. Попробуйте ещё раз!", Snackbar.LENGTH_LONG).show()
                }
            }
        })


        /*val progress_bar: ProgressBar = findViewById(R.id.progress_bar)
        val emailtext:TextView = findViewById(R.id.emailtext)
        val constraintLayout:ConstraintLayout = findViewById(R.id.constraintLayout)
        val timer:TextView = findViewById(R.id.timer)
        val button:Button = findViewById(R.id.button)
        val textView5:TextView = findViewById(R.id.textView5)
        val politika:TextView = findViewById(R.id.politika)
        politika.setOnClickListener {
            val url = "https://xn--80aaeafhdblwf1cagbb6blqcb6r.xn--p1ai/%d0%bf%d0%be%d0%bb%d0%b8%d1%82%d0%b8%d0%ba%d0%b0-%d0%ba%d0%be%d0%bd%d1%84%d0%b8%d0%b4%d0%b5%d0%bd%d1%86%d0%b8%d0%b0%d0%bb%d1%8c%d0%bd%d0%be%d1%81%d1%82%d0%b8/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        var fortimer = intent.getStringExtra("time").toString()
        if(fortimer == "turnOnTheTimer982/9/523A222"){//Чтобы точно такого значения небыло
            val tenMinutesInMillis: Long = 10 * 60 * 1000 // 10 минут в миллисекундах
            val timertime = object : CountDownTimer(tenMinutesInMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val minutes = millisUntilFinished / 1000 / 60
                    val seconds = (millisUntilFinished / 1000) % 60
                    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
                    timer.text = timeFormatted
                }
                override fun onFinish() {
                    Toast.makeText(this@AuthChooserActivity, "Время прошло, попробуйте снова!",  Toast.LENGTH_LONG).show()
                    val intent = Intent(this@AuthChooserActivity, AuthChooserActivity::class.java)//Intent на другой экран
                    startActivity(intent)//Переход
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
            }
            timertime.start()// Запускаем таймер
            textView5.text = "Дождитесь окончания таймера"
            emailtext.visibility = View.INVISIBLE
            constraintLayout.visibility = View.INVISIBLE
            button.visibility = View.INVISIBLE
            timer.visibility = View.VISIBLE
            button.text = "Ждите окончания таймера!"
        }
        else{
            val encryptionHelper = EncryptionHelper(KEY)
            var db = Firebase.firestore
            var button:Button = findViewById(R.id.button)
            button.setOnClickListener {
                if (it.isEnabled){
                    progress_bar.visibility = View.VISIBLE
                    var flag = false
                    db.collection("users").get().addOnSuccessListener { result ->
                        for(doc in result){
                            val emaildecripted:String = encryptionHelper.decrypt(doc.getString("email").toString())
                            println(emaildecripted)
                            if(emaildecripted == emailtext.text.toString()){
                                progress_bar.visibility = View.INVISIBLE
                                flag = true
                                val intent = Intent(this, vhod::class.java)//Intent на другой экран
                                intent.putExtra("ID", doc.id.toString())
                                intent.putExtra("email", emailtext.text.toString())
                                startActivity(intent)//Переход
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)//Анимация
                            }
                        }
                        if(!flag){
                            progress_bar.visibility = View.INVISIBLE
                            val intent = Intent(this, register::class.java)//Intent на другой экран
                            intent.putExtra("email", emailtext.text.toString())
                            startActivity(intent)//Переход
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)//Анимация
                        }
                    }.addOnFailureListener {
                        progress_bar.visibility = View.INVISIBLE
                        Toast.makeText(this, "Ошибка: ${it.message.toString()}", Toast.LENGTH_LONG).show()
                    }.addOnCanceledListener {
                        progress_bar.visibility = View.INVISIBLE
                        Toast.makeText(this, "Ошибка отказано в доступе!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        */
    }

    private fun goToHello() {
        Log.d("20241", "Переходим к приветственному экрану")
        val intent = Intent(this, helloactivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    override fun onBackPressed() {
        //Ставим параметр ничего не делать
    }
}



