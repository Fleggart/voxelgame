plugins {
    java
    application
}

group = "com.voxelgame"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.lwjgl.lwjgl:lwjgl:2.9.3") {
        exclude group: "net.java.jinput"
    }
    implementation("org.lwjgl.lwjgl:lwjgl_util:2.9.3")
    
    // 只包含 Linux 和 Windows 的原生库（跳过 macOS）
    runtimeOnly("org.lwjgl.lwjgl:lwjgl-platform:2.9.3:natives-windows")
    runtimeOnly("org.lwjgl.lwjgl:lwjgl-platform:2.9.3:natives-linux")
    // runtimeOnly("org.lwjgl.lwjgl:lwjgl-platform:2.9.3:natives-macos")  // 注释掉
}

application {
    mainClass = "com.voxelgame.VoxelGame"
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// 可选：创建 fat JAR
tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    manifest {
        attributes["Main-Class"] = "com.voxelgame.VoxelGame"
    }
    
    from(sourceSets.main.get().output)
    
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
    
    // 排除原生库文件（避免冲突）
    exclude("**/liblwjgl*.so")
    exclude("**/lwjgl*.dll")
    exclude("**/liblwjgl*.dylib")
}
