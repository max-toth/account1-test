package com.account1

import com.account1.config.Config
import com.account1.data.JsLibRating
import com.account1.service.JsLibScraperService
import org.jsoup.Jsoup
import java.util.concurrent.Executors

fun main(args: Array<String>) {

    Config.print()

    // Bing not stupid he wants cookies back so getting index page
    val searchEngine: String = Config.getValue("search.engine") as String
    val cookies = Jsoup.connect(searchEngine).execute().cookies()

    // then set cookies and search
    val document = Jsoup.connect("${searchEngine}search?q=${args.joinToString("+")}")
        .header("accept", "text/html,application/xhtml+xml,application/xml")
        .userAgent(Config.getValue("user.agent") as String?)
        .cookies(cookies)
        .get()

    val organicSettings: Map<String, Any> = Config.getValue("organic") as Map<String, Any>
    val units = document.select(organicSettings["item.selector"] as String?)
        .select(organicSettings["link.selector"] as String?)
        .map {
            JsLibScraperService(it.text())
        }

    val rating = JsLibRating()
    val pool = Executors.newCachedThreadPool()
    val results = pool.invokeAll(units)
        .map { it.get() }

    results.forEach {
            val workerData = it.data
            workerData.keys.forEach { key ->
                var rank = rating.data[key]
                if (rank != null) {
                    rank += workerData[key]!!
                    rating.data[key] = rank
                } else {
                    rating.data[key] = workerData[key]!!
                }
            }
        }

    println("***** TOP 5 JavaScript libs Usage *****")
    println(
        rating.data.toList()
            .sortedByDescending { (_, value) -> value }
            .take(5)
            .toMap()
            .forEach {
                println("${it.value} ${it.key}")
            }
    )
}