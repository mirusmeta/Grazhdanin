package ru.southcode

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
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class splashscreen : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var btn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

        textView = findViewById(R.id.textView)
        btn = findViewById(R.id.btn)

        checkInternetConnection()
    }

    private fun checkInternetConnection() {
        if (isNetworkAvailable()) {
            Log.i("20241", "Есть интернет")
            loadDataFromSharedPreferences()
        } else {
            showNoInternetUI()
            btn.setOnClickListener {
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
        textView.visibility = View.VISIBLE
        btn.visibility = View.VISIBLE
        Log.e("20241", "Нету интернета")
    }

    private fun loadDataFromSharedPreferences() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(0)//Корутины)
            val sharedPreferences = getSharedPreferences("memory", Context.MODE_PRIVATE)
            val entertoapp = sharedPreferences.getString("WP", "0").toString()

            if (entertoapp != "0") {
                val id = sharedPreferences.getString("id", "novalue").toString()
                val email = sharedPreferences.getString("email", "null@789").toString()
                val password = sharedPreferences.getString("password", "NOLESS").toString()

                if (id == "novalue" || email == "null@789" || password == "NOLESS") {
                    Log.e("20241", "Повреждение preferences")
                    Toast.makeText(
                        this@splashscreen,
                        "Данные пользователя повреждены, заново авторизуйтесь!",
                        Toast.LENGTH_LONG
                    ).show()
                    goToAuth()
                } else {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").get().addOnSuccessListener { documents ->
                        var flag = false
                        val encryptionHelper = EncryptionHelper(AuthChooserActivity.KEY)

                        for (doc in documents) {
                            if (doc.id == id) {
                                Log.i("20241", "User найден")
                                val emailSimple = doc.getString("email").toString()
                                val passwordSimple = doc.getString("password").toString()
                                val emailEncrypted = encryptionHelper.decrypt(emailSimple)
                                val passEncrypted = encryptionHelper.decrypt(passwordSimple)
                                Log.i(
                                    "20241",
                                    "Почта(E): $emailSimple\nПочта(U):$emailEncrypted\nПароль(E): $passwordSimple\nПароль(U): $passEncrypted"
                                )
                                if (email == emailEncrypted && password == passEncrypted) {
                                    val sp = sharedPreferences.edit()
                                    sp.putString("votes", doc.get("votes").toString())
                                    sp.putLong("liked", doc.getLong("liked")!!.toLong()).apply()
                                    sp.putString("isadmin", doc.get("isadmin").toString()).apply()
                                    Log.i("20241", "Обновление данных\nВход выполнен")

                                    val intent =
                                        Intent(this@splashscreen, helloactivity::class.java)
                                    startActivity(intent)
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                                    flag = true
                                } else {
                                    flag = true
                                    Log.e("20241", "Акк повреждён")
                                    Toast.makeText(
                                        this@splashscreen,
                                        "У Вашего аккаунта изменился пароль, заново авторизуйтесь!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    goToAuth()
                                }
                            }
                        }
                        if (!flag) {
                            Log.e("20241", "Нету пользователя!")
                            Toast.makeText(
                                this@splashscreen,
                                "Пользователя не существует!",
                                Toast.LENGTH_LONG
                            ).show()
                            goToAuth()
                        }
                    }
                }
            } else {
                goToAuth()
            }
        }
    }

    private fun goToAuth() {
        Log.d("20241", "Переходим к авторизации")
        val intent = Intent(this, AuthChooserActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}
