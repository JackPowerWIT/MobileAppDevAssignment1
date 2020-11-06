package org.wit.scorewriter.console.helpers

import java.io.*

fun write(fileName: String, data: String): Boolean
{
    val file = File(fileName)
    return try {
        val outputStreamWriter = OutputStreamWriter(FileOutputStream(file))
        outputStreamWriter.write(data)
        outputStreamWriter.close()
        true
    }
    catch (e: Exception){
        System.err.println("Cannot read file: $e")
        false
    }
}

fun read(fileName: String): String
{
    val file = File(fileName)
    var str = ""
    try {
        val inputStreamReader = InputStreamReader(FileInputStream(file))
        if (inputStreamReader != null){
            val bufferedReader = BufferedReader(inputStreamReader)
            val partialStr = StringBuilder()
            do {
                val line = bufferedReader.readLine()
                if (line != null) partialStr.append(line)
            } while (line != null)
            inputStreamReader.close()
            str = partialStr.toString()
        }
    }
    catch (e: FileNotFoundException){
        System.err.println("Cannot read file: $e")
    }
    catch (e: IOException){
        System.err.println("Cannot read file: $e")
    }
    return str
}

fun exists(fileName: String): Boolean
{
    val file = File(fileName)
    return file.exists()
}