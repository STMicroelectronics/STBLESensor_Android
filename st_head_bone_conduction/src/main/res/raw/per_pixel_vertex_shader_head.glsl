uniform mat4 u_MVPMatrix;
uniform mat4 u_ModelMatrix;

attribute vec4 a_Position;
attribute vec3 a_Normal;

varying vec3 v_Position;
varying vec3 v_Normal;

void main()
{
    // Trasforma la posizione in coordinate mondo
    v_Position = vec3(u_ModelMatrix * a_Position);

    // Trasforma la normale in coordinate mondo
    v_Normal = vec3(u_ModelMatrix * vec4(a_Normal, 0.0));

    // Posizione finale per lo schermo
    gl_Position = u_MVPMatrix * a_Position;
}
