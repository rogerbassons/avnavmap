package com.rogerbassons.avnavmap

import android.content.Context
import android.os.Environment
import android.renderscript.ScriptGroup
import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL


class AipStore (var context: Context?){
    val countries: Collection<String>
    val types: Collection<String>

    private val bucketUrl: String



    init {
        countries = mutableSetOf("es")
        types = mutableSetOf("asp", "apt")
        bucketUrl = "https://storage.googleapis.com/29f98e10-a489-4c82-ae5e-489dbcd4912f/"
    }

    fun downloadData() {

        countries.forEach { country ->
            types.forEach { type ->
                var filename = "${country}_${type}.json"

                val folder = this.context!!.cacheDir
                if (!folder.exists()) {
                    folder.mkdirs()
                }
                val file = File(folder, filename)

                if (!file.exists()) {
                    val url = URL(bucketUrl + filename)
                    val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                    try {
                        FileOutputStream(file).use { output ->
                            val buffer =
                                ByteArray(4 * 1024) // or other buffer size
                            var read: Int
                            while (BufferedInputStream(urlConnection.inputStream).read(buffer)
                                    .also { read = it } != -1
                            ) {
                                output.write(buffer, 0, read)
                            }
                            output.flush()
                        }
                    } finally {
                        urlConnection.disconnect()
                    }
                }
            }
        }

    }

    fun getAirspaces(): List<Airspace> {
        //this.downloadData()

        var airspaces = listOf<Airspace>()
        countries.forEach { country ->
            var filename = "${country}_asp.json"

            val am = this.context!!.assets
            var stream = am.open(filename)

            /*
            val folder = this.context!!.cacheDir
            val file = File(folder, filename)
            var stream = FileInputStream(file)
             */

            try {
                airspaces += Json.decodeFromStream<List<Airspace>>(stream)
            } catch (e: Exception) {
                e.message?.let { Log.e("JSON", it) }
            }


        }

        return airspaces
    }




}