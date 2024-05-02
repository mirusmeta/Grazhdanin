package ru.southcode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class changingpassword : AppCompatActivity() {
    private val KEY = "FyKg7for498App&L"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changingpassword)
        val oldpass:EditText = findViewById(R.id.oldpass)
        val newpassword:EditText = findViewById(R.id.newpassword)
        val newpassword2:EditText = findViewById(R.id.newpassword2)
        val button = findViewById<Button>(R.id.button)
        val paeror:TextView = findViewById(R.id.paeror)
        val ps:String = intent.getStringExtra("ps").toString()
        val id:String = intent.getStringExtra("id").toString()
        var d1 = false
        var d2 = false
        var d3 = false
        val db = Firebase.firestore

        oldpass.addTextChangedListener {
            d1 = it.toString() == ps
            paeror.visibility = View.INVISIBLE
            if(d1 && d2 && d3){
                button.isEnabled = true
            }
            else{
                button.isEnabled = false
            }
        }
        newpassword.addTextChangedListener {
            if(ps == newpassword.text.toString()){
                paeror.text = "Вы ввели старый пароль!"
                paeror.visibility = View.VISIBLE
                d2 = false
            }
            else if(ps.length >= 8){
                paeror.visibility = View.INVISIBLE
                d2 = true
            }
            else{
                paeror.text = "Длина пароля > 8 символов"
                paeror.visibility = View.VISIBLE
                d2 = false
            }
            if(d1 && d2 && d3){
                button.isEnabled = true
            }
            else{
                button.isEnabled = false
            }
        }
        newpassword2.addTextChangedListener {
            if(newpassword2.text.toString() == newpassword.text.toString()){
                d3 = true
                paeror.visibility = View.INVISIBLE
            }
            else{
                d3 = false
                Log.e("20241", "Пароль не совпадают!")
                paeror.text = "Пароли не совпадают!"
                paeror.visibility = View.VISIBLE
            }
            if(d1 && d2 && d3){
                button.isEnabled = true
            }
            else{
                button.isEnabled = false
            }
        }
        button.setOnClickListener {
            val encryptionHelper = EncryptionHelper(KEY)
            var sp = getSharedPreferences("memory", MODE_PRIVATE).edit()
            sp.putString("password", newpassword.text.toString()).apply()
            db.collection("users").document(id).update("password", encryptionHelper.encrypt(newpassword.text.toString())).addOnSuccessListener {
                Log.d("20241", "Пароль поменян")
                val intent = if(helloactivity.isAdmin)
                    Intent(this, AdminMainActivity::class.java)
                else
                    Intent(this, MainActivity::class.java)
                startActivity(intent)
            }.addOnFailureListener {
                Log.e("20241", "Пароль не поменян")
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@changingpassword, changing::class.java))
    }
}