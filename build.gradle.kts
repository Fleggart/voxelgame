plugins {
    java
    application
}

group = "com.voxelgame"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.lwjgl.lwjgl:lwjgl:2.9.0")
    implementation("org.lwjgl.lwjgl:lwjgl_util:2.9.0")
    implementation("net.java.jinput:jinput:2.0.5")
    
    // 只包含 Linux 和 Windows 的原生库（跳过 macOS）
    runtimeOnly("org.lwjgl.lwjgl:lwjgl-platform:2.9.0:natives-windows")
    runtimeOnly("org.lwjgl.lwjgl:lwjgl-platform:2.9.0:natives-linux")
    // runtimeOnly("org.lwjgl.lwjgl:lwjgl-platform:2.9.0:natives-macos")  // 注释掉
    
    runtimeOnly("net.java.jinput:jinput-platform:2.0.5:natives-windows")
    runtimeOnly("net.java.jinput:jinput-platform:2.0.5:natives-linux")
    // runtimeOnly("net.java.jinput:jinput-platform:2.0.5:natives-macos")  // 注释掉
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
    exclude("**/libjinput*.so")
    exclude("**/lwjgl*.dll")
    exclude("**/jinput*.dll")
    exclude("**/liblwjgl*.dylib")
    exclude("**/libjinput*.dylib")
}
