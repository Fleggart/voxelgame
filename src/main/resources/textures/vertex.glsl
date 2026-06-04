#version 110

attribute vec3 in_position;
attribute vec2 in_texCoord;
attribute vec3 in_color;

varying vec2 texCoord;
varying vec3 color;

uniform mat4 projection;
uniform mat4 modelview;

void main() {
    gl_Position = projection * modelview * vec4(in_position, 1.0);
    texCoord = in_texCoord;
    color = in_color;
}
