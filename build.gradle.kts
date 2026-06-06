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
    // LWJGL 2.x 核心库
    implementation("org.lwjgl.lwjgl:lwjgl:2.9.3")
    implementation("org.lwjgl.lwjgl:lwjgl_util:2.9.3")
    
    // JOML 数学库
    implementation("org.joml:joml:1.10.5")
    
    // Linux 原生库（只需要这个）
    runtimeOnly("org.lwjgl.lwjgl:lwjgl-platform:2.9.3:natives-linux")
}

application {
    mainClass = "com.voxelgame.VoxelGame"
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// Linux 下不需要手动提取 natives，直接设置 java.library.path 即可
tasks.named<JavaExec>("run") {
    systemProperty("java.library.path", System.getProperty("java.library.path"))
}
