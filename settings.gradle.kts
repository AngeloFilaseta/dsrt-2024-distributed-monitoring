/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
plugins {
    id("com.gradle.develocity") version "3.17.2"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.0.4"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include(
    "alchemist-loading",
    "alchemist-graphql",
    "alchemist-graphql-surrogates",
    "alchemist-monitor",
)
rootProject.name = "dsrt-2024-distributed-monitoring"

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
        uploadInBackground = !System.getenv("CI").toBoolean()
    }
}

gitHooks {
    commitMsg { conventionalCommits() }
    preCommit {
        tasks("ktlintCheck", "--parallel")
    }
    createHooks(overwriteExisting = true)
}
