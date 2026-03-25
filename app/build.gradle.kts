import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun signingValue(propertyName: String, envName: String): String? {
    return localProperties.getProperty(propertyName)
        ?.takeIf { it.isNotBlank() }
        ?: System.getenv(envName)?.takeIf { it.isNotBlank() }
}

val releaseStoreFilePath = signingValue("release.storeFile", "ENCYE_RELEASE_STORE_FILE")
val releaseStorePassword = signingValue("release.storePassword", "ENCYE_RELEASE_STORE_PASSWORD")
val releaseKeyAlias = signingValue("release.keyAlias", "ENCYE_RELEASE_KEY_ALIAS")
val releaseKeyPassword = signingValue("release.keyPassword", "ENCYE_RELEASE_KEY_PASSWORD")

android {
    namespace = "com.selegic.encye"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.selegic.encye"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "0.1.$versionCode"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (releaseStoreFilePath != null) {
                storeFile = rootProject.file(releaseStoreFilePath)
            }
            this.storePassword = releaseStorePassword
            this.keyAlias = releaseKeyAlias
            this.keyPassword = releaseKeyPassword
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "r8-rules.pro"
            )
//            buildConfigField("String", "BASE_URL", "\"https://your.api.url/\"")
            buildConfigField("String", "BASE_URL", "\"https://test.server.encye.com/\"")
            signingConfig = signingConfigs.getByName("release")
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
        debug {
            buildConfigField("String", "BASE_URL", "\"https://test.server.encye.com/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    buildFeatures {
        buildConfig = true
    }
}

gradle.taskGraph.whenReady {
    val isReleaseRequested = allTasks.any { task ->
        task.project.path == ":app" && task.name.contains("Release", ignoreCase = true)
    }

    if (isReleaseRequested) {
        val missingSigningValues = listOf(
            "release.storeFile or ENCYE_RELEASE_STORE_FILE" to (
                releaseStoreFilePath?.let { rootProject.file(it).exists() } == true
            ),
            "release.storePassword or ENCYE_RELEASE_STORE_PASSWORD" to !releaseStorePassword.isNullOrBlank(),
            "release.keyAlias or ENCYE_RELEASE_KEY_ALIAS" to !releaseKeyAlias.isNullOrBlank(),
            "release.keyPassword or ENCYE_RELEASE_KEY_PASSWORD" to !releaseKeyPassword.isNullOrBlank()
        )
            .filterNot { (_, present) -> present }
            .map { (name, _) -> name }

        if (missingSigningValues.isNotEmpty()) {
            error(
                "Release signing is not configured. Missing: ${missingSigningValues.joinToString()}. " +
                    "Add them to local.properties or set the matching environment variables."
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(platform(libs.firebase.bom))

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)

    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.kotlinx.serialization.core)

    implementation(libs.androidx.paging.runtime)
    // optional - Jetpack Compose integration
    implementation(libs.androidx.paging.compose)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.compose)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.datastore.preferences)
    implementation("io.github.malikshairali:native-html:1.0.0")
    implementation("be.digitalia.compose.htmlconverter:htmlconverter:1.1.0")
    implementation("com.google.android.gms:play-services-auth:21.5.1")

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)
}
