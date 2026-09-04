precision mediump float;

uniform vec3 u_MaterialKa;   // Ambient color
uniform vec3 u_MaterialKd;   // Diffuse color
uniform vec3 u_MaterialKs;   // Specular color
uniform float u_MaterialNs;  // Shininess

uniform vec3 u_LightDirection;
uniform vec3 u_LightColor;
uniform float u_AmbientStrength;

varying vec3 v_Position;
varying vec3 v_Normal;

void main()
{
    // Normalizza i vettori in ingresso
    vec3 n = normalize(v_Normal);
    vec3 l = normalize(u_LightDirection);

    // 1. Componente Ambient
    vec3 ambient = u_AmbientStrength * u_MaterialKa * u_LightColor;

    // 2. Componente Diffuse (Lambertian)
    float nDotL = max(dot(n, l), 0.0);
    vec3 diffuse = nDotL * u_MaterialKd * u_LightColor;

    // 3. Componente Specular (Blinn-Phong)
    // Assumiamo la camera in (0,0,1) rispetto al modello per semplicità
    vec3 viewDir = normalize(vec3(0.0, 0.0, 1.0) - v_Position);
    vec3 halfDir = normalize(l + viewDir);
    float spec = pow(max(dot(n, halfDir), 0.0), u_MaterialNs);
    vec3 specular = spec * u_MaterialKs * u_LightColor;

    // Colore finale
    vec3 finalColor = ambient + diffuse + specular;

    gl_FragColor = vec4(finalColor, 1.0);
}
