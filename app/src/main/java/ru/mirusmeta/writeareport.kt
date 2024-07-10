package ru.mirusmeta

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.IOException

class writeareport : AppCompatActivity() {
    private var imagechoosen = false
    private lateinit var geocoder: Geocoder
    private var lat = 75.428510
    private var lon = 23.621840
    private lateinit var mMap: GoogleMap
    private lateinit var rostovPolygon: Polygon
    private var IDofZaiavka = "-985"
    private lateinit var polygon: List<LatLng>
    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Log.i("20241", "Получена картинка")
                val selectedImageUri = result.data?.data
                var image: ImageView = findViewById(R.id.image)
                Picasso.get().load(selectedImageUri).into(image)
                imagechoosen = true
                var imgchoosen: TextView = findViewById(R.id.imgchoosen)
                imgchoosen.visibility = View.INVISIBLE
            }
            else{
                Log.e("20241", "Не получена картинка")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_writeareport)
        geocoder = Geocoder(this)
        var checkedItem = 0
        val sp = getSharedPreferences("memory", MODE_PRIVATE).edit()
        val mapcon: ConstraintLayout = findViewById(R.id.mapcon)
        val kategorytext: TextView = findViewById(R.id.kategorytext)
        val deskriptiontext: TextView = findViewById(R.id.deskriptiontext)
        val chooseimg: ConstraintLayout = findViewById(R.id.chooseimg)
        val buttonchoose: Button = findViewById(R.id.buttonchoose)
        val choosecity: TextView = findViewById(R.id.choosecity)
        val chooseplace: TextView = findViewById(R.id.chooseplace)
        val sendform: Button = findViewById(R.id.sendform)
        val maintext: TextView = findViewById(R.id.maintext)
        kategorytext.setOnClickListener {
            Log.d("20241", "Выборка категорий")
            val singleItems = arrayOf("Улица", "Транспорт", "Техническое")
            MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setTitle("Выберите категорию")
                .setPositiveButton("ОК") { dialog, which -> }
                .setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->
                    checkedItem = which
                    kategorytext.text = singleItems[which]
                    Log.i("20241", "Выбрана категория ${singleItems[which]}")
                }.show()
        }
        sendform.setOnClickListener {
            if (!imagechoosen || maintext.text.toString()
                    .isEmpty() || choosecity.text.toString()
                    .isEmpty() || lat == 0.0 || lon == 0.0 || deskriptiontext.text.toString()
                    .isEmpty() || kategorytext.text.toString().isEmpty()
            ) {
                Log.e("20241", "Заполнены не все поля")
                Toast.makeText(this, "Не все поля заполнены!", Toast.LENGTH_LONG).show()
            } else {
                Log.d("20241", "Все поля заполнены")
                val db = Firebase.firestore
                val imageView = findViewById<ImageView>(R.id.image)
                val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                val nameofimg = generateRandomString()
                Log.i("20241", "Сгенерирована название картинки: ${nameofimg}.jpg")
                val storageRef = FirebaseStorage.getInstance().reference.child("images/$nameofimg")
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                storageRef.putBytes(data)
                Log.i("20241", "Картинка отгружена в бд")
                val report = hashMapOf(
                    "categories" to kategorytext.text.toString(),
                    "deskription" to deskriptiontext.text.toString(),
                    "views" to 0,
                    "status" to "created",
                    "availbe" to "true",
                    "city" to choosecity.text.toString(),
                    "from" to MainActivity.ID,
                    "liked" to 0.0,
                    "name" to maintext.text.toString(),
                    "wherelat" to lat,
                    "wherelon" to lon,
                    "image" to nameofimg
                )
                db.collection("reports")
                    .add(report)
                    .addOnSuccessListener { documentReference ->
                        Log.v("20241", "Обращение добавлено")
                        var sp2 = getSharedPreferences("memory", MODE_PRIVATE)
                        sp.putString("myobr", "${sp2.getString("myobr", "")}|${documentReference.id}:created").apply()
                        IDofZaiavka = documentReference.id
                        updateuser()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Не получилось добавить пользователя!", Toast.LENGTH_LONG).show()
                        Log.e("20241", "Не отправлено! ${e.message.toString()}")
                    }
            }
        }
        chooseimg.setOnClickListener {
            openGallery()
        }
        buttonchoose.setOnClickListener {
            mapcon.visibility = View.INVISIBLE
            sendform.visibility = View.VISIBLE
            chooseplace.text = "$lat, $lon"
            Log.i("20241", "Выбраны координаты: $lat;$lon")
        }
        chooseplace.setOnClickListener {//Подругзка gMap
            Log.d("20241", "Открытие карты")
            sendform.visibility = View.INVISIBLE
            mapcon.visibility = View.VISIBLE
            val mapFragment = SupportMapFragment.newInstance()
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.add(R.id.map_container, mapFragment)
            fragmentTransaction.commit()
            // Задаем слушателю события onMapClick
            mapFragment.getMapAsync { map ->
                mMap = map
                drawRostovRegion()
                Log.d("20241", "Нарисован полигон")
                if (lat == 75.428510 && lon == 23.621840) {
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                48.20850932439838,
                                41.25624814967147
                            ), 6.5f
                        )
                    )
                } else {
                    Log.d("20241", "Добавлен маркер: $lat, $lon")
                    map.addMarker(MarkerOptions().title("Место").position(LatLng(lat, lon)))
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 14f)
                    )
                }
                map.setOnMapClickListener {
                    Log.d("20241", "Клик по карте")
                    val point = it
                    val isInside = isPointInsidePolygon(point, polygon)
                    if(isInside){
                        map.clear()
                        val latitude = it.latitude
                        val longitude = it.longitude
                        map.addMarker(
                            MarkerOptions().title("Место").position(LatLng(latitude, longitude))
                        )
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 16f)
                        )
                        lat = latitude
                        lon = longitude
                        buttonchoose.isEnabled = true
                        var address = getAddressFromLocation(lat, lon)
                        if (address != "незнаючтозаадресс") {
                            choosecity.text = address
                        }
                    }

                }
            }
        }

    }

    private fun updateuser() {
        if(IDofZaiavka != "-985"){
            var db = Firebase.firestore
            var editgo = this.getSharedPreferences("memory", Context.MODE_PRIVATE)?.edit()

            db.collection("users").document(MainActivity.ID).get().addOnSuccessListener {
                Log.i("20241", "Добавлена заявка")
                val informationbefore = it.getString("minefilds").toString()
                var db2 = Firebase.firestore
                db.collection("users").document(MainActivity.ID).update("minefilds", "$informationbefore|$IDofZaiavka:created").addOnSuccessListener {
                    editgo?.putString("myobr", "$informationbefore|$IDofZaiavka:created")?.apply()
                    finish()
                }
            }.addOnFailureListener {
                Log.e("20241", "Не добавлена заявка: ${it.message.toString()}")
            }
        }
        else{
            Log.e("20241", "Не пройдена проверка")
        }
    }

    private fun openGallery() {//Открытие activityforresult()
        Log.d("20241", "Открытие галлереи")
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getContent.launch(intent)
    }

    fun generateRandomString(): String {//Рандомизатор имени картиники, тоесть делает картинку уникальной
        return "image${System.currentTimeMillis()}.jpg"
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        val addresses: List<Address>?
        var nameaddress: String = ""
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val addressString = address.getAddressLine(0)
                nameaddress = addressString.toString()
                Log.i("20241", "Адресс получен: $nameaddress")
            } else {
                Log.e("20241", "Ошибка при получении адресса")
                nameaddress = "незнаючтозаадресс"
            }
        } catch (e: IOException) {
            Log.e("20241", "Ошибка при получении адресса: ${e.message.toString()}")
        }
        return nameaddress
    }
    private fun drawRostovRegion() {
        val rostovRegion = listOf<LatLng>(LatLng(47.13553438153998, 38.302159754585915),
            LatLng(47.626788207924484, 38.64693558667729),
            LatLng(47.675911216330555, 38.80838119541676),
            LatLng(47.853823989308516, 38.85773830894567),
            LatLng(47.79720543788483, 39.40033506176705),
            LatLng(47.81452227287008, 39.79169321577317),
            LatLng(48.18520152855693, 39.97987807258965),
            LatLng(48.21946905252555, 40.038922458828104),
            LatLng(48.289650340935864, 40.0459086869659),
            LatLng(48.67300343460887, 39.789073897806524),
            LatLng(48.77721645231985, 39.82783429183503),
            LatLng(48.76882571532184, 39.97266899791961),
            LatLng(48.883900976613795, 40.123992888804835),
            LatLng(49.253777986370096, 40.23531760760152),
            LatLng(49.564021247056, 40.21442250901585),
            LatLng(49.56885566582923, 40.574328942551844),
            LatLng(49.74923801943919, 41.16787498856184),
            LatLng(49.9478953871737, 41.16862299955497),
            LatLng(49.947614542807464, 41.61126687641212),
            LatLng(49.845554967224196, 41.854097405504426),
            LatLng(49.712596931748806, 42.022740347161466),
            LatLng(49.15161112253979, 41.91021742245316),
            LatLng(49.06248116402108, 41.946237160773904),
            LatLng(48.92420327864517, 42.57674572739463),
            LatLng(48.615251873706754, 42.589174770177266),
            LatLng(48.47521088274825, 41.998314695739275),
            LatLng(47.982790168506185, 41.964676887794),
            LatLng(47.885120069239555, 42.633608424215694),
            LatLng(47.48306808302766, 42.89760324147796),
            LatLng(47.394984901896194, 43.71681273272154),
            LatLng(47.260345326781604, 43.71428007495698),
            LatLng(47.2206928027828, 44.26371405508802),
            LatLng(46.99551484987049, 44.23361754312208),
            LatLng(46.558413149567954, 43.698461751213095),
            LatLng(46.46248141256627, 43.793346409837945),
            LatLng(46.438426217117176, 43.67734897004885),
            LatLng(46.20694368078315, 43.581340114945974),
            LatLng(46.20373210727938, 43.33606353975711),
            LatLng(46.47999999656177, 42.95666023687134),
            LatLng(46.59330066498818, 41.98044679807991),
            LatLng(46.333882318463274, 42.03822007877765),
            LatLng(46.33826166455486, 41.875041668847196),
            LatLng(46.199248220221605, 41.87421112831077),
            LatLng(46.259662160932315, 41.653413320106544),
            LatLng(46.03370979063651, 41.148167764047926),
            LatLng(46.33748558498134, 40.99921390998942),
            LatLng(46.33232932001834, 40.340600539189154),
            LatLng(46.78066488228873, 40.179610006290446),
            LatLng(46.82840841512607, 40.0971873534906),
            LatLng(46.83973494033634, 39.219058728855856),
            LatLng(46.742390240317526, 39.19499358639279),
            LatLng(46.645520546589125, 39.18348875240232),
            LatLng(46.68050905663721, 38.904560052267286),
            LatLng(46.816254818357976, 38.915757606509516),
            LatLng(46.83556222490131, 38.95693800158574),
            LatLng(46.86858575192827, 38.94707115712585),
            LatLng(46.85501743658072, 38.847996661108674),
            LatLng(46.83334689396991, 38.860288645573334),
            LatLng(46.831931848238426, 38.6897452908099),
            LatLng(46.87883828720678, 38.68945077735905),
            LatLng(47.11920264845045, 38.230287189210635),
            // Добавьте остальные координаты границ области
        )
        val polygonOptions = PolygonOptions()
            .addAll(rostovRegion)
            .strokeColor(resources.getColor(R.color.purple_200))
            .fillColor(resources.getColor(R.color.blue_tran)) // Красный цвет с некоторой прозрачностью
        rostovPolygon = mMap.addPolygon(polygonOptions)
        polygon = rostovRegion
    }
    fun isPointInsidePolygon(point: LatLng, polygon: List<LatLng>): Boolean {
        var isInside = false
        val polySize = polygon.size
        var j = polySize - 1

        for (i in polygon.indices) {
            val vertex1 = polygon[i]
            val vertex2 = polygon[j]
            if (vertex1.longitude < point.longitude && vertex2.longitude >= point.longitude || vertex2.longitude < point.longitude && vertex1.longitude >= point.longitude) {
                if (vertex1.latitude + (point.longitude - vertex1.longitude) / (vertex2.longitude - vertex1.longitude) * (vertex2.latitude - vertex1.latitude) < point.latitude) {
                    isInside = !isInside
                }
            }
            j = i
        }
        return isInside
    }
}