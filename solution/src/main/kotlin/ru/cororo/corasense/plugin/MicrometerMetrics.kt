package ru.cororo.corasense.plugin

import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.koin.ktor.ext.get
import ru.cororo.corasense.service.MicrometerService

fun Application.configureMicrometer() {
    val prometheusRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        registry = prometheusRegistry
        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics()
        )
    }

    hikariDs?.metricRegistry = prometheusRegistry
    get<MicrometerService>().init(prometheusRegistry)

    routing {
        get("/metrics", builder = {
            summary = "Метрика для Prometheus"
        }) {
            call.respond(prometheusRegistry.scrape())
        }
    }
}