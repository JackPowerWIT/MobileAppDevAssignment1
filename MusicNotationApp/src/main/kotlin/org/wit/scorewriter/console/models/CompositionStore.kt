package org.wit.scorewriter.console.models

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.wit.scorewriter.console.helpers.*
import java.util.*

val JSON_FILE = "compositions.json"
val gsonBuilder = GsonBuilder().setPrettyPrinting().create()
val listType = object: TypeToken<ArrayList<CompositionModel>>() {}.type

class CompositionSerialiser {

    fun serialise(compositions: List<CompositionModel>): Boolean
    {
        val jsonString = gsonBuilder.toJson(compositions, listType)
        return write(JSON_FILE, jsonString)
    }

    fun deserialise(): List<CompositionModel>
    {
        if (exists(JSON_FILE)) {
            val jsonString = read(JSON_FILE)
            return Gson().fromJson(jsonString, listType)
        }
        return emptyList()
    }
}