package ru.mirusmeta

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.vk.id.OAuth
import com.vk.id.VKID
import com.vk.id.logout.VKIDLogoutCallback
import com.vk.id.logout.VKIDLogoutFail
import com.vk.id.refresh.VKIDRefreshTokenFail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

        checkInternetConnection()
    }

    private fun checkInternetConnection() {
        if (isNetworkAvailable()) {
            Log.i("20241", "Есть интернет")
            loadData()
        } else {
            showNoInternetUI()
            btn?.setOnClickListener {
                recreate()
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
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

    private fun loadData() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(0)
            if (phoneNum != null) {
                goToHome()
            } else {
                goToAuth()
            }
        }
    }

    private fun goToHome() {
        Log.d("20241", "Переходим к домашнему экра")
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
