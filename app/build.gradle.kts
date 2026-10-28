import java.util.Properties
import java.security.MessageDigest

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

// 지원자용(디버그) 빌드에서 앱이 그림 AI를 직접 부르는 키. local.properties(깃 제외)에만 두고 릴리스에는 넣지 않는다.
// 이 키가 든 APK는 누구나 풀어볼 수 있으므로 공개 링크로 배포하지 않고, 사용 한도를 건 전용 키를 쓴다.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
// Public QA APKs never embed a provider key. A private developer build requires an explicit opt-in.
val includeDirectImageKey = providers.gradleProperty("includeDirectImageKey").orNull == "true"
// Separate private evaluation build; public debug QA and every release keep these tools off.
val enablePrivatePilotTools = providers.gradleProperty("enablePrivatePilotTools").orNull == "true"
val openAiKey: String = if (includeDirectImageKey) localProps.getProperty("OPENAI_API_KEY", "").trim() else ""
val openAiImageModel: String = localProps.getProperty("OPENAI_IMAGE_MODEL", "gpt-image-2").trim()

android {
    namespace = "kr.voicemate.malitda"
    compileSdk = 36

    defaultConfig {
        applicationId = "kr.voicemate.malitda"
        minSdk = 26
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = 13
        versionName = "0.3.4"
        // 그림 만들기용 R4 서버 주소. 키는 서버에만 있고 앱에는 넣지 않는다. 릴리스는 비워 기능을 끈다.
        buildConfigField("String", "R4_SERVER_URL", "\"\"")
        buildConfigField("boolean", "PILOT_TOOLS_ENABLED", "false")
        buildConfigField("String", "OPENAI_API_KEY", "\"\"")
        buildConfigField("String", "OPENAI_IMAGE_MODEL", "\"$openAiImageModel\"")
        vectorDrawables { useSupportLibrary = true }
        // 제출용 빌드: -PsubmitAbis=arm 이면 x86_64(에뮬레이터용)를 뺀다
        val arm = listOf("arm64-v8a", "armeabi-v7a")
        ndk { abiFilters += if (project.findProperty("submitAbis") == "arm") arm else arm + "x86_64" }
        externalNativeBuild {
            cmake {
                val whisperDir = (project.findProperty("whisperCppDir") as String?) ?: rootProject.file("third_party/whisper.cpp").absolutePath
                arguments += listOf("-DWHISPER_CPP_DIR=${whisperDir.replace('\\', '/')}", "-DANDROID_STL=c++_static")
                // 디버그 빌드도 인식 엔진은 최적화한다. ggml 핵심 연산은 C 파일이라 cFlags가 없으면 -O0으로 빌드돼
                // 지원자용(디버그) APK에서 말하기 인식이 수십 초씩 걸렸다(2026-10-26 실기기 피드백).
                cppFlags += listOf("-O2", "-DNDEBUG")
                cFlags += listOf("-O2", "-DNDEBUG")
            }
        }
    }

    ndkVersion = "27.2.12479018"
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    signingConfigs {
        create("release") {
            if (keystoreProps.isNotEmpty()) {
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystoreProps.isNotEmpty()) signingConfig = signingConfigs.getByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("boolean", "PILOT_TOOLS_ENABLED", enablePrivatePilotTools.toString())
            // adb reverse tcp:8769 tcp:8769 로 PC의 R4 서버에 연결한다.
            buildConfigField("String", "R4_SERVER_URL", "\"http://127.0.0.1:8769\"")
            // 키가 있으면 USB·PC 서버 없이 앱이 바로 그림을 만든다(없으면 R4 서버 방식).
            buildConfigField("String", "OPENAI_API_KEY", "\"$openAiKey\"")
        }
    }

    // 마이그레이션 테스트가 내보낸 스키마(1.json·2.json)를 읽는다.
    sourceSets { getByName("androidTest").assets.srcDir("$projectDir/schemas") }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += setOf("META-INF/AL2.0", "META-INF/LGPL2.1", "META-INF/DEPENDENCIES")
        jniLibs.useLegacyPackaging = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    // room-testing의 스키마 읽기가 kotlinx-serialization 버전 불일치로 깨지지 않도록 맞춘다(PR #1).
    implementation(platform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.8.1"))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.vosk.android)
    implementation("${libs.jna.get()}@aar")
    implementation(libs.sqlcipher.android)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    // 계측 테스트(PR #1에서 이식): 마이그레이션·저장소 검증
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test:core:1.7.0")
    androidTestImplementation("androidx.room:room-testing:2.8.2")
}

// A previously warm emulator hid missing model assets. Every packaged APK must carry both local engines.
val verifyBundledSpeechModels by tasks.registering {
    val manifest = rootProject.file("scripts/model-sha256.properties")
    inputs.file(manifest)
    doLast {
        val checksums = Properties().apply { manifest.inputStream().use { load(it) } }
        checksums.forEach { relative, expected ->
            val model = file("src/main/assets/$relative")
            check(model.isFile) { "Speech model missing: $relative. Run scripts/prepare-models.ps1 before packaging." }
            val digest = MessageDigest.getInstance("SHA-256")
            model.inputStream().use { input ->
                val bytes = ByteArray(1 shl 16)
                while (true) { val n = input.read(bytes); if (n < 0) break; digest.update(bytes, 0, n) }
            }
            val actual = digest.digest().joinToString("") { "%02x".format(it) }
            check(actual == expected.toString()) { "Speech model checksum mismatch: $relative" }
        }
    }
}
tasks.configureEach {
    if (name.matches(Regex("merge(?:Debug|Release)Assets"))) dependsOn(verifyBundledSpeechModels)
}
