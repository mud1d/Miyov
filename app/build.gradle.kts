plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android { namespace = "uz.miyov.pet"; compileSdk = 35
 defaultConfig { applicationId = "uz.miyov.pet"; minSdk = 26; targetSdk = 28; versionCode = 5; versionName = "0.5-articulated-walk" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}

dependencies { testImplementation("junit:junit:4.13.2") }
