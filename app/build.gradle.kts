plugins {
    id("com.android.application")
}

android {
    namespace = "com.itzdfplayer.capturerequestinjector"
    compileSdk = 36 // Can be modified as needed

    defaultConfig {
        applicationId = "com.itzdfplayer.capturerequestinjector"
        minSdk = 29
        versionCode = 112
        versionName = "1.1.2"
    }

    lint {
        targetSdk = 36
    }

    packaging {
        resources {
            merges += "META-INF/xposed/*"
            excludes += "**"
        }
        dex {
            useLegacyPackaging = true
        }
    }

    signingConfigs {
        create("release") {
            val keystoreFileEnv = System.getenv("KEYSTORE_FILE")
            val keystorePasswordEnv = System.getenv("KEYSTORE_PASSWORD")
            val keyAliasEnv = System.getenv("KEY_ALIAS")
            val keyPasswordEnv = System.getenv("KEY_PASSWORD")

            if (keystoreFileEnv != null && keystorePasswordEnv != null && keyAliasEnv != null && keyPasswordEnv != null) {
                storeFile = file(keystoreFileEnv)
                storePassword = keystorePasswordEnv
                keyAlias = keyAliasEnv
                keyPassword = keyPasswordEnv
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    lint {
        checkReleaseBuilds = false
    }

    dependenciesInfo {
        includeInApk = false
    }
}

dependencies {
    compileOnly("androidx.annotation:annotation:1.10.0")
    compileOnly("io.github.libxposed:api:102.0.0")
    
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.2")
    implementation("com.google.android.material:material:1.14.0")
}
