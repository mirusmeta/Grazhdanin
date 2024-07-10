package ru.mirusmeta

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlin.math.floor

class raiting : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_raiting)
        enableEdgeToEdge()
        Log.d("20241", "Переход в activity оценивания")
        val statusot:TextView = findViewById(R.id.statusot)
        val likes:TextView = findViewById(R.id.likes)
        val views:TextView = findViewById(R.id.views)
        val category:TextView = findViewById(R.id.category)
        val name:TextView = findViewById(R.id.name)
        val textView3:TextView = findViewById(R.id.textView3)
        val id = intent.getStringExtra("id")
        val db = Firebase.firestore
        var lat = 0.0
        var lon = 0.0
        var olrait = 0.0
        var kv = 1
        val ochen:Button = findViewById(R.id.ochen)
        ochen.isEnabled = false
        val ratingBar:RatingBar = findViewById(R.id.ratingBar)
        ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            Log.d("20241", "Изменена оценка: ${rating} из 5.0")
            ochen.isEnabled = true
        }
        //Проверка у пользователя
        val sp = getSharedPreferences("memory", MODE_PRIVATE)
        val likes1 = sp.getString("votes", "dontgotten").toString()
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
                loadImageWithRoundedCorners(uri.toString())
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
            val st = it.getString("status")
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
        val h = Handler()
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
    private fun loadImageWithRoundedCorners(url: String) {
        Picasso.get().load(url)
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                    val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
                    roundedBitmapDrawable.cornerRadius = 28f
                    findViewById<ImageView>(R.id.imagev).setImageDrawable(roundedBitmapDrawable)
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
    }

}