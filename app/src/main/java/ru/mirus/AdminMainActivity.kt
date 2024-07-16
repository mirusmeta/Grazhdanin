package ru.mirus

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class AdminMainActivity : AppCompatActivity() {
    companion object {
        @JvmStatic
        var ID = ""
        var ISADMIN = false
    }
    private lateinit var mMap: GoogleMap
    private lateinit var rostovPolygon: Polygon
    private lateinit var polygon: List<LatLng>
    lateinit var mapFragment: SupportMapFragment

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)
        var h = Handler()
        ISADMIN = true
        var constrofcard: ConstraintLayout = findViewById(R.id.constrofcard)
        var closeicon: ImageView = findViewById(R.id.closeicon)
        var nameofbrash: TextView = findViewById(R.id.nameofbrash)
        var cardcategory: TextView = findViewById(R.id.cardcategory)
        var views: TextView = findViewById(R.id.views)
        var likes: TextView = findViewById(R.id.likes)
        var carddeskription: TextView = findViewById(R.id.carddeskription)
        var status: TextView = findViewById(R.id.status)
        val sharedPreferences = getSharedPreferences("memory", Context.MODE_PRIVATE)
        closeicon.setOnClickListener {
            val yDelta = 500f  // Расстояние, на которое элемент сместится вниз
            val duration = 480L  // Длительность анимации в миллисекундах
            val animation = TranslateAnimation(0f, 0f, 0f, yDelta)
            animation.duration = duration
            animation.interpolator = AccelerateInterpolator()
            constrofcard.startAnimation(animation)
            h.postDelayed({constrofcard.visibility = View.INVISIBLE},490)
        }
        var izmenit: TextView = findViewById(R.id.izmenit)
        var surandname: TextView = findViewById(R.id.surandname)
        var email: TextView = findViewById(R.id.email)
        var password: TextView = findViewById(R.id.password)
        var kolvoall: TextView = findViewById(R.id.kolvoall)
        var logout: TextView = findViewById(R.id.signout)
        var support: ConstraintLayout = findViewById(R.id.support)
        var db2 = Firebase.firestore
        val chip1: Chip = findViewById(R.id.chip1); val chip2: Chip = findViewById(R.id.chip2)
        chip2.setOnClickListener {
            if(chip1.isChecked){
                myupdatebase()
            }
            chip2.isChecked = true
            chip1.isChecked = false
        }
        chip1.setOnClickListener {
            if(chip2.isChecked){
                firebaseupdate()
            }
            chip1.isChecked = true
            chip2.isChecked = false
        }
        //Карта
        mapFragment = SupportMapFragment.newInstance()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.map_container, mapFragment)
        fragmentTransaction.commit()
        mapFragment.getMapAsync{ map ->
            mMap = map
            drawRostovRegion()
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(48.20850932439838, 41.25624814967147), 6.4f)
            )
            map.setOnMarkerClickListener {
                val point = it.position
                val isInside = isPointInsidePolygon(point, polygon)
                if(isInside){
                    var ID = it.snippet.toString()
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position, 13.6f))
                    db2.collection("reports").document(ID).get().addOnSuccessListener {
                        if(constrofcard.visibility == View.INVISIBLE){
                            val duration = 480L
                            val animation = TranslateAnimation(0f, 0f, 500f, 0f)
                            animation.duration = duration
                            animation.interpolator = AccelerateInterpolator()
                            constrofcard.startAnimation(animation)
                            h.postDelayed({constrofcard.visibility = View.VISIBLE}, 490)
                        }
                        nameofbrash.text = it.getString("name").toString()
                        cardcategory.text = it.getString("categories").toString()
                        var statust = it.getString("status").toString()
                        when(statust){
                            "created" -> statust = "Создано"
                            "viewed" -> statust = "Просмотрено"
                            "incomplete" -> statust = "Выполняется"
                            "completed" -> statust = "Выполнено"
                        }
                        status.text = statust
                        val id = it.id
                        val deskofall = it.getString("deskription").toString()
                        val revorkeddesc = limitLines(deskofall)
                        carddeskription.text = revorkeddesc
                        likes.text = it.getDouble("liked").toString()
                        views.text = it.getLong("views").toString()
                        constrofcard.setOnClickListener {
                            var intent = Intent(this@AdminMainActivity, raiting::class.java)
                            intent.putExtra("id", id.toString())
                            startActivity(intent)
                        }
                    }
                }
                true
            }
        }
        firebaseupdate()
        var sp = getSharedPreferences("memory", MODE_PRIVATE)
        MainActivity.ID = sp.getString("id", "null").toString()
        initusers()
        surandname.text = ""
        surandname.text = "${sp.getString("surname", "Error")} ${sp.getString("name", "Error")}"
        email.text = sp.getString("email", "Error")
        var passcode = ""
        for(i in 0..sp.getString("password", "Error").toString().length){
            passcode += "*"
        }
        password.text = passcode
        kolvoall.text = "Оценил(а) ${sp.getLong("liked", 0)} обращений"

        //Выход из аккаунта
        logout.setOnClickListener{logout()}
        support.setOnClickListener { sendEmail() }

        var add: TextView = findViewById(R.id.add)//Кнопка создания обращения
        add.setOnClickListener {
            val intent = Intent(this, writeareport::class.java)//Intent на другой экран
            startActivity(intent)//Переход
        }
        //Меню переключений
        var textviewall: TextView = findViewById(R.id.textviewall)
        var homepage: ConstraintLayout = findViewById(R.id.homepage)
        var mappage: ConstraintLayout = findViewById(R.id.mappage)
        var grazhdanepage:ConstraintLayout = findViewById(R.id.grazhdanepage)
        var profilepage: ConstraintLayout = findViewById(R.id.profilepage)
        var bottom_navigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottom_navigation.setOnItemSelectedListener {item ->
            when(item.itemId) {
                R.id.item_1 -> {
                    homepage.visibility = View.VISIBLE
                    mappage.visibility = View.INVISIBLE
                    profilepage.visibility = View.INVISIBLE
                    grazhdanepage.visibility = View.INVISIBLE
                    textviewall.text = "Все обращения"
                    true
                }
                R.id.item_2 -> {
                    homepage.visibility = View.INVISIBLE
                    mappage.visibility = View.VISIBLE
                    profilepage.visibility = View.INVISIBLE
                    grazhdanepage.visibility = View.INVISIBLE
                    textviewall.text = "Карта"
                    true
                }
                R.id.item_3 -> {
                    homepage.visibility = View.INVISIBLE
                    mappage.visibility = View.INVISIBLE
                    profilepage.visibility = View.INVISIBLE
                    grazhdanepage.visibility = View.VISIBLE
                    textviewall.text = "Граждане"
                    true
                }
                R.id.item_4 -> {
                    homepage.visibility = View.INVISIBLE
                    mappage.visibility = View.INVISIBLE
                    profilepage.visibility = View.VISIBLE
                    grazhdanepage.visibility = View.INVISIBLE
                    textviewall.text = "Профиль"
                    true
                }
                else -> false
            }

        }

        val db = FirebaseFirestore.getInstance()
        db.collection("reports").addSnapshotListener { value, error ->
            firebaseupdate()
        }
        support.setOnClickListener {
            openEmail("vometix@gmail.com", "Поддержка \"Ответственный гражданин\"")
        }
    }
    private lateinit var userSearchView: com.google.android.material.search.SearchView
    private fun initusers() {
        var h = Handler()
        h.postDelayed({
            var users_recycler_view: RecyclerView = findViewById(R.id.users_recycler_view)
            val encryptionHelper = EncryptionHelper(AuthChooserActivity.KEY)
            val db = FirebaseFirestore.getInstance()
            userSearchView = findViewById(R.id.searchview)
            val reportsCollection = db.collection("users")
            reportsCollection.get()
                .addOnSuccessListener { result ->
                    val userList = mutableListOf<UserModel>()
                    for (document in result) {
                    if(document.id == MainActivity.ID){
                            val report = UserModel(
                                document.id.toString(),
                                "${encryptionHelper.decrypt(document.getString("name").toString())} ${encryptionHelper.decrypt(document.getString("surname").toString())} (Вы)",
                                encryptionHelper.decrypt(document.getString("email").toString()),
                                document.getLong("liked")!!
                            )
                            userList.add(report)
                        }
                        else{
                            val report = UserModel(
                                document.id.toString(),
                                "${encryptionHelper.decrypt(document.getString("name").toString())} ${encryptionHelper.decrypt(document.getString("surname").toString())}",
                                encryptionHelper.decrypt(document.getString("email").toString()),
                                document.getLong("liked")!!
                            )
                            userList.add(report)
                        }
                    }
                    userList.sortBy { it.liked }
                    userList.reverse()
                    val usersAdapter = UserAdapter(this@AdminMainActivity, userList)
                    users_recycler_view.layoutManager = LinearLayoutManager(this)
                    users_recycler_view.adapter = usersAdapter
                    /*userSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String): Boolean {
                            // Фильтрация списка пользователей
                            val filteredUsers = mutableListOf<UserModel>()
                            for (user in userList) {
                                if (user.email.contains(query)) {
                                    filteredUsers.add(user)
                                }
                            }

                            // Обновление адаптера
                            usersAdapter.updateUsers(filteredUsers)
                            return false
                        }

                        override fun onQueryTextChange(query: String): Boolean {
                            return false
                        }
                    })*/

                }
                .addOnFailureListener { _ ->
                    Toast.makeText(this, "Ошибка загрузки данных!", Toast.LENGTH_LONG).show()
                }


        },100)

    }
    private fun myupdatebase() {
        var h = Handler()
        h.postDelayed({//Задержка так как он должен подргузить картинки
            var listview:RecyclerView = findViewById(R.id.listview)
            val db = FirebaseFirestore.getInstance()
            val reportsCollection = db.collection("reports")
            reportsCollection.get()
                .addOnSuccessListener { result ->
                    val reportsList = mutableListOf<CustomModel>()
                    for (document in result) {
                        if(document.getString("from").toString() == MainActivity.ID){
                            var lat = 0.0
                            var lon = 0.0
                            var name = ""
                            lat = document.getDouble("wherelat")!!
                            lon = document.getDouble("wherelon")!!
                            name = document.getString("name").toString()
                            val report = CustomModel(
                                document.id.toString(),
                                document.getString("name").toString(),
                                "Место: ${document.getString("city").toString()}",
                                "images/${document.getString("image").toString()}",
                                document.id.toString(),
                                document.getDouble("liked")!!
                            )
                            reportsList.add(report)
                        }
                    }
                    reportsList.sortBy { it.raiting }
                    reportsList.reverse()
                    val reportAdapter = CustomAdapter(this@AdminMainActivity, reportsList)
                    listview.layoutManager = LinearLayoutManager(this)
                    listview.adapter = reportAdapter
                }
                .addOnFailureListener { _ ->
                    Toast.makeText(this, "Ошибка загрузки данных!", Toast.LENGTH_LONG).show()
                }
        },100)
    }
    private fun openEmail(email: String, subject: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("mailto:$email?subject=$subject")
        startActivity(intent)
    }

    private fun firebaseupdate() {
        val listview:RecyclerView = findViewById(R.id.listview)
        val db = FirebaseFirestore.getInstance()
        val reportsCollection = db.collection("reports")
        reportsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Toast.makeText(this, "Ошибка загрузки данных!", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            snapshot?.let { documents ->
                val reportsList = mutableListOf<CustomModel>()
                val updatedMarkers = mutableSetOf<String>()
                for (document in documents) {
                    val lat = document.getDouble("wherelat") ?: 0.0
                    val lon = document.getDouble("wherelon") ?: 0.0
                    val name = document.getString("name") ?: ""
                    val status = document.getString("status") ?: ""

                    val report = CustomModel(
                        document.id,
                        name,
                        "Место: ${document.getString("city")}",
                        "images/${document.getString("image")}",
                        document.id,
                        document.getDouble("liked") ?: 0.0
                    )
                    reportsList.add(report)

                    // Добавляем маркер только если он еще не создан
                    if (!updatedMarkers.contains(document.id)) {
                        mapFragment.getMapAsync { map ->
                            val marker = when (status) {
                                "created" -> map.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title(name).position(LatLng(lat, lon)).snippet(document.id))
                                "viewed" -> map.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title(name).position(LatLng(lat, lon)).snippet(document.id))
                                "incomplete" -> map.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)).title(name).position(LatLng(lat, lon)).snippet(document.id))
                                "completed" -> map.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(name).position(LatLng(lat, lon)).snippet(document.id))
                                else -> null
                            }
                            marker?.let {
                                updatedMarkers.add(document.id)
                            }
                        }
                    }
                }
                reportsList.sortBy { it.raiting }
                reportsList.reverse()
                val reportAdapter = CustomAdapter(this@AdminMainActivity, reportsList)
                listview.layoutManager = LinearLayoutManager(this)
                listview.adapter = reportAdapter
            }
        }
    }

    private fun logout(){
        MaterialAlertDialogBuilder(this)
            .setTitle("Выход")
            .setMessage("Вы точно хотите выйти из аккаунта?")
            .setNegativeButton("Нет") { dialog, which ->
                // Respond to negative button press
            }
            .setPositiveButton("Да") { dialog, which ->
                var sp = getSharedPreferences("memory", MODE_PRIVATE).edit()
                sp.clear()
                sp.apply()
                startActivity(Intent(this, splashscreen::class.java))
            }
            .show()
    }
    private fun sendEmail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:") // указываем схему "mailto:" для отправки по email
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("vometix@gmail.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Поддержка по приложению Отечественный Гражданин")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "")
        // Проверяем, есть ли почтовое приложение на устройстве, которое может обработать наше намерение
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        }
    }
    override fun onBackPressed() {
        var x = false
        if(x){
            super.onBackPressed()
        }
    }
    private fun  bitmapDescriptorFromVector(context: Context, vectorResId:Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        val bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        val canvas =  Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
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
        mMap.clear()
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
    fun limitLines(text: String, maxLines: Int = 24): String {
        val lines = text.split(" ")
        return if (lines.size <= maxLines) {
            text
        } else {
            lines.subList(0, maxLines).joinToString(" ") + "..."
        }
    }
}