package ru.mirus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.vk.id.VKID

class raiting : AppCompatActivity() {

    private val statusot by lazy{
        findViewById<TextView>(R.id.statusot)
    }
    private val likes by lazy{
        findViewById<TextView>(R.id.likes)
    }
    private val views by lazy{
        findViewById<TextView>(R.id.views)
    }
    private val category by lazy{
        findViewById<TextView>(R.id.category)
    }
    private val name by lazy{
        findViewById<TextView>(R.id.name)
    }
    private val desciption by lazy{
        findViewById<TextView>(R.id.desciption)
    }
    private val myImageForLoading by lazy{
        findViewById<ImageView>(R.id.imagev)
    }
    private val ochen by lazy{
        findViewById<Button>(R.id.ochen)
    }
    private val phoneOfUserWithoutText by lazy { "+${VKID.instance.accessToken?.userData?.phone}" }


    //Единицы
    private var lat = 0.0
    private var lon = 0.0
    private var stringOfAll:String? = ""
    private var ratesOfAll: Double? = 0.0
    private var kolvoAll:Int? = 0
    private var MYRATE:Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_raiting)

        val id = intent.getStringExtra("id")
        val db = Firebase.firestore

        ochen.isEnabled = false
        val ratingBar:RatingBar = findViewById(R.id.ratingBar)
        ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser -> ochen.isEnabled = true }

        db.collection("reports").document(id.toString()).get().addOnSuccessListener {
            name.text = it.getString("name")

            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference.child("images/${it.getString("image")}")
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get().load(uri).transform(RoundedCornersTransformation(34f)).into(myImageForLoading)
            }

            stringOfAll = it.getString("marksofall")
            stringOfAll?.split("+")?.filter { it.isNotEmpty() }?.map{ pair ->
                val parts = pair.split(":")
                val phone = "+${parts[0]}"
                val rate = parts[1].toInt()
                ratesOfAll = ratesOfAll!! + rate
                kolvoAll = kolvoAll!! + 1
                if(phoneOfUserWithoutText == phone){
                    MYRATE = rate
                    ratingBar.rating = MYRATE!!.toFloat()
                    ochen.visibility = View.INVISIBLE
                }
                phone to rate
            }
            if(kolvoAll != 0){
                likes.text = (ratesOfAll!! / kolvoAll!!).toString()
                views.text = kolvoAll.toString()
            }else{
                likes.text = "0"
                views.text = "0"
            }


            lat = it.getDouble("wherelat")!!
            lon = it.getDouble("wherelon")!!
            category.text = it.getString("categories").toString()
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
            desciption.text = formattedText.toString().trim { it <= ' ' }
        }.addOnFailureListener {
            Log.e("20241", "Данные с бд не получены")
        }
        val h = Handler()
        h.postDelayed({
            val mapFragment = SupportMapFragment.newInstance()
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.add(R.id.map_container, mapFragment)
            fragmentTransaction.commit()
            mapFragment.getMapAsync { map ->
                map.addMarker(MarkerOptions().title("Место").position(LatLng(lat, lon)))
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 16f)
                )
                Log.d("20241", "Добавлена карта с маркером")
            }
        },600)
        ochen.setOnClickListener {
            if(stringOfAll != null){
                stringOfAll += "$phoneOfUserWithoutText:${ratingBar.rating.toInt()}"
                db.collection("reports").document(id.toString()).update(mapOf("marksofall" to stringOfAll)).addOnSuccessListener {
                    finish()
                }
            }

        }
    }
}