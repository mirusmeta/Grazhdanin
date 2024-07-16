package ru.mirus

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vk.id.VKID
import kotlinx.coroutines.*

class splashscreen : AppCompatActivity() {

    private var textView: TextView? = null
    private var btn: Button? = null

    private val phoneNum by lazy {
        VKID.instance.accessToken?.userData?.phone
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

        VKID.init(this@splashscreen)

        textView = findViewById(R.id.textView)
        btn = findViewById(R.id.btn)

        val imgLogo = findViewById<ImageView>(R.id.img)
        val slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        imgLogo.startAnimation(slideDownAnimation)

        slideDownAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                val swingAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.swing)
                imgLogo.startAnimation(swingAnimation)
            }
        })

        // Check internet connection in background
        CoroutineScope(Dispatchers.IO).launch {
            checkInternetConnection()
        }
    }

    private suspend fun checkInternetConnection() {
        if (isNetworkAvailable()) {
            Log.i("20241", "Есть интернет")
            loadData()
        } else {
            withContext(Dispatchers.Main) {
                showNoInternetUI()
                btn?.setOnClickListener {
                    recreate()
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }

    private fun showNoInternetUI() {
        textView?.visibility = View.VISIBLE
        btn?.visibility = View.VISIBLE
        Log.e("20241", "Нету интернета")
    }

    private suspend fun loadData() {
        delay(1800)
        withContext(Dispatchers.Main) {
            if (phoneNum != null) {
                goToHome()
            } else {
                goToAuth()
            }
        }
    }

    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun goToAuth() {
        Log.d("20241", "Переходим к авторизации")
        val intent = Intent(this, AuthChooserActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}
