import Util.id
import Util.isMultiplatform
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream
import java.util.*

plugins {
    application
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.collektive)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.multiJvmTesting)
    alias(libs.plugins.taskTree)
    alias(libs.plugins.publishOnCentral)
}

allprojects {
    repositories {
        mavenCentral()
    }
    with(rootProject.libs.plugins) {
        if (project.isMultiplatform) {
            apply(plugin = kotlin.multiplatform.id)
        } else {
            apply(plugin = kotlin.jvm.id)
        }
        apply(plugin = kotlin.qa.id)
        apply(plugin = publishOnCentral.id)
    }
}

/*
 * Only required if you plan to use Protelis, remove otherwise
 */
sourceSets {
    main {
        dependencies {
            libs.alchemist.apply {
                implementation(this)
                implementation(euclidean.geometry)
                implementation(ui.tooling)
                implementation(swingui)
            }
            implementation(project(":alchemist-monitor"))
            implementation(libs.bundles.collektive)
            implementation(kotlin("reflect"))
        }
        resources {
            srcDir("src/main/protelis")
        }
    }
}

val usesJvm: Int = File(File(projectDir, "docker/sim"), "Dockerfile")
    .readLines()
    .first { it.isNotBlank() }
    .let {
        Regex("FROM\\s+eclipse-temurin:(\\d+)\\s*$").find(it)?.groups?.get(1)?.value
            ?: throw IllegalStateException("Cannot read information on the JVM to use.")
    }
    .toInt()

multiJvm {
    jvmVersionForCompilation.set(usesJvm)
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

// Heap size estimation for batches
val maxHeap: Long? by project
val heap: Long = maxHeap ?: if (System.getProperty("os.name").lowercase().contains("linux")) {
    ByteArrayOutputStream().use { output ->
        exec {
            executable = "bash"
            args = listOf("-c", "cat /proc/meminfo | grep MemAvailable | grep -o '[0-9]*'")
            standardOutput = output
        }
        output.toString().trim().toLong() / 1024
    }.also { println("Detected ${it}MB RAM available.") } * 9 / 10
} else {
    // Guess 16GB RAM of which 2 used by the OS
    14 * 1024L
}
val taskSizeFromProject: Int? by project
val taskSize = taskSizeFromProject ?: 512
val threadCount = maxOf(1, minOf(Runtime.getRuntime().availableProcessors(), heap.toInt() / taskSize))

fun String.capitalizeString(): String =
    this.replaceFirstChar {
        if (it.isLowerCase()) {
            it.titlecase(
                Locale.getDefault(),
            )
        } else {
            it.toString()
        }
    }

/**
 * The initial port for the GraphQL monitor. Will be incremented for each simulation.
 */
val startingGraphQLPort = 1313
/*
 * Scan the folder with the simulation files, and create a task for each one of them.
 */
File(project.projectDir.path + "/src/main/yaml").listFiles()
    ?.filter { it.extension == "yml" }
    ?.sortedBy { it.nameWithoutExtension }
    ?.forEach {
        fun basetask(name: String, additionalConfiguration: JavaExec.() -> Unit = {}) = tasks.register<JavaExec>(name) {
            description = "Launches graphic simulation ${it.nameWithoutExtension}"
            mainClass.set("it.unibo.alchemist.Alchemist")
            classpath = sourceSets["main"].runtimeClasspath
            args("run", it.absolutePath)
            javaLauncher.set(
                javaToolchains.launcherFor {
                    languageVersion.set(JavaLanguageVersion.of(multiJvm.latestJava))
                },
            )
            if (System.getenv("CI") == "true") {
                args("--override", "terminate: { type: AfterTime, parameters: [2] } ")
            } else {
                this.additionalConfiguration()
            }
        }
        val capitalizedName = it.nameWithoutExtension.capitalizeString()

        val default by basetask("run${capitalizedName}Experiment")
    }

tasks.withType(KotlinCompile::class).all {
    kotlinOptions.freeCompilerArgs = listOf("-Xcontext-receivers")
}

val copyFromVMC by tasks.registering(Copy::class) {
    listOf("src", "docker", "effects").forEach {
        copy {
            from(File(rootProject.projectDir, "vmc-experiments/$it"))
            into(File(project.projectDir, it))
        }
    }
}
