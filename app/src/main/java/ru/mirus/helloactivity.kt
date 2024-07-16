package ru.mirus

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
import com.vk.id.VKID

class helloactivity : AppCompatActivity() {

    companion object {
        var isAdmin = false
    }
    private val nameOfUser by lazy {
        "${VKID.instance.accessToken?.userData?.firstName} ${VKID.instance.accessToken?.userData?.lastName}"
    }
    private val spannableHelloName by lazy {
        val spannableHelloName = SpannableString("Добро пожаловать,\n$nameOfUser")
        spannableHelloName.setSpan(ForegroundColorSpan(Color.BLACK), 0, 16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableHelloName.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.btn)), 17, spannableHelloName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableHelloName
    }
    private val spannableCompanyName by lazy {
        val spannableCompanyName = SpannableString("By, Mirus")
        spannableCompanyName.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.purple_200)), 4, spannableCompanyName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableCompanyName
    }
    private var helloName: TextView? = null
    private var companyName: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_helloactivity)
        Log.d("20241", "Запуск приветственного окна")

        helloName = findViewById(R.id.helloname)
        companyName = findViewById(R.id.s)

        helloName?.text = spannableHelloName
        companyName?.text = spannableCompanyName

        checkAdmin()
        Handler().postDelayed({
            val intent = Intent(this@helloactivity, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }, 120)
    }

    private fun checkAdmin() {
        //Тут будет браться список из строки firestore, если членовек админ!
    }
}
