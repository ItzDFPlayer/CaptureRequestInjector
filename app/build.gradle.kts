plugins {
    id("com.android.application")
}

android {
    namespace = "com.itzdfplayer.capturerequestinjector"
    compileSdk = 36 // Can be modified as needed

    defaultConfig {
        applicationId = "com.itzdfplayer.capturerequestinjector"
        minSdk = 29
        versionCode = 110
        versionName = "1.1.0"
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
            val keystoreFile = System.getenv("KEYSTORE_FILE")
            val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
            val keyAlias = System.getenv("KEY_ALIAS")
            val keyPassword = System.getenv("KEY_PASSWORD")

            if (keystoreFile != null && keystorePassword != null && keyAlias != null && keyPassword != null) {
                storeFile = file(keystoreFile)
                storePassword = keystorePassword
                keyAlias = keyAlias
                keyPassword = keyPassword
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
