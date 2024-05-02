package ru.southcode

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlin.math.floor

class raiting : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_raiting)
        Log.d("20241", "Переход в activity оценивания")
        var statusot:TextView = findViewById(R.id.statusot)
        var likes:TextView = findViewById(R.id.likes)
        var views:TextView = findViewById(R.id.views)
        var category:TextView = findViewById(R.id.category)
        var name:TextView = findViewById(R.id.name)
        var img:ImageView = findViewById(R.id.imagev)
        var textView3:TextView = findViewById(R.id.textView3)
        var id = intent.getStringExtra("id")
        var db = Firebase.firestore
        var lat = 0.0
        var lon = 0.0
        var olrait = 0.0
        var kv = 1
        var ochen:Button = findViewById(R.id.ochen)
        ochen.isEnabled = false
        var ratingBar:RatingBar = findViewById(R.id.ratingBar)
        ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            Log.d("20241", "Изменена оценка: ${rating} из 5.0")
            ochen.isEnabled = true
        }
        //Проверка у пользователя
        var sp = getSharedPreferences("memory", MODE_PRIVATE)
        var likes1 = sp.getString("votes", "dontgotten").toString()
        if(likes1 != "dontgotten"){
            if(likes1.contains(id.toString())){
                ochen.visibility = View.INVISIBLE
                ratingBar.rating = likes1.substringAfter("$id;", "").substringBefore("|").toFloat()
                Log.d("20241", "Вы уже оценивали это обращение: ${ratingBar.rating}")
            }
        }
        //Проверка самого репорта
        db.collection("reports").document(id.toString()).get().addOnSuccessListener {
            Log.d("20241", "Данные с бд получены")
            name.text = it.getString("name")
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference.child("images/${it.getString("image")}")
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get().load(uri).into(img)
            }.addOnFailureListener { exception ->
                Log.e("20241", "Картинка не загружена из бд: ${exception.message}")
            }
            olrait = it.getDouble("liked")!!
            lat = it.getDouble("wherelat")!!
            lon = it.getDouble("wherelon")!!
            category.text = it.getString("categories").toString()
            views.text = it.getLong("views").toString()
            likes.text = it.getDouble("liked").toString()
            kv += it.getLong("views")!!.toInt()
            var st = it.getString("status")
            when(st){
                "created" -> statusot.text = "Создано"
                "viewed" -> statusot.text = "Просмотрено"
                "incomplete" -> statusot.text = "В процессе выполнения"
                "completed" -> statusot.text = "Выполнено"
            }
            val originalText = it.getString("deskription").toString()
            val maxLineLength = 38

            val formattedText = StringBuilder()
            var startIndex = 0
            var endIndex = maxLineLength

            while (startIndex < originalText.length) {
                endIndex = Math.min(endIndex, originalText.length)
                while (endIndex < originalText.length && originalText[endIndex] != ' ') {
                    endIndex--
                }
                formattedText.append(originalText.substring(startIndex, endIndex))
                formattedText.append("\n")
                startIndex = endIndex + 1
                endIndex = startIndex + maxLineLength
            }
            textView3.text = formattedText.toString().trim { it <= ' ' }
        }.addOnFailureListener {
            Log.e("20241", "Данные с бд не получены")
        }
        var h = Handler()
        h.postDelayed({
            val mapFragment = SupportMapFragment.newInstance()
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.add(R.id.map_container, mapFragment)
            fragmentTransaction.commit()
            // Задаем слушателю события onMapClick
            mapFragment.getMapAsync { map ->
                map.addMarker(MarkerOptions().title("Место").position(LatLng(lat, lon)))
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 16f)
                )
                Log.d("20241", "Добавлена карта с маркером")
            }
        },600)
        ochen.setOnClickListener {
            if(olrait == 0.0){
                olrait = ratingBar.rating.toDouble()
            }
            else{
                olrait = (olrait + ratingBar.rating) / 2.0
                olrait = floor(olrait * 10.0) / 10.0
            }
            db.collection("users").document(MainActivity.ID).get().addOnSuccessListener {
                val k:Long = it.getLong("liked")!! + 1
                val likedme = it.getString("votes").toString() + "${id};${ratingBar.rating.toDouble()}|"
                var sharedPreferences = getSharedPreferences("memory", MODE_PRIVATE).edit()
                sharedPreferences.putString("votes", likedme).apply()
                db.collection("users").document(MainActivity.ID).update(mapOf("liked" to k, "votes" to likedme)).addOnSuccessListener {
                    db.collection("reports").document(id.toString()).update(mapOf("liked" to olrait, "views" to kv)).addOnSuccessListener {
                        Log.i("20241", "Выставлена оценка")
                        finish()
                    }.addOnFailureListener {
                        Log.e("20241", "Данные обращения не обновлены")
                    }
                }.addOnFailureListener {
                    Log.e("20241", "Данные пользователя не обновлены")
                }
            }.addOnFailureListener {
                Log.e("20241", "Данные с бд не получены")
            }

        }
    }
}