plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "4.1.0"
}

group = "com.infoyupay"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("mtc.expediente41s")
    mainClass.set("com.infoyupay.mtcexpediente41.Launcher")
    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=javafx.graphics"
    )
}

javafx {
    version = "26.0.1"
    modules = listOf("javafx.controls", "javafx.fxml")
}

jlink {
    options = listOf(
        "--strip-debug",
        "--compress=zip-6",
        "--no-header-files",
        "--no-man-pages"
    )
    launcher {
        name = "Expediente41"
        noConsole = true
    }
    jpackage {
        imageName = "Expediente41"
        installerName = "Expediente41"
        appVersion = project.version.toString().substringBefore('-')
        vendor = "InfoYupay SACS"
        imageOptions = listOf(
            "--description",
            "Generador de expedientes DSTT-041"
        )
    }
}

dependencies {
    /*=============*
     * Annotations *
     *=============*/
    // Source: https://mvnrepository.com/artifact/org.jetbrains/annotations
    implementation("org.jetbrains:annotations:26.1.0")

    /*===================*
     * Logging framework *
     *===================*/
    // Source: https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-api:2.0.18")
    // Source: https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.5.38")

    /*============================*
     * External files management. *
     *============================*/
    // Source: https://mvnrepository.com/artifact/org.apache.pdfbox/pdfbox
    implementation("org.apache.pdfbox:pdfbox:3.0.8")

    /*=====================*
     * Testing frameworks. *
     *=====================*/
    // Source: https://mvnrepository.com/artifact/org.junit/junit-bom
    testImplementation(platform("org.junit:junit-bom:6.1.1"))
    // Source: https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter
    testImplementation("org.junit.jupiter:junit-jupiter")
    // Source: https://mvnrepository.com/artifact/org.junit.platform/junit-platform-launcher
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // Source: https://mvnrepository.com/artifact/org.assertj/assertj-core
    testImplementation("org.assertj:assertj-core:3.27.7")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
