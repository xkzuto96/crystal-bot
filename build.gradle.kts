plugins {
    java
}

group = "dev.crystalbot"
version = "1.0.1"

base {
    archivesName.set("crystal-bot")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.citizensnpcs.co/repo")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("net.citizensnpcs:citizens-main:2.0.37-SNAPSHOT") {
        exclude(group = "org.spigotmc", module = "spigot")
        exclude(group = "org.spigotmc", module = "spigot-api")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.register<Copy>("prepareModrinthUpload") {
    group = "distribution"
    description = "Builds and copies the plugin jar into build/modrinth for Modrinth upload."
    dependsOn(tasks.named("jar"))
    from(tasks.named<Jar>("jar").map { it.archiveFile })
    into(layout.buildDirectory.dir("modrinth"))
    rename { "crystal-bot-${project.version}.jar" }
}
