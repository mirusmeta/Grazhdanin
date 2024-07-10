package ru.mirusmeta

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class proverka : AppCompatActivity() {
    private val KEY = "FyKg7for498App&L"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proverka)
        Log.v("20241", "Окно проверки")
        //Объявление переменных
        val id = intent.getStringExtra("ID")
        val textviewall:TextView = findViewById(R.id.textviewall)
        val email = intent.getStringExtra("email")
        val password = intent.getStringExtra("password")
        val textofemail:TextView = findViewById(R.id.textofemail)
        val passwordtext:EditText = findViewById(R.id.passwordtext)
        val button:Button = findViewById(R.id.button)
        val db = Firebase.firestore
        val h = Handler()
        val progress: LinearProgressIndicator = findViewById(R.id.progress)

        textofemail.text = email
        passwordtext.addTextChangedListener {
            if(it?.length!! >= 8){
                button.isEnabled = true
            }
        }
        button.setOnClickListener {
            Log.d("20241", "Запуск алгоритма проверки")
            progress.visibility = View.VISIBLE
            button.isEnabled = false
            passwordtext.isEnabled = false
            if(!isNetworkAvailable()){
                Log.e("20241", "Нету интернета")
                startActivity(Intent(this, splashscreen::class.java))
                finish()
            }
            db.collection("users").document(id.toString()).get().addOnSuccessListener {
                Log.d("20241", "Получение данных пользователя с бд")
                if(password != passwordtext.text.toString()){
                    Log.e("20241", "Не тот пароль от пользователя")
                    passwordtext.isEnabled = true
                    progress.visibility = View.INVISIBLE
                    textviewall.text = "Ошибка"
                    h.postDelayed({textviewall.text = "VoID"}, 5000)
                }
                else{
                    val encryptionHelper = EncryptionHelper(KEY)
                    if(passwordtext.text.toString() == encryptionHelper.decrypt(it.getString("password").toString())){
                        Log.i("20241", "Проверка пройдена")
                        progress.visibility = View.INVISIBLE
                        var intent = Intent(this@proverka, changing::class.java)
                        intent.putExtra("id", id)
                        startActivity(intent)
                    }
                    else{
                        Log.e("20241", "Не совпадает с паролем с бд")
                        passwordtext.isEnabled = true
                        progress.visibility = View.INVISIBLE
                        textviewall.text = "Ошибка"
                        h.postDelayed({textviewall.text = "VoID"}, 5000)
                    }
                }
            }
        }
    }
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                // Другие типы подключений, если они вам нужны
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
}