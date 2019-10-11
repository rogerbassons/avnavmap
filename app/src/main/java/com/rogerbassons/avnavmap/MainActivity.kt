package com.rogerbassons.avnavmap

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.core.graphics.ColorUtils
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

interface OnAipTaskCompleted {
    fun onTaskCompleted(airspaces: List<Airspace>)
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
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        map = findViewById<MapView>(R.id.map)
        map!!.setTileSource(TileSourceFactory.MAPNIK)

        map!!.setMultiTouchControls(true)
        map!!.setBuiltInZoomControls(false)

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


        val urls = arrayOf("http://tile.openstreetmap.org/")
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
            polygon.points = it.polygon
            polygon.title = "AIRSPACE"

            val color = getAirspaceColor(it)
            polygon.fillPaint.color = ColorUtils.setAlphaComponent(color, 60)
            polygon.outlinePaint.color = ColorUtils.setAlphaComponent(color, 80)

            map!!.overlayManager.add(polygon)
        }
    }

    private fun getAirspaceColor(airspace: Airspace): Int {
        when(airspace.type) {
            "DANGER" -> return Color.YELLOW
            "RESTRICTED" -> return Color.RED
            "PROHIBITED" -> return Color.RED
            else -> return Color.BLUE
        }
    }

}
