plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.achelm.offmusicplayer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.achelm.offmusicplayer"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.14.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")

    // For Bottom Navigation
    implementation("com.github.ibrahimsn98:SmoothBottomBar:1.7.9")

    // For Blur Image
    implementation("jp.wasabeef:blurry:4.0.1")

    // For Notification
    implementation("androidx.media:media:1.7.0")

    // For storing objects in shared preferences
    implementation("com.google.code.gson:gson:2.10")

    // For circular seekbar, volume controls
    implementation("com.github.lukelorusso:VerticalSeekBar:1.2.7")

    // Lottie Lib for Animation
    implementation("com.airbnb.android:lottie:6.1.0")

    // For Cicle Image
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // For The AdMob
    implementation("com.google.android.gms:play-services-ads:23.0.0")

    // For Rating the App
    implementation("com.google.android.play:review:2.0.1")

}