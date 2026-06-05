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
}

application {
    mainClass = "com.voxelgame.VoxelGame"
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// 设置运行时的JVM参数
tasks.withType<JavaExec> {
    systemProperty("java.library.path", "build/libs")
}
