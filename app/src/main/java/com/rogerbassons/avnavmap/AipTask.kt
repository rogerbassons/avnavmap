package com.rogerbassons.avnavmap


import android.content.Context
import android.os.AsyncTask
import android.util.Log
import org.osmdroid.util.GeoPoint
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.lang.Exception

public class AipTaskParams internal constructor(
    internal var context: Context,
    internal var listener: OnAipTaskCompleted
)

data class Airspace(val polygon: List<GeoPoint>, val type: String)


public class AipTask : AsyncTask<AipTaskParams, Void, List<Airspace> >() {
    private var context: Context? = null
    private var listener: OnAipTaskCompleted? = null

    override fun doInBackground(vararg parameters: AipTaskParams): List<Airspace> {
        val params = parameters[0]
        this.context = params.context
        this.listener = params.listener


        var airspaces = listOf<Airspace>()
        try {
            airspaces = loadAirspacesXML()
        } catch (e: Exception) {
            Log.e("XML", e.message)
        }
        return airspaces
    }

    override fun onPostExecute(airspaces: List<Airspace>) {
        listener!!.onTaskCompleted(airspaces)
    }


    @Throws(XmlPullParserException::class, IOException::class)
    private fun loadAirspacesXML(): List<Airspace> {

        val am = this.context!!.assets
        var stream = am.open("openaip_airspace_spain_es.aip")

        return AipXmlParser().parse(stream)
    }



}



