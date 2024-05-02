package ru.southcode

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class helloactivity : AppCompatActivity() {

    companion object {
        var isAdmin = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_helloactivity)
        Log.d("20241", "Запуск приветственного окна")

        val helloName: TextView = findViewById(R.id.helloname)
        val companyName: TextView = findViewById(R.id.s)
        val sharedPreferences = getSharedPreferences("memory", Context.MODE_PRIVATE)

        val yaAdmin = sharedPreferences.getString("isadmin", "user") == "yes"

        val nameOfUser = sharedPreferences.getString("name", "")

        val spannableHelloName = SpannableString("Добро пожаловать,\n$nameOfUser")
        spannableHelloName.setSpan(ForegroundColorSpan(Color.BLACK), 0, 16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableHelloName.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.btn)), 17, spannableHelloName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        helloName.text = spannableHelloName

        val spannableCompanyName = SpannableString("By, Vometix")
        spannableCompanyName.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.purple_200)), 4, spannableCompanyName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        companyName.text = spannableCompanyName

        val delayDuration = if (yaAdmin) 200 else 200L
        val intentClass = if (yaAdmin) AdminMainActivity::class.java else MainActivity::class.java
        isAdmin = yaAdmin
        Handler().postDelayed({
            val intent = Intent(this, intentClass)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }, delayDuration)
    }
}
