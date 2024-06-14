/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.server.modules

import com.expediagroup.graphql.generator.execution.FunctionDataFetcher
import com.expediagroup.graphql.generator.execution.SimpleKotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.generator.hooks.FlowSubscriptionSchemaGeneratorHooks
import com.expediagroup.graphql.server.ktor.DefaultKtorGraphQLContextFactory
import com.expediagroup.graphql.server.ktor.GraphQL
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import graphql.schema.DataFetcherFactory
import io.ktor.http.HttpMethod
import io.ktor.serialization.jackson.JacksonWebsocketContentConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.websocket.WebSockets
import it.unibo.alchemist.boundary.graphql.schema.operations.mutations.SimulationControl
import it.unibo.alchemist.boundary.graphql.schema.operations.queries.EnvironmentQueries
import it.unibo.alchemist.boundary.graphql.schema.operations.queries.NodeQueries
import it.unibo.alchemist.boundary.graphql.schema.operations.subscriptions.EnvironmentSubscriptions
import it.unibo.alchemist.boundary.graphql.schema.operations.subscriptions.NodeSubscriptions
import it.unibo.alchemist.model.Environment
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

/**
 * Ktor module for enabling GraphQL on server.
 */
fun Application.graphQLModule(environment: Environment<*, *>) {
    install(CORS) {
        HttpMethod.DefaultMethods.forEach {
            allowMethod(it)
        }
        allowHeaders { true }
        allowOrigins { true }
        allowNonSimpleContentTypes = true
        allowCredentials = true
        allowSameOrigin = true
        anyHost()
    }

    install(Compression) {
        gzip()
    }

    install(WebSockets) {
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = JacksonWebsocketContentConverter()
    }

    install(GraphQL) {
        schema {
            packages = listOf(
                "it.unibo.alchemist.boundary.graphql.schema",
            )
            queries = listOf(
                EnvironmentQueries(environment),
                NodeQueries(environment),
            )
            mutations = listOf(
                SimulationControl(environment),
            )
            subscriptions = listOf(
                EnvironmentSubscriptions(environment),
                NodeSubscriptions(environment),
            )
            hooks = FlowSubscriptionSchemaGeneratorHooks()
        }
        server {
            contextFactory = DefaultKtorGraphQLContextFactory()
        }
        engine {
            exceptionHandler = DataFetcherExceptionHandler {
                CompletableFuture.completedFuture(DataFetcherExceptionHandlerResult.newResult().build())
            }
            dataFetcherFactoryProvider = CustomDataFetcherFactoryProvider()
        }
    }
}

private class CustomDataFetcherFactoryProvider : SimpleKotlinDataFetcherFactoryProvider() {
    override fun functionDataFetcherFactory(target: Any?, kClass: KClass<*>, kFunction: KFunction<*>): DataFetcherFactory<Any?> =
        DataFetcherFactory {
            CustomFunctionFetcher(
                target = target,
                fn = kFunction,
            )
        }
}

private class CustomFunctionFetcher(
    target: Any?,
    private val fn: KFunction<*>,
) : FunctionDataFetcher(target, fn) {
    override fun runBlockingFunction(parameterValues: Map<KParameter, Any?>): Any? = retryTillNoException(
        parameterValues
    )

    private fun retryTillNoException(parameterValues: Map<KParameter, Any?>): Any? {
        var exCaught: Boolean
        var toReturn: Any? = null
        do {
            try {
                toReturn = fn.callBy(parameterValues)
                exCaught = false
            } catch (e: Exception) {
                exCaught = true
            }

        } while (exCaught)
        return toReturn
    }
}
