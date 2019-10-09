package com.rogerbassons.avnavmap

import android.app.Activity
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay









class MainActivity : Activity() {

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


        setStaringPoint()
        setLocationOverlay()
        addAirspaces()

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

    private fun addAirspaces() {


        //polygons supports holes too, points should be in a counter-clockwise order
        //val holes = ArrayList<GeoPoint>()
        //holes.add(geoPoints)
        //polygon.holes = holes

        map!!.overlayManager.add(testPolygon())
    }

    private fun testPolygon(): Polygon {
        val geoPoints = arrayListOf<GeoPoint>()

        geoPoints.add(GeoPoint(42.326621663127, 3.11))
        geoPoints.add(GeoPoint(42.326288411638, 3.1189971020077))
        geoPoints.add(GeoPoint(42.325291998131, 3.1279040058678))
        geoPoints.add(GeoPoint(42.323642411684, 3.1366314262004))
        geoPoints.add(GeoPoint(42.321356188407, 3.1450918935817))
        geoPoints.add(GeoPoint(42.318456244164, 3.1532006386888))
        geoPoints.add(GeoPoint(42.314971642833, 3.1608764481625))
        geoPoints.add(GeoPoint(42.310937302541, 3.1680424832858))
        geoPoints.add(GeoPoint(42.306393642944, 3.1746270530146))
        geoPoints.add(GeoPoint(42.301386177221, 3.1805643334304))
        geoPoints.add(GeoPoint(42.29596505303, 3.1857950263086))
        geoPoints.add(GeoPoint(42.290184547176, 3.1902669501925))
        geoPoints.add(GeoPoint(42.284102519214, 3.1939355581334))
        geoPoints.add(GeoPoint(42.2777798296, 3.1967643770746))
        geoPoints.add(GeoPoint(42.271279728355, 3.198725364725))
        geoPoints.add(GeoPoint(42.264667220469, 3.1997991806646))
        geoPoints.add(GeoPoint(42.258008414495, 3.199975369341))
        geoPoints.add(GeoPoint(42.251369860899, 3.1992524535398))
        geoPoints.add(GeoPoint(42.244817886824, 3.1976379378377))
        geoPoints.add(GeoPoint(42.238417933929, 3.1951482224532))
        geoPoints.add(GeoPoint(42.232233905867, 3.1918084288004))
        geoPoints.add(GeoPoint(42.226327531892, 3.1876521389073))
        geoPoints.add(GeoPoint(42.220757752842, 3.1827210516788))
        geoPoints.add(GeoPoint(42.215580135529, 3.1770645597599))
        geoPoints.add(GeoPoint(42.210846321254, 3.1707392514786))
        geoPoints.add(GeoPoint(42.206603513793, 3.1638083430184))
        geoPoints.add(GeoPoint(42.202894011824, 3.1563410465837))
        geoPoints.add(GeoPoint(42.199754790293, 3.1484118808696))
        geoPoints.add(GeoPoint(42.197217134746, 3.1400999306424))
        geoPoints.add(GeoPoint(42.195306332122, 3.1314880626537))
        geoPoints.add(GeoPoint(42.19404142097, 3.1226621054792))
        geoPoints.add(GeoPoint(42.193435003465, 3.1137100011571))
        geoPoints.add(GeoPoint(42.193493121015, 3.1047209367376))
        geoPoints.add(GeoPoint(42.194215194633, 3.0957844640075))
        geoPoints.add(GeoPoint(42.195594030654, 3.0869896157524))
        geoPoints.add(GeoPoint(42.197615891733, 3.0784240269469))
        geoPoints.add(GeoPoint(42.200260632447, 3.07017306922))
        geoPoints.add(GeoPoint(42.203501898224, 3.0623190068421))
        geoPoints.add(GeoPoint(42.207307385697, 3.0549401823046))
        geoPoints.add(GeoPoint(42.211639161995, 3.0481102393253))
        geoPoints.add(GeoPoint(42.21645403994, 3.0418973908081))
        geoPoints.add(GeoPoint(42.221704005529, 3.0363637389155))
        geoPoints.add(GeoPoint(42.227336693613, 3.0315646539742))
        geoPoints.add(GeoPoint(42.233295907169, 3.027548218438))
        geoPoints.add(GeoPoint(42.23952217515, 3.0243547415652))
        geoPoints.add(GeoPoint(42.245953343478, 3.0220163498492))
        geoPoints.add(GeoPoint(42.252525193404, 3.0205566575591))
        geoPoints.add(GeoPoint(42.25917208119, 3.0199905210124))
        geoPoints.add(GeoPoint(42.265827592771, 3.0203238794222))
        geoPoints.add(GeoPoint(42.272425206941, 3.0215536843312))
        geoPoints.add(GeoPoint(42.27889896044, 3.0236679187855))
        geoPoints.add(GeoPoint(42.28518410829, 3.0266457065062))
        geoPoints.add(GeoPoint(42.291217772738, 3.0304575104063))
        geoPoints.add(GeoPoint(42.296939574248, 3.0350654188738))
        geoPoints.add(GeoPoint(42.302292238129, 3.0404235173198))
        geoPoints.add(GeoPoint(42.30722217061, 3.0464783415756))
        geoPoints.add(GeoPoint(42.311679998458, 3.053169408829))
        geoPoints.add(GeoPoint(42.315621066587, 3.0604298209329))
        geoPoints.add(GeoPoint(42.319005888517, 3.0681869341064))
        geoPoints.add(GeoPoint(42.321800545011, 3.0763630882931))
        geoPoints.add(GeoPoint(42.323977026744, 3.0848763887534))
        geoPoints.add(GeoPoint(42.325513517438, 3.0936415318633))
        geoPoints.add(GeoPoint(42.326394614495, 3.10257066657))
        geoPoints.add(GeoPoint(42.326621663127, 3.11))

        val polygon = Polygon()    //see note below
        geoPoints.add(geoPoints[0])
        polygon.points = geoPoints
        polygon.title = "LED134"
        polygon.fillPaint.color = Color.argb(60, 46, 95, 255)
        polygon.outlinePaint.color = Color.argb(100, 46, 95, 255)

        return polygon
    }

}
