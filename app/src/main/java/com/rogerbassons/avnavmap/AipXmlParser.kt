package com.rogerbassons.avnavmap

import android.util.Log
import android.util.Xml
import org.osmdroid.util.GeoPoint
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream


private val ns: String? = null

class AipXmlParser {

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): List<Airspace> {
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            return readOpenAip(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readOpenAip(parser: XmlPullParser): List<Airspace> {
        var asps = mutableListOf<Airspace>()

        parser.require(XmlPullParser.START_TAG, ns, "OPENAIP")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            Log.d("PARSER", parser.name)
            if (parser.name == "AIRSPACES") {
                asps = readAirspaces(parser)
            } else {
                skip(parser)
            }
        }
        return asps
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readAirspaces(parser: XmlPullParser): MutableList<Airspace> {
        val asps = mutableListOf<Airspace>()

        parser.require(XmlPullParser.START_TAG, ns, "AIRSPACES")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            Log.d("PARSER", parser.name)
            if (parser.name == "ASP") {
                val airspace = readASP(parser)
                if (airspace != null) {
                    asps.add(airspace)
                }

            } else {
                skip(parser)
            }
        }
        return asps
    }


    @Throws(XmlPullParserException::class, IOException::class)
    private fun readASP(parser: XmlPullParser): Airspace? {
        var airspace: Airspace? = null

        parser.require(XmlPullParser.START_TAG, ns, "ASP")
        var category = parser.getAttributeValue(null, "CATEGORY")
        if (category == null) {
            category = ""
        }

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == "GEOMETRY") {

                airspace = Airspace(readGeometry(parser)!!, category)

            } else {
                skip(parser)
            }
        }
        return airspace
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readGeometry(parser: XmlPullParser): List<GeoPoint>? {
        var points: List<GeoPoint>? = null

        parser.require(XmlPullParser.START_TAG, ns, "GEOMETRY")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == "POLYGON") {
                points = readPolygon(parser)
            } else {
                skip(parser)
            }
        }
        return points
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readPolygon(parser: XmlPullParser): MutableList<GeoPoint> {
        val points = mutableListOf<GeoPoint>()

        var text = ""
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.text
            parser.nextTag()
        }

        text.split(",").forEach {
            val latlon = it.trim().split(" ")

            points.add(GeoPoint(latlon[1].toDouble(), latlon[0].toDouble()))
        }


        Log.d("PARSER", parser.name)
        Log.d("POLYGON", text)
        return points
    }

}
