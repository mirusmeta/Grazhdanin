package ru.mirus

import com.squareup.picasso.Target
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.widget.addTextChangedListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class RaitingAdmin : AppCompatActivity() {

    private var FIRST_CATEGORY = ""
    private lateinit var dropdownMenu: AutoCompleteTextView
    private lateinit var img: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_raiting_admin)
        enableEdgeToEdge()
        dropdownMenu = findViewById(R.id.dropdown_menu)
        val likes: TextView = findViewById(R.id.likes)
        val views: TextView = findViewById(R.id.views)
        val category: TextView = findViewById(R.id.category)
        val name: TextView = findViewById(R.id.name)
        img = findViewById(R.id.imagev)
        val textView3: TextView = findViewById(R.id.textView3)
        val id = intent.getStringExtra("id")
        val db = FirebaseFirestore.getInstance()

        setUpDropdownMenu(dropdownMenu)
        fetchDataFromFirestore(db, id, name, category, views, likes, textView3)

        val h = Handler()
        h.postDelayed({
            setUpMap(db, id, h)
        }, 600)

        setUpOchenButton(dropdownMenu, id, db)
    }
    private fun fetchDataFromFirestore(
        db: FirebaseFirestore,
        id: String?,
        name: TextView,
        category: TextView,
        views: TextView,
        likes: TextView,
        textView3: TextView
    ) {
        db.collection("reports").document(id.toString()).get().addOnSuccessListener { document ->
            name.text = document.getString("name")
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference.child("images/${document.getString("image")}")
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                loadImageWithRoundedCorners(uri.toString())
            }.addOnFailureListener { exception ->
                Log.e("FirestoreImageLoadError", "Failed to load image: ${exception.message}")
            }
            category.text = document.getString("categories")
            views.text = document.getLong("views").toString()
            likes.text = document.getDouble("liked").toString()

            val statusEN = document.getString("status")
            val statusRU = when (statusEN) {
                "created" -> "Создано"
                "viewed" -> "Просмотрено"
                "incomplete" -> "Выполняется"
                "completed" -> "Готово"
                else -> ""
            }
            dropdownMenu.setText(statusRU, false)
            FIRST_CATEGORY = statusRU.toString()

            val originalText = document.getString("deskription") ?: ""
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
        }
    }

    private fun setUpMap(db: FirebaseFirestore, id: String?, h: Handler) {
        val mapFragment = SupportMapFragment.newInstance()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.map_container, mapFragment)
        fragmentTransaction.commit()

        db.collection("reports").document(id.toString()).get().addOnSuccessListener { document ->
            val lat = document.getDouble("wherelat") ?: 0.0
            val lon = document.getDouble("wherelon") ?: 0.0

            mapFragment.getMapAsync { map ->
                map.addMarker(MarkerOptions().title("Место").position(LatLng(lat, lon)))
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 16f))
            }
        }
    }

    private fun setUpDropdownMenu(dropdownMenu: AutoCompleteTextView) {
        val items = listOf("Создано", "Просмотрено", "Выполняется", "Готово")
        val adapter = ArrayAdapter(this, R.layout.dropdown_menu_item, items)
        dropdownMenu.setAdapter(adapter)
    }

    private fun setUpOchenButton(dropdownMenu: AutoCompleteTextView, id: String?, db: FirebaseFirestore) {
        val ochen: Button = findViewById(R.id.ochen)
        dropdownMenu.addTextChangedListener {
            ochen.isEnabled = dropdownMenu.text.toString() != FIRST_CATEGORY && FIRST_CATEGORY.isNotEmpty()
        }

        ochen.setOnClickListener {
            if (it.isEnabled) {
                it.isEnabled = false
                if (dropdownMenu.text.toString() != FIRST_CATEGORY) {
                    Toast.makeText(this, dropdownMenu.text.toString(), Toast.LENGTH_LONG).show()
                    val status = when (dropdownMenu.text.toString()) {
                        "Создано" -> "created"
                        "Просмотрено" -> "viewed"
                        "Выполняется" -> "incomplete"
                        "Готово" -> "completed"
                        else -> ""
                    }
                    db.collection("reports").document(id.toString()).update("status", status)
                }
            } else {
                it.isEnabled = false
            }
        }
    }
    private fun loadImageWithRoundedCorners(url: String) {
        Picasso.get().load(url)
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                    val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
                    roundedBitmapDrawable.cornerRadius = 28f
                    img.setImageDrawable(roundedBitmapDrawable)
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
    }

    override fun onBackPressed() {
        finish()
    }
}
