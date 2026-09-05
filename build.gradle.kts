plugins {
    `java-library`
    application
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "io.github.stepanmail1999-ctrl"
version = "0.6.2"

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

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
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
            "Implementation-Title" to "Log2JV",
            "Implementation-Version" to project.version,
        )
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        groupId = group.toString(),
        artifactId = "log2jv",
        version = version.toString()
    )

    pom {
        name.set("Log2JV")
        description.set("Simple zero-dependency Java logging library")
        inceptionYear.set("2026")
        url.set("https://github.com/stepanmail1999-ctrl/Log2JV")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("stepanmail1999-ctrl")
                name.set("Stepan")
                email.set("stepanmail1999@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/stepanmail1999-ctrl/Log2JV")
            connection.set("scm:git:git://github.com/stepanmail1999-ctrl/Log2JV.git")
            developerConnection.set("scm:git:ssh://github.com/stepanmail1999-ctrl/Log2JV.git")
        }
    }
}

tasks.register("printJarPath") {
    dependsOn(tasks.jar)
    doLast {
        println("JAR: ${tasks.jar.get().archiveFile.get().asFile}")
    }
}