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
    
    // 原生库 - 需要根据平台选择
    runtimeOnly("org.lwjgl.lwjgl:lwjgl-platform:2.9.0:natives-windows")
    runtimeOnly("org.lwjgl.lwjgl:lwjgl-platform:2.9.0:natives-linux")
    runtimeOnly("org.lwjgl.lwjgl:lwjgl-platform:2.9.0:natives-macos")
    
    runtimeOnly("net.java.jinput:jinput-platform:2.0.5:natives-windows")
    runtimeOnly("net.java.jinput:jinput-platform:2.0.5:natives-linux")
    runtimeOnly("net.java.jinput:jinput-platform:2.0.5:natives-macos")
}

application {
    mainClass = "com.voxelgame.VoxelGame"
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// 这个任务会创建一个包含所有依赖的 JAR（包括原生库）
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
