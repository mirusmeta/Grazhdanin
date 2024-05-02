package ru.southcode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class changing : AppCompatActivity() {
    private val KEY = "FyKg7for498App&L"
    private var passcode = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changing)
        Log.d("20241", "Открыт changing")
        val db = Firebase.firestore
        val db2 = Firebase.firestore
        val id = intent.getStringExtra("id").toString()
        val nametext:EditText = findViewById(R.id.nametext)
        val surnametext:EditText = findViewById(R.id.suenametext)
        val mailtext:EditText = findViewById(R.id.mailtext)
        val passwordclc:ConstraintLayout = findViewById(R.id.passwordclc)
        val button:Button = findViewById(R.id.button)
        val encryptionHelper = EncryptionHelper(KEY)

        nametext.addTextChangedListener {
            if(it!!.isNotEmpty()){
                button.isEnabled = true
            }
            else if(it.isEmpty() && surnametext.text.toString().isEmpty() && mailtext.text.toString().isEmpty()){
                button.isEnabled = false
            }
        }
        surnametext.addTextChangedListener {
            if(it!!.isNotEmpty()){
                button.isEnabled = true
            }
            else if(it.isEmpty() && nametext.text.toString().isEmpty() && mailtext.text.toString().isEmpty()){
                button.isEnabled = false
            }
        }
        mailtext.addTextChangedListener {
            if(it!!.contains('@') && it!!.length > 4 && it!!.contains('.')){
                button.isEnabled = true
            }
            else if(it.isEmpty() && surnametext.text.toString().isEmpty() && nametext.text.toString().isEmpty()){
                button.isEnabled = false
            }
        }

        db.collection("users").document(id).get().addOnSuccessListener { data ->
            nametext.hint = "${nametext.hint} (${encryptionHelper.decrypt(data.getString("name").toString())})"
            surnametext.hint = "${surnametext.hint} (${encryptionHelper.decrypt(data.getString("surname").toString())})"
            mailtext.hint = "${mailtext.hint} (${encryptionHelper.decrypt(data.getString("email").toString())})"
            passwordclc.setOnClickListener {
                passcode = encryptionHelper.decrypt(data.getString("password").toString())
                val intent = Intent(this@changing, changingpassword::class.java)
                intent.putExtra("ps", passcode)
                intent.putExtra("id", id)
                startActivity(intent)
            }
        }.addOnFailureListener {
            Log.d("20241", "Ошибка получения данных из бд")
            startActivity(Intent(this, MainActivity::class.java))
            Toast.makeText(this@changing, "Ошибка", Toast.LENGTH_LONG).show()
        }
        var t1 = ""
        var t2 = ""
        var t3 = ""
        var triger1 = false
        var triger2 = false
        var triger3 = false
        button.setOnClickListener {
            if(nametext.text.toString().isNotEmpty()){
                triger1 = true
                t1 = nametext.text.toString()
            }
            if(surnametext.text.toString().length > 4){
                triger2 = true
                t2 = surnametext.text.toString()
            }
            if(mailtext.text.toString().contains('@') && mailtext.text.toString().length > 4 && mailtext.text.toString().contains('.')){
                triger3 = true
                t3 = mailtext.text.toString()
            }
            var sp1 = getSharedPreferences("memory", MODE_PRIVATE).edit()
            if(triger1){
                db.collection("users").document(id).update("name", encryptionHelper.encrypt(t1))
                sp1.putString("name", t1).apply()
                Log.i("20241", "Обновлено имя: $t1\nШифр:${encryptionHelper.encrypt(t1)}")
                val intent = if(helloactivity.isAdmin)
                    Intent(this, AdminMainActivity::class.java)
                else
                    Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            if(triger2){
                db.collection("users").document(id).update("surname", encryptionHelper.encrypt(t2))
                sp1.putString("surname", t2).apply()
                Log.i("20241", "Обновлена фамилия: $t2\nШифр:${encryptionHelper.encrypt(t2)}")
                val intent = if(helloactivity.isAdmin)
                    Intent(this, AdminMainActivity::class.java)
                else
                    Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            if(triger3){
                var inthe = false
                db2.collection("users").get().addOnSuccessListener {
                    for(res in it){
                        if(res.getString("email").toString() == t3){
                            Log.e("20241", "Email уже есть в бд")
                            Toast.makeText(this, "Такой Email уже существует!", Toast.LENGTH_LONG).show()
                            inthe = true
                        }
                    }
                    if(!inthe){
                        db.collection("users").document(id).update("email", encryptionHelper.encrypt(t3))
                        sp1.putString("email", t3).apply()
                        Log.i("20241", "Обновлен email: $t3\nШифр:${encryptionHelper.encrypt(t3)}")
                        val intent = if(helloactivity.isAdmin)
                            Intent(this, AdminMainActivity::class.java)
                        else
                            Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}