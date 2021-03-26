package com.account1.service

import com.account1.config.Config
import com.account1.data.JsLibRating
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.Callable

class JsLibScraperService(private val url: String) : Callable<JsLibRating> {

    private val jsLibRating: JsLibRating = JsLibRating()

    override fun call(): JsLibRating {
        var indexPageUrl = url
        if (!url.startsWith("http")) {
            indexPageUrl = "http://${indexPageUrl}"
        }

        println("I'm a thread for $indexPageUrl")

        var indexPageDoc: Document
        try {
            indexPageDoc = Jsoup.connect(indexPageUrl).get()
        } catch (ignored: Exception) {
            return jsLibRating
        }

        val organicSettings: Map<String, Any> = Config.getValue("organic") as Map<String, Any>

        indexPageDoc.select(organicSettings["lib.selector"] as String?)
            .forEach {
                val jsLib = it.attr("src")
                .split("/")
                .last()

                var rating = jsLibRating.data[jsLib]
                if (rating == null) {
                    jsLibRating.data[jsLib] = 1
                } else {
                    jsLibRating.data[jsLib] = ++rating
                }
            }

        println("Thread for $url is done")

        return jsLibRating
    }
}