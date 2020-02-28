attribute vec4 aPosition;
attribute vec3 aNormal;
varying vec4 color;
uniform mat3 uNormalMatrix;
uniform mat4 uMVMatrix;
uniform mat4 uMVPMatrix;
uniform mat4 uMVLightMatrix;
const vec4 lightPositionWorld = vec4(0.0,0.0,-20.0,1.0);
void main(){
  vec3 normal = uNormalMatrix*aNormal;
  vec4 vertexEye = uMVMatrix*aPosition;
  vec4 lightPositionEye = uMVLightMatrix*lightPositionWorld;
  float distance = length(lightPositionEye-vertexEye);
  vec3 ds = normalize(vec3(lightPositionEye-vertexEye));
  float diffuseIntensity = max(dot(normal,ds), 0.20);
  diffuseIntensity = diffuseIntensity*(1.0 / (1.0 + 0.005*distance * distance));
  color = diffuseIntensity*vec4(0.2f, 0.709803922f, 0.89039216f, 1.0f);
  gl_Position = uMVPMatrix*aPosition;
}