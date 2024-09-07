package ru.mirus

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

import android.view.animation.AccelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.vk.id.VKID
import com.vk.id.logout.VKIDLogoutCallback
import com.vk.id.logout.VKIDLogoutFail

class MainActivity : AppCompatActivity() {
    companion object {
        @JvmStatic
        var ID = ""
    }

    private lateinit var mMap: GoogleMap
    private lateinit var rostovPolygon: Polygon
    private lateinit var polygon: List<LatLng>
    lateinit var mapFragment: SupportMapFragment
    private var h = Handler()
    private var db:FirebaseFirestore? = null

    //ФИО и Телефон
    private val nameOfUser by lazy { "Вы: ${VKID.instance.accessToken?.userData?.firstName} ${VKID.instance.accessToken?.userData?.lastName}" }
    private val phoneOfUser by lazy { "Телефон: +${VKID.instance.accessToken?.userData?.phone}" }
    private val phoneOfUserWithoutText by lazy { "+${VKID.instance.accessToken?.userData?.phone}" }


    //Разметка объявление
    private val constrofcard by lazy { findViewById<ConstraintLayout>(R.id.constrofcard) }
    private val closeicon by lazy { findViewById<ImageView>(R.id.closeicon) }
    private val nameofbrash by lazy { findViewById<TextView>(R.id.nameofbrash) }
    private val cardcategory by lazy { findViewById<TextView>(R.id.cardcategory) }
    private val views by lazy { findViewById<TextView>(R.id.views) }
    private val likes by lazy { findViewById<TextView>(R.id.likes) }
    private val carddeskription by lazy { findViewById<TextView>(R.id.carddeskription) }
    private val status by lazy { findViewById<TextView>(R.id.status) }
    private val imgoftask by lazy { findViewById<ImageView>(R.id.imgoftask) }
    private val faq by lazy { findViewById<ConstraintLayout>(R.id.faq) }

    // Разметка действия с профилем
    private val imageProfile by lazy { VKID.instance.accessToken?.userData?.photo200 }
    private val profileImage by lazy { findViewById<ImageView>(R.id.profileImage) }
    private val surandname by lazy { findViewById<TextView>(R.id.surandname) }
    private val phone by lazy { findViewById<TextView>(R.id.email) }
    private val logout by lazy { findViewById<TextView>(R.id.signout) }
    private val support by lazy { findViewById<ConstraintLayout>(R.id.support) }
    private val add by lazy { findViewById<TextView>(R.id.add) }

    // Меню переключений
    private val textviewall by lazy { findViewById<TextView>(R.id.textviewall) }
    private val homepage by lazy { findViewById<ConstraintLayout>(R.id.homepage) }
    private val mappage by lazy { findViewById<ConstraintLayout>(R.id.mappage) }
    private val profilepage by lazy { findViewById<ConstraintLayout>(R.id.profilepage) }
    private val bottom_navigation by lazy { findViewById<BottomNavigationView>(R.id.bottom_navigation) }
    private val chip1 by lazy { findViewById<Chip>(R.id.chip1) }
    private val chip2 by lazy { findViewById<Chip>(R.id.chip2) }
    private var currentPage: Int = R.id.item_1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Firebase.firestore

        runBottomMenu()//Нижнее меню
        closeicon?.setOnClickListener { closeFromPred() }//Закрытие вспомогательной карточки

        //Инициализации
        initializeMap()
        initializeProfile()
        dbUpdates()

        //Слушатели
        logout?.setOnClickListener{
            logout()
        }
        add.setOnClickListener {
            startActivity(Intent(this, writeareport::class.java))
        }
        support?.setOnClickListener {
            openEmail("vometix@gmail.com", "Поддержка \"${getString(R.string.app_name)}\"")
        }
        faq?.setOnClickListener {
            openFAQquestions()
        }
    }

    private fun initializeProfile() {
        surandname?.text = nameOfUser
        phone?.text = phoneOfUser
        Picasso.get().load(imageProfile).transform(RoundedCornersTransformation(10f)).into(profileImage)
    }

    private fun initializeMap() {
        mapFragment = SupportMapFragment.newInstance()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.map_container, mapFragment).commit()
        mapFragment.getMapAsync{ map ->
            mMap = map
            drawRostovRegion()
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(48.20850932439838, 41.25624814967147), 6.4f)
            )
            map.setOnMarkerClickListener {
                if(isPointInsidePolygon(it.position, polygon)){
                    val idOfPoint = it.snippet.toString()
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position, 13.6f))
                    db?.collection("reports")?.document(idOfPoint)?.get()?.addOnSuccessListener { it->
                        if(constrofcard?.visibility == View.INVISIBLE){
                            constrofcard?.apply {
                                visibility = View.VISIBLE
                                translationY = 500f
                                val animator = ObjectAnimator.ofFloat(this, "translationY", 0f).apply {
                                    duration = 400L
                                    interpolator = AccelerateDecelerateInterpolator()
                                }
                                animator.start()
                            }
                        }
                        nameofbrash?.text = it.getString("name").toString()
                        cardcategory?.text = it.getString("categories").toString()
                        val statusMap = mapOf(
                            "created" to "Создано",
                            "viewed" to "Просмотрено",
                            "incomplete" to "Выполняется",
                            "completed" to "Выполнено",
                            "denclined" to "Отклонено"
                        )

                        val statust = it.getString("status")?.let { status ->
                            statusMap.getOrDefault(status, "Ошибка загрузки")
                        } ?: "Ошибка загрузки"

                        status?.text = statust
                        val id = it.id
                        carddeskription?.text = limitStringLength(it.getString("deskription").toString())

                        var ratesOfAll: Double? = 0.0
                        var kolvoAll:Int? = 0
                        it.getString("marksofall")?.split("+")?.filter { it.isNotEmpty() }?.map{ pair ->
                            val parts = pair.split(":")
                            val phone = "+${parts[0]}"
                            val rate = parts[1].toInt()
                            ratesOfAll = ratesOfAll!! + rate
                            kolvoAll = kolvoAll!! + 1
                            phone to rate
                        }
                        if(kolvoAll != 0){
                            likes.text = (ratesOfAll!! / kolvoAll!!).toString()
                            views.text = kolvoAll.toString()
                        }else{
                            likes.text = "0"
                            views.text = "0"
                        }


                        val storageRef = FirebaseStorage.getInstance().reference.child("images/${it.getString("image")}")
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            Picasso.get().load(uri).transform(RoundedCornersTransformation(20f)).into(imgoftask)
                        }.addOnFailureListener { exception ->
                            Log.e("20241", "Не получилось загрузить изображение у picaso: ${exception.message}")
                        }

                        constrofcard?.setOnClickListener {
                            Log.d("20241", "Переход в содержимое карточки через карту")
                            val intent = Intent(this@MainActivity, raiting::class.java)
                            intent.putExtra("id", id.toString())
                            startActivity(intent)
                        }
                    }
                }
                else{
                    Log.d("20241", "Клик за пределами полигона")
                }
                true
            }
        }
    }

    private fun closeFromPred() {
        val yDelta = 500f  // Расстояние, на которое элемент сместится вниз
        val duration = 302L  // Длительность анимации в миллисекундах
        val animation = TranslateAnimation(0f, 0f, 0f, yDelta)
        animation.duration = duration
        animation.interpolator = AccelerateInterpolator()
        constrofcard?.startAnimation(animation)
        h.postDelayed({
            constrofcard?.visibility = View.INVISIBLE
        }, 20)

    }

    @SuppressLint("ResourceAsColor")
    private fun runBottomMenu() {
        bottom_navigation?.setOnItemSelectedListener { item ->
            val previousPage = currentPage
            currentPage = item.itemId
            val direction = currentPage > previousPage

            when (item.itemId) {
                R.id.item_1 -> {
                    if(previousPage != R.id.item_1){
                        switchView(homepage, mappage, profilepage, direction)
                        textviewall?.text = "Все обращения"
                    }
                    true
                }
                R.id.item_3 -> {
                    if(previousPage != R.id.item_3){
                        switchView(mappage, homepage, profilepage, direction)
                        textviewall?.text = "Карта"
                    }
                    true
                }
                R.id.item_4 -> {
                    if(previousPage != R.id.item_4){
                        switchView(profilepage, homepage, mappage, direction)
                        textviewall?.text = "Профиль"
                    }
                    true
                }
                else -> false
            }
        }



        chip1?.setOnClickListener {
            if(chip2?.isChecked == true){
                firebaseupdate()
            }
            chip1?.isChecked = true
            chip2?.isChecked = false
        }
        chip2?.setOnClickListener {
            if(chip1?.isChecked == true){
                myupdatebase()
            }
            chip2?.isChecked = true
            chip1?.isChecked = false
        }

    }

    private fun dbUpdates() {
        val db = FirebaseFirestore.getInstance()
        db.collection("reports").addSnapshotListener { value, error ->
            Log.d("20241", "Слушатель на изменение данных в firestore")
            firebaseupdate()
        }
    }

    private fun openFAQquestions() {
        val url = "https://xn--80aaeafhdblwf1cagbb6blqcb6r.xn--p1ai/%d1%87%d0%b0%d1%81%d1%82%d0%be-%d0%b7%d0%b0%d0%b4%d0%b0%d0%b2%d0%b0%d0%b5%d0%bc%d1%8b%d0%b5-%d0%b2%d0%be%d0%bf%d1%80%d0%be%d1%81%d1%8b/"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun myupdatebase() {
        Log.d("20241", "Обновление бд моих обращений")
        var h = Handler()
        h.postDelayed({//Задержка так как он должен подргузить картинки
            var listview:RecyclerView = findViewById(R.id.listview)
            val db = FirebaseFirestore.getInstance()
            val reportsCollection = db.collection("reports")
            reportsCollection.get()
                .addOnSuccessListener { result ->
                    val reportsList = mutableListOf<CustomModel>()
                    result.forEach {
                        if(it.getString("from").toString() == phoneOfUserWithoutText){
                            var ratesOfAll: Double? = 0.0
                            var kolvoAll:Int? = 0
                            it.getString("marksofall")?.split("+")?.filter { it.isNotEmpty() }?.map{ pair ->
                                val parts = pair.split(":")
                                val phone = "+${parts[0]}"
                                val rate = parts[1].toInt()
                                ratesOfAll = ratesOfAll!! + rate
                                kolvoAll = kolvoAll!! + 1
                                phone to rate
                            }
                            var report:CustomModel
                            if(kolvoAll != 0){
                                report = CustomModel(
                                    it.id.toString(),
                                    it.getString("name").toString(),
                                    "Место: ${it.getString("city").toString()}",
                                    "images/${it.getString("image").toString()}",
                                    it.id.toString(),
                                    ((ratesOfAll!! / kolvoAll!!).toDouble())
                                )
                            }
                            else{
                                report = CustomModel(
                                    it.id.toString(),
                                    it.getString("name").toString(),
                                    "Место: ${it.getString("city").toString()}",
                                    "images/${it.getString("image").toString()}",
                                    it.id.toString(),
                                    0.0
                                )
                            }
                            reportsList.add(report)
                        }
                    }
                    reportsList.sortBy { it.raiting }
                    reportsList.reverse()
                    val reportAdapter = CustomAdapter(this@MainActivity, reportsList)
                    listview.layoutManager = LinearLayoutManager(this)
                    listview.adapter = reportAdapter
                }
                .addOnFailureListener { _ ->
                    Log.d("20241", "Ошибка бд")
                }
        },100)
    }

    private fun openEmail(email: String, subject: String) {
        Log.d("20241", "Открытие Email")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("mailto:$email?subject=$subject")
        startActivity(intent)

    }

    private fun firebaseupdate(){
        Log.d("20241", "Обновление бд всех обращений")
        var h = Handler()
        h.postDelayed({//Задержка так как он должен подргузить картинки
            var listview:RecyclerView = findViewById(R.id.listview)
            val db = FirebaseFirestore.getInstance()
            val reportsCollection = db.collection("reports")
            reportsCollection.get()
                .addOnSuccessListener { result ->
                    val reportsList = mutableListOf<CustomModel>()
                    for (document in result) {
                        if(document.getString("availbe").toString() == "true"){
                            val lat = document.getDouble("wherelat")!!
                            val lon = document.getDouble("wherelon")!!
                            val name = document.getString("name").toString()
                            var ratesOfAll: Double? = 0.0
                            var kolvoAll:Int? = 0

                            document.getString("marksofall")?.split("+")?.filter { it.isNotEmpty() }?.map{ pair ->
                                val parts = pair.split(":")
                                val phone = "+${parts[0]}"
                                val rate = parts[1].toInt()
                                ratesOfAll = ratesOfAll!! + rate
                                kolvoAll = kolvoAll!! + 1
                                phone to rate
                            }
                            var report:CustomModel
                            if(kolvoAll != 0){
                                report = CustomModel(
                                    document.id.toString(),
                                    document.getString("name").toString(),
                                    "Место: ${document.getString("city").toString()}",
                                    "images/${document.getString("image").toString()}",
                                    document.id.toString(),
                                    ((ratesOfAll!! / kolvoAll!!).toDouble())
                                )
                            }
                            else{
                                report = CustomModel(
                                    document.id.toString(),
                                    document.getString("name").toString(),
                                    "Место: ${document.getString("city").toString()}",
                                    "images/${document.getString("image").toString()}",
                                    document.id.toString(),
                                    0.0
                                )
                            }
                            reportsList.add(report)
                            mapFragment.getMapAsync {map ->
                                when(document.getString("status").toString()){
                                    "created" -> map.addMarker(MarkerOptions().icon(
                                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title(name).position(LatLng(lat, lon)).snippet(document.id.toString()))
                                    "viewed" -> map.addMarker(MarkerOptions().icon(
                                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title(name).position(LatLng(lat, lon)).snippet(document.id.toString()))
                                    "incomplete" -> map.addMarker(MarkerOptions().icon(
                                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).title(name).position(LatLng(lat, lon)).snippet(document.id.toString()))
                                    "completed" -> map.addMarker(MarkerOptions().icon(
                                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(name).position(LatLng(lat, lon)).snippet(document.id.toString()))
                                }
                            }
                        }
                    }
                    reportsList.sortBy { it.raiting }
                    reportsList.reverse()
                    val reportAdapter = CustomAdapter(this@MainActivity, reportsList)
                    listview.layoutManager = LinearLayoutManager(this)
                    listview.adapter = reportAdapter
                }
                .addOnFailureListener { _ ->
                    Toast.makeText(this, "Ошибка загрузки данных!", Toast.LENGTH_LONG).show()
                }
        },100)
    }
    private fun logout(){
        Log.d("20241", "Окно выхода")
        MaterialAlertDialogBuilder(this@MainActivity, R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setTitle("Выход")
            .setMessage("Вы точно хотите выйти из аккаунта?")
            .setNegativeButton("Нет") { dialog, which ->
                Log.d("2024", "Не выполнен выход из аккаунта")
            }
            .setPositiveButton("Да") { dialog, which ->
                VKID.instance.logout(
                    lifecycleOwner = this@MainActivity,
                    callback = object : VKIDLogoutCallback {
                        override fun onFail(fail: VKIDLogoutFail) {
                            Snackbar.make(findViewById(R.id.back), "Не получилось выполнить выход!", Snackbar.LENGTH_LONG).show()
                        }

                        override fun onSuccess() {
                            Log.d("2024", "Выполнен выход из аккаунта")
                            startActivity(Intent(this@MainActivity, splashscreen::class.java))
                        }
                    }
                )

            }
            .show()

    }
    private fun switchView(toShow: ConstraintLayout, toHide1: ConstraintLayout, toHide2: ConstraintLayout, direction: Boolean) {

        val duration2 = 300L
        val slideDirection = if (direction) 1f else -1f

        // Анимация исчезновения и сдвига для скрываемых вью
        val fadeOut1 = ObjectAnimator.ofFloat(toHide1, "alpha", 1f, 0f).apply { duration = duration2 }
        val fadeOut2 = ObjectAnimator.ofFloat(toHide2, "alpha", 1f, 0f).apply { duration = duration2 }
        val slideOut1 = ObjectAnimator.ofFloat(toHide1, "translationX", 0f, -toHide1.width.toFloat() * slideDirection).apply { duration = duration2 }
        val slideOut2 = ObjectAnimator.ofFloat(toHide2, "translationX", 0f, -toHide2.width.toFloat() * slideDirection).apply { duration = duration2 }

        // Анимация появления и сдвига для показываемого вью
        toShow.alpha = 0f
        toShow.translationX = toShow.width.toFloat() * slideDirection
        val fadeIn = ObjectAnimator.ofFloat(toShow, "alpha", 0f, 1f).apply { duration = duration2 }
        val slideIn = ObjectAnimator.ofFloat(toShow, "translationX", toShow.width.toFloat() * slideDirection, 0f).apply { duration = duration2 }

        val hideSet = AnimatorSet().apply {
            playTogether(fadeOut1, fadeOut2, slideOut1, slideOut2)
        }
        val showSet = AnimatorSet().apply {
            playTogether(fadeIn, slideIn)
        }
        AnimatorSet().apply {
            playSequentially(hideSet, showSet)
            start()
        }

        hideSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                toHide1.visibility = View.INVISIBLE
                toHide2.visibility = View.INVISIBLE
                toShow.visibility = View.VISIBLE
            }
        })
    }


    override fun onBackPressed() {

    }
    private fun drawRostovRegion() {
        val rostovRegion = listOf<LatLng>(LatLng(47.13553438153998, 38.302159754585915), LatLng(47.626788207924484, 38.64693558667729), LatLng(47.675911216330555, 38.80838119541676), LatLng(47.853823989308516, 38.85773830894567), LatLng(47.79720543788483, 39.40033506176705), LatLng(47.81452227287008, 39.79169321577317), LatLng(48.18520152855693, 39.97987807258965), LatLng(48.21946905252555, 40.038922458828104), LatLng(48.289650340935864, 40.0459086869659), LatLng(48.67300343460887, 39.789073897806524), LatLng(48.77721645231985, 39.82783429183503), LatLng(48.76882571532184, 39.97266899791961), LatLng(48.883900976613795, 40.123992888804835), LatLng(49.253777986370096, 40.23531760760152), LatLng(49.564021247056, 40.21442250901585), LatLng(49.56885566582923, 40.574328942551844), LatLng(49.74923801943919, 41.16787498856184), LatLng(49.9478953871737, 41.16862299955497), LatLng(49.947614542807464, 41.61126687641212), LatLng(49.845554967224196, 41.854097405504426), LatLng(49.712596931748806, 42.022740347161466), LatLng(49.15161112253979, 41.91021742245316), LatLng(49.06248116402108, 41.946237160773904), LatLng(48.92420327864517, 42.57674572739463), LatLng(48.615251873706754, 42.589174770177266), LatLng(48.47521088274825, 41.998314695739275), LatLng(47.982790168506185, 41.964676887794), LatLng(47.885120069239555, 42.633608424215694), LatLng(47.48306808302766, 42.89760324147796), LatLng(47.394984901896194, 43.71681273272154), LatLng(47.260345326781604, 43.71428007495698), LatLng(47.2206928027828, 44.26371405508802), LatLng(46.99551484987049, 44.23361754312208), LatLng(46.558413149567954, 43.698461751213095), LatLng(46.46248141256627, 43.793346409837945), LatLng(46.438426217117176, 43.67734897004885), LatLng(46.20694368078315, 43.581340114945974), LatLng(46.20373210727938, 43.33606353975711), LatLng(46.47999999656177, 42.95666023687134), LatLng(46.59330066498818, 41.98044679807991), LatLng(46.333882318463274, 42.03822007877765), LatLng(46.33826166455486, 41.875041668847196), LatLng(46.199248220221605, 41.87421112831077), LatLng(46.259662160932315, 41.653413320106544), LatLng(46.03370979063651, 41.148167764047926), LatLng(46.33748558498134, 40.99921390998942), LatLng(46.33232932001834, 40.340600539189154), LatLng(46.78066488228873, 40.179610006290446), LatLng(46.82840841512607, 40.0971873534906), LatLng(46.83973494033634, 39.219058728855856), LatLng(46.742390240317526, 39.19499358639279), LatLng(46.645520546589125, 39.18348875240232), LatLng(46.68050905663721, 38.904560052267286), LatLng(46.816254818357976, 38.915757606509516), LatLng(46.83556222490131, 38.95693800158574), LatLng(46.86858575192827, 38.94707115712585), LatLng(46.85501743658072, 38.847996661108674), LatLng(46.83334689396991, 38.860288645573334), LatLng(46.831931848238426, 38.6897452908099), LatLng(46.87883828720678, 38.68945077735905), LatLng(47.11920264845045, 38.230287189210635))
        val polygonOptions = PolygonOptions()
            .addAll(rostovRegion)
            .strokeColor(resources.getColor(R.color.purple_200))
            .fillColor(resources.getColor(R.color.blue_tran))
        rostovPolygon = mMap.addPolygon(polygonOptions)
        polygon = rostovRegion
        mMap.clear()
    }
    private fun isPointInsidePolygon(point: LatLng, polygon: List<LatLng>): Boolean {
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
    fun limitStringLength(input: String): String { return "${ input.take(30) }..." }
}