/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.component.props

import it.unibo.alchemist.component.props.state.GraphQLControllerProps
import it.unibo.alchemist.component.props.state.SubscriptionProps

/**
 * Props for the Info component.
 */
external interface InfoProps : GraphQLControllerProps, SubscriptionProps
