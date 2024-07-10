package ru.mirusmeta

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class register : AppCompatActivity() {
    private val KEY: String = "FyKg7for498App&L"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        //Все переменные
        var arrowleft:ImageView = findViewById(R.id.arrowleft)
        var textofemail:TextView = findViewById(R.id.textofemail)
        var nametext:EditText = findViewById(R.id.nametext)
        var surnametext:EditText = findViewById(R.id.surnametext)
        var passwordtext:EditText = findViewById(R.id.passwordtext)
        var repeatpassword:EditText = findViewById(R.id.repeatpassword)
        var button:Button = findViewById(R.id.button)
        var passcodeisnot:TextView = findViewById(R.id.passcodeisnot)
        var db = Firebase.firestore.collection("users")
        var flag = false
        var progress_bar:ProgressBar = findViewById(R.id.progress_bar)
        
        //Алгоритм проверки
        var nameofemail = intent.getStringExtra("email").toString()
        if(nameofemail.isEmpty() || !nameofemail.contains('@')){
            Toast.makeText(this, "Некорректный Email", Toast.LENGTH_LONG).show()
            finish()
        }
        textofemail.text = nameofemail.toString()

        //Кнопка назад
        arrowleft.setOnClickListener {
            finish()
        }

        //Все поля
        nametext.addTextChangedListener {
            button.isEnabled = nametext.text.toString().length >= 3 && surnametext.text.toString().length >= 3 && passwordtext.text.toString().length >= 8

        }
        surnametext.addTextChangedListener {
            button.isEnabled = nametext.text.toString().length >= 3 && surnametext.text.toString().length >= 3 && passwordtext.text.toString().length >= 8
        }
        passwordtext.addTextChangedListener {
            button.isEnabled = nametext.text.toString().length >= 3 && surnametext.text.toString().length >= 3 && passwordtext.text.toString().length >= 8
            passcodeisnot.visibility = View.INVISIBLE
        }
        repeatpassword.addTextChangedListener {
            button.isEnabled = nametext.text.toString().length >= 3 && surnametext.text.toString().length >= 3 && passwordtext.text.toString().length >= 8
            passcodeisnot.visibility = View.INVISIBLE
        }

        //Обработчик кнопки
        button.setOnClickListener {
            if(button.isEnabled){
                val encryptionHelper = EncryptionHelper(KEY)
                progress_bar.visibility = View.VISIBLE
                if(passwordtext.text.toString() == repeatpassword.text.toString() && passwordtext.text.toString().length >= 8){
                    val user = hashMapOf(
                        "email" to encryptionHelper.encrypt(nameofemail),
                        "liked" to 0,
                        "name" to encryptionHelper.encrypt(nametext.text.toString()),
                        "surname" to encryptionHelper.encrypt(surnametext.text.toString()),
                        "password" to encryptionHelper.encrypt(passwordtext.text.toString()),
                        "votes" to "",
                        "isadmin" to "no"
                    )
                    db.add(user)
                        .addOnSuccessListener { documentReference ->
                            var sharedPreferences = getSharedPreferences("memory", Context.MODE_PRIVATE).edit()//Все данные о пользователе складываем
                            sharedPreferences.putString("email", nameofemail)
                            sharedPreferences.putString("liked", "0")
                            sharedPreferences.putString("name", nametext.text.toString())
                            sharedPreferences.putString("password", passwordtext.text.toString()).apply()
                            sharedPreferences.putString("surname", surnametext.text.toString()).apply()
                            sharedPreferences.putString("WP", "1")
                            sharedPreferences.putString("id", documentReference.id.toString())
                            sharedPreferences.putString("isadmin", "no")
                            sharedPreferences.putString("votes", "0").apply()//Сохраняем и заканчиваем вход
                            progress_bar.visibility = View.INVISIBLE //Прошёл вход
                            var h = Handler()
                            h.postDelayed({
                                val intent = Intent(this, helloactivity::class.java)//Intent на другой экран
                                startActivity(intent)//Переход
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)//Анимация
                            }, 20)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Пользователь не добавлен, попробуйте еще раз!",Toast.LENGTH_LONG).show()
                                finish()
                            }
                }
                else{
                    progress_bar.visibility = View.INVISIBLE
                    passcodeisnot.visibility = View.VISIBLE
                }
            }
        }
    }
}