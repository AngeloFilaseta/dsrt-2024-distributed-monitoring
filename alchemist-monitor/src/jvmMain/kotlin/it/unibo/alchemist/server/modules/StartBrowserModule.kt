/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.modules

import io.ktor.server.engine.ApplicationEngineEnvironment
import org.slf4j.LoggerFactory
import java.awt.Desktop
import java.net.URI

private val logger = LoggerFactory.getLogger("StartBrowserModule")

/**
 * Start the default browser of the user on the server address.
 */
fun ApplicationEngineEnvironment.startBrowserModule() {
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        logger.warn("It is possible to start a Browser automatically on this Desktop.")
        connectors.forEach {
            val address = "${it.type.name.lowercase()}://${it.host}:${it.port}"
            Desktop.getDesktop().browse(URI(address))
        }
    } else {
        logger.warn("Cannot start a browser automatically on this Desktop.")
    }
}
