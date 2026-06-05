plugins {
    java
    application
}

group = "com.voxelgame"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.lwjgl.lwjgl:lwjgl:2.9.3")
    implementation("org.lwjgl.lwjgl:lwjgl_util:2.9.3")
    implementation("org.lwjgl.lwjgl:lwjgl-platform:2.9.3")
    runtimeOnly("org.lwjgl.lwjgl:lwjgl-platform:2.9.3:natives-linux")
    runtimeOnly("org.lwjgl.lwjgl:lwjgl-platform:2.9.3:natives-windows")
    runtimeOnly("org.lwjgl.lwjgl:lwjgl-platform:2.9.3:natives-macos")
    
    // JOML for modern math operations
    implementation("org.joml:joml:1.10.5")
}

application {
    mainClass = "com.voxelgame.VoxelGame"
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<JavaExec> {
    systemProperty("java.library.path", "build/libs")
}
