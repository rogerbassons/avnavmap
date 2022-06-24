package com.rogerbassons.avnavmap
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.osmdroid.util.GeoPoint
import kotlin.collections.ArrayList

@Serializable
class Geometry {
    var type: String? = null
    var coordinates: ArrayList<ArrayList<ArrayList<Double>>>? = null
}

@Serializable
class UpperLimit {
    var value = 0
    var unit = 0
    var referenceDatum = 0
}

@Serializable
class LowerLimit {
    var value = 0
    var unit = 0
    var referenceDatum = 0
}

@Serializable
class Airspace(
    var _id: String? = null,
    var approved: Boolean = false,
    var name: String? = null,
    var type: Int = 0,
    var icaoClass: Int = 0,
    var onDemand: Boolean = false,
    var onRequest: Boolean = false,
    var byNotam: Boolean = false,
    var specialAgreement: Boolean = false,
    var geometry: Geometry? = null,
    var display: Boolean = true,
    var parents: List<Airspace>? = null,
    var country: String? = null,
    var upperLimit: UpperLimit? = null,
    var lowerLimit: LowerLimit? = null,
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
    var createdBy: String? = null,
    var updatedBy: String? = null,
    var __v: Int = 0,
    var activity: Int = 0) {

    fun GetGeometry(): List<GeoPoint>? {
        val points = mutableListOf<GeoPoint>()

        this.geometry!!.coordinates!![0].forEach {
            points.add(GeoPoint(it[1], it[0]))
        }

        return points
    }

    fun GetUpperLimitText(): String {
        var text = ""
        var alt = this.upperLimit!!.value

        if (this.upperLimit != null) {
            when (this.upperLimit!!.unit) {
                1 -> text += "$alt FT"
                6 -> text += "FL $alt"
            }

            when (this.upperLimit!!.referenceDatum) {
                0 -> text += " GND"
                1 -> text += " MSL"
                2 -> text += " STD"
            }
        }

        return text
    }

    fun GetLowerLimitText(): String {
        var text = ""
        var alt = this.lowerLimit!!.value

        if (this.lowerLimit != null) {
            when(this.lowerLimit!!.unit) {
                1 -> text += if (alt == 0) "SFC" else "${alt}FT"
                6 -> text += if (alt == 0) "SFC" else "FL$alt"
            }

            when (this.lowerLimit!!.referenceDatum) {
                0 -> text += " GND"
                1 -> text += " MSL"
                2 -> text += " STD"
            }
        }

        return text
    }


    fun GetClassText(): String {
        var text = ""

        when(this.icaoClass) {
            0 -> text += "A"
            1 -> text += "B"
            2 -> text += "C"
            3 -> text += "D"
            4 -> text += "E"
            5 -> text += "F"
            6 -> text += "G"
        }

        return text
    }
}


