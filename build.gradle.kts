plugins {
    java
    application
}

group = "com.mojang"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    // LWJGL 2.9.0
    implementation("org.lwjgl.lwjgl:lwjgl:2.9.0")
    implementation("org.lwjgl.lwjgl:lwjgl_util:2.9.0")
    
    // JInput
    implementation("net.java.jinput:jinput:2.0.5")
    
    // 原生库（运行时）
    runtimeOnly("org.lwjgl.lwjgl:lwjgl-platform:2.9.0:natives-linux")
    
    // JInput 原生库
    runtimeOnly("net.java.jinput:jinput-platform:2.0.5:natives-linux")
}

application {
    mainClass = "com.mojang.rubydung.RubyDung"
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
