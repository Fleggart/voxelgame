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
    
    // JOML for modern math operations
    implementation("org.joml:joml:1.10.5")
    
    // Native libraries - use classifier for specific platforms
    runtimeOnly("org.lwjgl.lwjgl:lwjgl:2.9.3") {
        isTransitive = false
    }
    runtimeOnly("org.lwjgl.lwjgl:lwjgl_util:2.9.3") {
        isTransitive = false
    }
    
    // Platform-specific native libraries
    runtimeOnly("org.lwjgl.lwjgl:lwjgl-platform:2.9.3:natives-windows")
    runtimeOnly("org.lwjgl.lwjgl:lwjgl-platform:2.9.3:natives-linux")
    // macOS natives removed - not available in Maven Central
}

application {
    mainClass = "com.voxelgame.VoxelGame"
    // Add native library path
    applicationDefaultJvmArgs = listOf("-Djava.library.path=build/libs")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// Create a task to extract native libraries
tasks.register<Copy>("extractNatives") {
    from(configurations.runtimeClasspath.get().filter { 
        it.name.contains("natives") 
    })
    into("build/libs")
    eachFile {
        // Extract the native libraries from the JARs
        if (name.endsWith(".dll") || name.endsWith(".so") || name.endsWith(".dylib")) {
            // Keep the file as-is
        }
    }
    includeEmptyDirs = false
}

// Make run depend on extractNatives
tasks.named("run") {
    dependsOn("extractNatives")
    systemProperty("java.library.path", "build/libs")
}
