package com.rogerbassons.avnavmap


import android.content.Context
import android.os.AsyncTask
import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.IOException
import java.lang.Exception

public class AipTaskParams internal constructor(
    internal var context: Context,
    internal var listener: OnAipTaskCompleted
)


public class AipTask : AsyncTask<AipTaskParams, Void, List<Airspace> >() {
    private var context: Context? = null
    private var listener: OnAipTaskCompleted? = null

    override fun doInBackground(vararg parameters: AipTaskParams): List<Airspace> {
        val params = parameters[0]
        this.context = params.context
        this.listener = params.listener

        var airspaces = listOf<Airspace>()
        try {
            airspaces = loadAirspaces()
        } catch (e: Exception) {
            e.message?.let { Log.e("loading airspaces", it) }
        }
        return airspaces
    }

    override fun onPostExecute(airspaces: List<Airspace>) {
        listener!!.onTaskCompleted(airspaces)
    }


    @Throws(IOException::class)
    private fun loadAirspaces(): List<Airspace> {
        return AipStore(context).getAirspaces()
    }



}



