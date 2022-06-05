package com.rogerbassons.avnavmap

import android.app.Activity
import android.graphics.*
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.view.*
import androidx.core.graphics.ColorUtils
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File
import java.lang.Math.*
import kotlin.math.pow


interface OnAipTaskCompleted {
    fun onTaskCompleted(airspaces: List<Airspace>)
}

class TextOverlay : Overlay {

    private val paint = Paint()
    private var firstPoint: GeoPoint? = null
    private var secondPoint: GeoPoint? = null
    private var text: String = ""


    constructor(firstPoint: GeoPoint, secondPoint: GeoPoint, txt: String) {
        this.firstPoint = firstPoint
        this.secondPoint = secondPoint

        paint.color = Color.BLACK
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        text = txt
    }

    override fun draw(c: Canvas, osmv: MapView, shadow: Boolean) {
        if (shadow) {
            return
        }

        var p1= Point()
        var p2= Point()
        osmv.projection.toPixels(firstPoint, p1)
        osmv.projection.toPixels(secondPoint, p2)

        var path = Path()
        path.moveTo(p1.x.toFloat(), p1.y.toFloat())
        path.lineTo(p2.x.toFloat(), p2.y.toFloat())


        var mat = Matrix()

        var dx = abs(p1.x - p2.x).toDouble()
        var dy = abs(p1.y - p2.y).toDouble()
        var dist = sqrt(dx.pow(2) + dy.pow(2))

        mat.setTranslate((-dy / dist).toFloat() * 20, (dx / dist).toFloat() * 40)
        path.transform(mat)


        c.drawTextOnPath(text, path, 0f, 0f, paint)
    }
}

class MainActivity : Activity(), OnAipTaskCompleted {

    private var map: MapView? = null
    private var mLocationOverlay: MyLocationNewOverlay? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        //val ctx = applicationContext
        //Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        Configuration.getInstance().osmdroidBasePath =
            File(Environment.getExternalStorageDirectory(), "osmdroid")
        Configuration.getInstance().osmdroidTileCache =
            File(cacheDir.absolutePath, "tile")


        //inflate and create the map
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        map = findViewById<MapView>(R.id.map)
        map!!.setTileSource(TileSourceFactory.MAPNIK)

        map!!.setMultiTouchControls(true)
        map!!.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

        AipTask().execute(AipTaskParams(applicationContext, this))


        setStaringPoint()
        setLocationOverlay()

    }

    public override fun onResume() {
        super.onResume()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map!!.onResume() //needed for compass, my location overlays, v6.0.0 and up
    }

    public override fun onPause() {
        super.onPause()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map!!.onPause()  //needed for compass, my location overlays, v6.0.0 and up
    }

    fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle
    ): View {


        val urls = arrayOf("https://tile.openstreetmap.org/")
        val tileSource = XYTileSource(
            "Mapnik",
            0,
            19,
            256,
            ".png",
            urls
        )

        map!!.setTileSource(tileSource)


        return map!!
    }

    private fun setStaringPoint() {
        val mapController = map!!.controller
        mapController!!.setZoom(9.5)
        val startPoint = GeoPoint(42.0, 2.8)
        mapController.setCenter(startPoint)
    }

    private fun setLocationOverlay() {
        val gpsProvider = GpsMyLocationProvider(applicationContext)
        gpsProvider.locationUpdateMinDistance = 10f
        gpsProvider.locationUpdateMinTime = 1

        mLocationOverlay = MyLocationNewOverlay(gpsProvider, map)
        mLocationOverlay!!.enableMyLocation()
        map!!.overlays.add(this.mLocationOverlay)
    }

    override fun onTaskCompleted(airspaces: List<Airspace>) {
        airspaces.forEach {
            val polygon = Polygon()    //see note below
            polygon.points = it.GetGeometry()
            polygon.title = "AIRSPACE"

            val color = getAirspaceColor(it)
            polygon.fillPaint.color = ColorUtils.setAlphaComponent(color, 60)
            polygon.outlinePaint.color = ColorUtils.setAlphaComponent(color, 80)
            polygon.subDescription ="test"

            map!!.overlayManager.add(polygon)


            map!!.overlayManager.add(
                TextOverlay(
                    polygon.actualPoints[0],
                    polygon.actualPoints[1],
                    it.GetClassText() + " " + it.GetLowerLimitText() + " - " + it.GetUpperLimitText()
                )
            )


        }

        map!!.invalidate()

    }

    private fun getAirspaceColor(airspace: Airspace): Int {
        return when(airspace.type) {
            2,3,8,9,14,16,19,25 -> Color.RED
            1 -> Color.rgb(255, 165, 0)
            else -> Color.BLUE
        }
    }

}
