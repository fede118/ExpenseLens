import org.xml.sax.InputSource
import java.io.StringReader
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.detekt.gradle.plugin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.hilt)
    alias(libs.plugins.jacoco)
    alias(libs.plugins.google.services)
    kotlin("plugin.parcelize")
}

android {
    namespace = "com.section11.expenselens"
    compileSdk = 35

    val localProperties = getLocalProperties()

    signingConfigs {
        create("release") {
            storeFile = file(localProperties["KEYSTORE_PATH"] as String? ?: "")
            storePassword = localProperties["KEYSTORE_PASSWORD"] as String? ?: ""
            keyAlias = localProperties["KEY_ALIAS"] as String? ?: ""
            keyPassword = localProperties["KEY_PASSWORD"] as String? ?: ""
        }
    }

    defaultConfig {
        applicationId = "com.section11.expenselens"
        minSdk = 29
        targetSdk = 34
        versionCode = 5
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildFeatures.buildConfig = true
        buildConfigField("String", "GEMINI_BASE_URL", "\"${project.findProperty("GEMINI_API_BASE_URL") ?: ""}\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties["GEMINI_API_KEY"]}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${localProperties["GOOGLE_WEB_CLIENT_ID"]}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    detekt {
        buildUponDefaultConfig = true // preconfigure defaults
        allRules = true // activate all available (even unstable) rules.
        config.setFrom(files("$projectDir/config/detekt/detekt.yml"))
    }
}

private fun getLocalProperties(): Properties {
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { stream ->
            localProperties.load(stream)
        }
    } else {
        // Use environment variables if local.properties file does not exist
        localProperties["KEYSTORE_PATH"] = System.getenv("KEYSTORE_PATH")
        localProperties["KEYSTORE_PASSWORD"] = System.getenv("KEYSTORE_PASSWORD")
        localProperties["KEY_ALIAS"] = System.getenv("KEY_ALIAS")
        localProperties["KEY_PASSWORD"] = System.getenv("KEY_PASSWORD")
    }
    return localProperties
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.accompanist.permissions)
    implementation(libs.text.recognition)
    implementation(libs.retrofit2.retrofit)
    implementation(libs.converter.gson)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil.compose)

    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockk.android)
    testImplementation(libs.mockk.agent)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

jacoco {
    toolVersion = "0.8.10"
}

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        // Required for Android projects
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.create("jacocoTestReport", JacocoReport::class.java) {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R\$*.class",
        "**/BuildConfig.*",
        "**/androidX.*",
        "**/androidx/**",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/di/**/*.*",
        "**/models/**/*.*",
        "**/dto/**/*.*",
        "**/database/**/*.*",
        "**/navigation/**/*.*",
        "**/previewrepository/**/*.*",
        "**/composables/**", // exclude files in composable folders
        "**/*Composable*.*", // exclude files with "composable" in their name
        "**/ComposableSingletons*.*", // Exclude ComposableSingletons
        "**/*Application*.*", // Exclude Application classes
        "**/*Activity*.*", // Exclude all activities
        "**/ui/theme/*.*", // Exclude ui theme
        "**/ui/utils/*.*", // Exclude ui theme
        "**/*state*.*", // Exclude files with "state" in the name (case-insensitive)
        "**/*State*.*", // Covers capitalized "State"
        "**/*UiState*.*", // Exclude specific pattern for "UiState" files
        "**/framework/deserializer/*.*", // Exclude deserializers"
        "**/ExpenseLensImageCapture.*", // Exclude expenseLensImageCapture since its a wrapper of ImageCapture because it cannot be mocked, ergo I cannot test it
        "**/META-INF/**/*.*" // Exclude META-INF
    )
    val debugTree = fileTree("/build/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    val mainSrc = fileTree("$projectDir/src/main/java") {
        exclude(fileFilter)
    }

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(
        fileTree(
            mapOf(
                "dir" to "${layout.buildDirectory.get()}",
                "includes" to listOf("/jacoco/testDebugUnitTest.exec")
            )
        )
    )
}

tasks.register("checkCoverage") {
    dependsOn("jacocoTestReport")
    doLast {
        val xmlFile = file("./build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        val threshold = 80.0

        if (!xmlFile.exists()) {
            throw GradleException("JaCoCo XML report not found at ${xmlFile.absolutePath}")
        }

        // Parse the XML file while ignoring DTD
        val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        factory.isValidating = false
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)

        val builder = factory.newDocumentBuilder()

        // Set a custom EntityResolver to ignore the DTD
        builder.setEntityResolver { publicId, systemId ->
            println("Ignoring DTD: PublicId=$publicId, SystemId=$systemId")
            InputSource(StringReader(""))
        }

        val xml = builder.parse(xmlFile)

        val counters = xml.getElementsByTagName("counter")
        var totalCovered = 0
        var totalMissed = 0

        for (i in 0 until counters.length) {
            val node = counters.item(i)
            if (node.attributes.getNamedItem("type").nodeValue == "LINE") {
                totalCovered += node.attributes.getNamedItem("covered").nodeValue.toInt()
                totalMissed += node.attributes.getNamedItem("missed").nodeValue.toInt()
            }
        }

        // Calculate coverage
        val coverage = (totalCovered.toDouble() / (totalCovered + totalMissed)) * 100

        if (coverage < threshold) {
            throw GradleException(
                "Coverage is below the threshold of ${threshold}%" +
                        "\nCoverage: ${coverage.format(2)}%"
            )
        }

        println("Coverage is above the threshold of ${threshold}%")
        println("Coverage: ${coverage.format(2)}%")
    }
}

tasks.register("checkReadyForPr") {
    dependsOn("jacocoTestReport")
    dependsOn("detekt")
    dependsOn("checkCoverage")
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)