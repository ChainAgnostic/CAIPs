#!/usr/bin/env kotlin

@file:Repository("https://jitpack.io")
@file:DependsOn("com.github.komputing:XIP-checker:0.0.1")

import org.komputing.dipchecker.CheckConfig
import org.komputing.dipchecker.checkDate
import org.komputing.dipchecker.checkFolder
import java.io.File
import kotlin.system.exitProcess

val config = CheckConfig("caip",
                mapOf(
                    "title" to { },
                    "status" to { value ->
                        if (!listOf("Draft", "Active", "Review", "Final", "Superseded").contains(value)) throw IllegalArgumentException("Invalid status $value")
                    },
                    "type" to { value ->
                        if (!listOf("Meta", "Standard").contains(value)) throw IllegalArgumentException("Invalid type $value")
                    },
                    "author" to { },
                    "created" to { checkDate(it) }),
                mapOf(
                    "requires" to {
                        if (!(Regex("\\[[0-9]+(, [0-9]+)*\\]").matches(it) || (Regex("[0-9]+").matches(it)))) throw IllegalArgumentException("Invalid requires $it")
                    },
                    "discussions-to" to { },
                    "superseded-by" to { },
                    "updated" to { checkDate(it) }
                )
)
	    
try {
    println(checkFolder(File("CAIPs"), config))
} catch (e: Exception) {
    println("Validation of CAIPs failed")
    println("Reason: " + e.message)
    exitProcess(1)
}

