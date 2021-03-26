package com.account1.config

import org.yaml.snakeyaml.Yaml
import java.io.InputStream

object Config {

    private var settings: Map<String, Any>

    init {
        val yaml = Yaml()
        val inputStream: InputStream = this.javaClass
            .classLoader
            .getResourceAsStream("application.yml")
        settings = yaml.load(inputStream)
    }

    fun print() {
        println(settings)
    }

    fun getValue(key: String): Any? {
        return settings[key]
    }

}