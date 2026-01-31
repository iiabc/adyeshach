repositories {
    maven("https://maven.blamejared.com/")
    maven("https://maven.nucleoid.xyz/")
}

dependencies {
    compileOnly(project(":project:common"))
    compileOnly(project(":project:common-impl"))
    compileOnly(project(":project:module-editor"))
    compileOnly("io.github.toxicity188:bettermodel:1.15.2") {
        exclude(group = "org.jetbrains.kotlin")
    }
    compileOnly("org.joml:joml:1.10.5")
}

taboolib { subproject = true }

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "21"
    }
}

@Suppress("DEPRECATION")
configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
