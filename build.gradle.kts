plugins {
    java
    application
}

group = "io.log2jv"
version = "0.6.1"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClass.set("io.log2jv.Demo")
}

tasks.withType<JavaCompile> {
    options.release.set(11)
    options.encoding = "UTF-8"
}

tasks.withType<JavaExec> {
    jvmArgs("-Dfile.encoding=UTF-8")
}

tasks.test {
    jvmArgs("-Dfile.encoding=UTF-8")
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
        )
    }
}

tasks.register("printJarPath") {
    dependsOn(tasks.jar)
    doLast {
        println("JAR готов: ${tasks.jar.get().archiveFile.get().asFile}")
    }
}