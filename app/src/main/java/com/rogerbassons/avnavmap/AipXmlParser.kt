package com.rogerbassons.avnavmap

import android.util.Log
import android.util.Xml
import org.osmdroid.util.GeoPoint
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import kotlin.math.roundToInt


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

        var geometry = listOf<GeoPoint>()
        var topLimit = ""
        var bottomLimit = ""

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when {
                parser.name == "GEOMETRY" -> { geometry = readGeometry(parser)!! }
                parser.name == "ALTLIMIT_TOP" -> { topLimit = readAltlimit(parser, "TOP") }
                parser.name == "ALTLIMIT_BOTTOM" -> { bottomLimit = readAltlimit(parser, "BOTTOM") }
                else -> skip(parser)
            }



        }

        airspace = Airspace(geometry!!, category, bottomLimit, topLimit)

        return airspace
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readAltlimit(parser: XmlPullParser, type: String): String {
        var alt = ""

        parser.require(XmlPullParser.START_TAG, ns, "ALTLIMIT_$type")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == "ALT") {
                alt = readAlt(parser)
            } else {
                skip(parser)
            }
        }
        return alt
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readAlt(parser: XmlPullParser): String {

        var unit = parser.getAttributeValue(null, "UNIT")

        var text = ""

        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.text
            parser.nextTag()
        }

        var value = text.toDouble()
        if (unit == "F") {
            text = value.roundToInt().toString() + " FT"
        } else if (unit == "FL") {
            text = "FL" + value.roundToInt().toString()
        }


        return text
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


        return points
    }

}
