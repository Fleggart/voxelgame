#version 110

uniform sampler2D texture;
uniform bool hasTexture;
uniform bool hasColor;

varying vec2 texCoord;
varying vec3 color;

void main() {
    vec4 finalColor;
    
    if (hasTexture) {
        finalColor = texture2D(texture, texCoord);
        if (hasColor) {
            finalColor *= vec4(color, 1.0);
        }
    } else if (hasColor) {
        finalColor = vec4(color, 1.0);
    } else {
        finalColor = vec4(1.0, 1.0, 1.0, 1.0);
    }
    
    gl_FragColor = finalColor;
}
