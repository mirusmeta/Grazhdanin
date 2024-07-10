package ru.mirusmeta

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


class vhod : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vhod)
        var counter = 0
        var textofemail:TextView = findViewById(R.id.textofemail)
        var arrowleft:ImageView = findViewById(R.id.arrowleft)
        var passwordtext:EditText = findViewById(R.id.passwordtext)
        var button:Button = findViewById(R.id.button)
        var h = Handler()
        var db = Firebase.firestore
        var bglog:ConstraintLayout = findViewById(R.id.bglog)
        var passcodeisnot:TextView = findViewById(R.id.passcodeisnot)
        var nameofemail = intent.getStringExtra("email").toString()
        var IDofuser = intent.getStringExtra("ID").toString()
        var progress_bar:ProgressBar = findViewById(R.id.progress_bar)
        //Возврат обратно и проверка данных
        if(nameofemail.isEmpty() || IDofuser.isEmpty()){
            Toast.makeText(this, "Некорректный Email", Toast.LENGTH_LONG).show()
            finish()
        }
        arrowleft.setOnClickListener {
            finish()
        }
        textofemail.text = nameofemail
        passwordtext.addTextChangedListener {
            button.isEnabled = it.toString().length >= 8
        }
        button.setOnClickListener {
            if(button.isEnabled && passwordtext.text.length >= 8){//Доп проверки
                progress_bar.visibility = View.VISIBLE
                bglog.alpha = 0.2f
                button.isEnabled = false
                db.collection("users").document(IDofuser).get().addOnSuccessListener {
                    val encryptionHelper = EncryptionHelper(AuthChooserActivity.KEY)
                    val emailEncrypted = encryptionHelper.decrypt(it.getString("email").toString())
                    val passwordEncrypted = encryptionHelper.decrypt(it.getString("password").toString())
                    if(emailEncrypted == nameofemail){
                        if (passwordEncrypted == passwordtext.text.toString()){
                            //Отгрузка данных
                            var sharedPreferences = getSharedPreferences("memory", Context.MODE_PRIVATE).edit()//Все данные о пользователе складываем
                            sharedPreferences.putString("id", IDofuser)
                            sharedPreferences.putString("email", nameofemail)
                            sharedPreferences.putLong("liked", it.getLong("liked")!!)
                            sharedPreferences.putString("name", encryptionHelper.decrypt(it.getString("name").toString()))
                            sharedPreferences.putString("password", passwordEncrypted)
                            sharedPreferences.putString("surname", encryptionHelper.decrypt(it.getString("surname").toString()))
                            sharedPreferences.putString("WP", "1").apply()
                            sharedPreferences.putString("isadmin", it.getString("isadmin").toString())
                            sharedPreferences.putString("votes", it.getString("votes").toString()).apply()//Сохраняем и заканчиваем вход
                            progress_bar.visibility = View.INVISIBLE
                            bglog.alpha = 1f
                            button.isEnabled = true
                            startActivity(Intent(this, helloactivity::class.java))
                        }
                        else{
                            progress_bar.visibility = View.INVISIBLE
                            bglog.alpha = 1f
                            button.isEnabled = true
                            counter += 1
                            passcodeisnot.visibility = View.VISIBLE
                            when(counter){
                                in 1..3 -> passcodeisnot.text = "Неправильный пароль, у вас осталось ${5 - counter} попытки"
                                4 -> passcodeisnot.text = "Неправильный пароль, у вас осталось последняя попытка"
                                5 -> {
                                    val intent = Intent(this, AuthChooserActivity::class.java)
                                    intent.putExtra("time", "turnOnTheTimer982/9*/523A222")
                                    startActivity(intent)
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                                }
                            }
                            h.postDelayed({passcodeisnot.visibility = View.INVISIBLE},2800)

                        }
                    }
                    else{
                        Toast.makeText(this, "Произошла ошибка, попробуйте ещё раз!", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }.addOnCanceledListener {
                    Toast.makeText(this, "Нет доступа, попробуйте еще раз!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, AuthChooserActivity::class.java))
                }.addOnFailureListener {
                    Toast.makeText(this, "Произошла ошибка, попробуйте еще раз!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, AuthChooserActivity::class.java))
                }
            }
        }
    }
}