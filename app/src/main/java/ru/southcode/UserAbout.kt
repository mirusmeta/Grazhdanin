package ru.southcode

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class UserAbout : AppCompatActivity() {
    private lateinit var userID:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_about)
        userID = intent.getStringExtra("id").toString()
        updateBd()
    }
    private fun updateBd(){
        val surandname:TextView = findViewById(R.id.surandname)
        var db = Firebase.firestore
        val idpole:TextView = findViewById(R.id.idpole)
        val email:TextView = findViewById(R.id.email)
        val roleblock:TextView = findViewById(R.id.roleblocktext)
        val roleblock2:ConstraintLayout = findViewById(R.id.roleblock)
        val text1:TextView = findViewById(R.id.text1)
        val text2:TextView = findViewById(R.id.text2)
        val remacc:TextView = findViewById(R.id.remacc)
        var rembd = Firebase.firestore
        val encryptionHelper = EncryptionHelper(AuthChooserActivity.KEY)
        db.collection("users").document(userID).get().addOnSuccessListener {
            if(MainActivity.ID == userID){
                surandname.text = "${encryptionHelper.decrypt(it.getString("surname").toString())} ${encryptionHelper.decrypt(it.getString("name").toString())}(Вы)"
                remacc.setOnClickListener {
                    MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
                        .setTitle("Удаление аккаунта")
                        .setMessage("Вы точно хотите удалить свой аккаунт?")
                        .setPositiveButton("Да"){dialog, which ->
                            rembd.collection("users").document(userID).delete()
                            finishAffinity()
                        }.setNegativeButton("Нет"){dialog, which -> }.show()
                }
            }
            else{
                surandname.text = "${encryptionHelper.decrypt(it.getString("surname").toString())} ${encryptionHelper.decrypt(it.getString("name").toString())}"
                remacc.setOnClickListener {
                    MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
                        .setTitle("Удаление аккаунта")
                        .setMessage("Вы точно хотите удалить этот аккаунт?")
                        .setPositiveButton("Да"){dialog, which ->
                            rembd.collection("users").document(userID).delete().addOnSuccessListener { finish() }
                        }.setNegativeButton("Нет"){dialog, which -> }.show()
                }
            }

            idpole.text = "ID: $userID"
            email.text = encryptionHelper.decrypt(it.getString("email").toString())
            when(it.getString("isadmin").toString()){
                "yes" -> roleblock.text = "Роль: Администратор"
                "no" -> roleblock.text = "Роль: Гражданин"
                "dis" -> roleblock.text = "Роль: Диспейтчер"
            }
            val singleItems = arrayOf("Гражданин", "Диспейтчер(Beta)", "Администратор")
            var checkedItem = 0
            roleblock2.setOnClickListener {
                MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
                    .setTitle("Назначение ролей")
                    .setPositiveButton("ОК") { dialog, which ->
                        when(checkedItem){
                            0 -> {rembd.collection("users").document(userID).update("isadmin", "no").addOnSuccessListener { updateBd() }}
                            1 -> {rembd.collection("users").document(userID).update("isadmin", "dis").addOnSuccessListener { updateBd() }}
                            2 -> {rembd.collection("users").document(userID).update("isadmin", "yes").addOnSuccessListener { updateBd() }}
                        }
                    }
                    .setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->
                        checkedItem = which
                    }.show()
            }
            text1.text = "Оценил(а): ${it.getLong("liked").toString()} обращений"
            text2.text = "Средняя оценка обращений: ${getAllChi(it.getString("votes").toString())}"
        }
    }
    private fun getAllChi(inputString:String):Double{
        val doubleRegex = Regex("""\d+\.\d+""")
        val doubleNumbers = doubleRegex.findAll(inputString)
            .map { it.value.toDouble() }
            .toList()

        val sum = doubleNumbers.sum()
        val average = if (doubleNumbers.isNotEmpty()) sum / doubleNumbers.size else 0.0
        return(average)
    }
}