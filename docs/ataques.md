# Ataques a Endpoints

# Ataque 1: Recurso protegido sin token
## Error HTTP estimado: 401 [No autorizado]
### Petición: GET /api/v1/solicitudes sin cabecera Authorization
### Fecha: 2026-10-28 18:18

## Respuesta obtenida

HTTP/1.1 401

X-Content-Type-Options: nosniff

X-XSS-Protection: 0

Cache-Control: no-cache, no-store, max-age=0, must-revalidate

Pragma: no-cache

Expires: 0

X-Frame-Options: SAMEORIGIN

Content-Type: application/json;charset=UTF-8

Content-Length: 147

Date: Wed, 28 Oct 2026 23:18:13 GMT

{"timestamp":"2026-10-28T18:18:13.0329171","status":401,"error":"Unauthorized","mensaje":"Token inválido o expirado","ruta":"/api/v1/solicitudes"}

# Ataque 2: Payload alterado con "role":"ADMIN" y la firma original
## Error HTTP estimado: 401 [No autorizado]
## Payload enviado (decodificado):
{"sub":"juan@example.com","role":"ADMIN","iss":"oficioya","iat":1793229492,"exp":1793231292}

### Petición: GET /api/v1/admin/usuarios con Authorization: Bearer <TOKEN_ALTERADO>

## Respuesta obtenida

HTTP/1.1 401

X-Content-Type-Options: nosniff

X-XSS-Protection: 0

Cache-Control: no-cache, no-store, max-age=0, must-revalidate

Pragma: no-cache

Expires: 0

X-Frame-Options: SAMEORIGIN

Content-Type: application/json;charset=UTF-8

Content-Length: 150

Date: Wed, 28 Oct 2026 23:18:13 GMT

{"timestamp":"2026-10-28T18:18:13.4412971","status":401,"error":"Unauthorized","mensaje":"Token inválido o expirado","ruta":"/api/v1/admin/usuarios"}

# Ataque 3: Usar un token vencido (duración reducida temporalmente)
## Error HTTP estimado: 401 [No autorizado]
### Petición: POST /api/v1/auth/registro
### Fecha: 2026-10-28 21:45

## Comando(s) usado(s)

export APP_JWT_TTL_MINUTES=1

URL=http://localhost:8080/api/v1

#### st() { curl -s -o /dev/null -w "%{http_code}\n" "$@"; }

#### login() { curl -s -i -X POST $URL/auth/login -H "Content-Type: application/json" ->(el código sigue abajo porque se buguea)

#### -d "{\"correo\":\"1\",\"password\":\"$2\"}"; }

TOKEN=$(login trabajador@example.com ContrasenaDemo123! | tail -1 | sed -E 's/.*"accesoToken":"([^"]+)".*/\1/')
echo "${TOKEN:0:20}..."

## Respuesta obtenida

HTTP/1.1 400

X-Content-Type-Options: nosniff

X-XSS-Protection: 0

Cache-Control: no-cache, no-store, max-age=0, must-revalidate

Pragma: no-cache

Expires: 0

X-Frame-Options: SAMEORIGIN

Content-Type: application/json

Transfer-Encoding: chunked

Date: Thu, 29 Oct 2026 02:45:24 GMT

Connection: close

{"timestamp":"2026-10-28T21:45:24.2177072","status":400,"error":"Bad Request","mensaje":"Rol no permitido al registrar","ruta":"/api/v1/auth/registro"}

*Aclaración: faltan $ en los números pero markdown da errores*

# Ataque 4: Con el token de Nicolás se pide la solicitud 1 de Juan
## Error HTTP estimado: 404 [Conexión establecida pero recurso no encontrado]
### Petición: GET /api/v1/solicitudes/1 con Authorization: Bearer <TOKEN_DE_NICOLAS>
### Fecha: 2026-10-28 18:18

## Respuesta obtenida

HTTP/1.1 404

X-Content-Type-Options: nosniff

X-XSS-Protection: 0

Cache-Control: no-cache, no-store, max-age=0, must-revalidate

Pragma: no-cache

Expires: 0

X-Frame-Options: SAMEORIGIN

Content-Type: application/json

Transfer-Encoding: chunked

Date: Wed, 28 Oct 2026 23:18:13 GMT

{"timestamp":"2026-10-28T18:18:13.2124499","status":404,"error":"Not Found","mensaje":"Solicitud no encontrada","ruta":"/api/v1/solicitudes/1"}

# Ataque 5: Un CONTRATANTE consulta /admin/usuarios
## Error HTTP estimado: 403 [Autorización negada]
### Petición: GET /api/v1/admin/usuarios con Authorization: Bearer <TOKEN_DE_JUAN>
### Fecha: 2026-10-28 18:18

## Respuesta obtenida

HTTP/1.1 403

X-Content-Type-Options: nosniff

X-XSS-Protection: 0

Cache-Control: no-cache, no-store, max-age=0, must-revalidate

Pragma: no-cache

Expires: 0

X-Frame-Options: SAMEORIGIN

Content-Type: application/json;charset=UTF-8

Content-Length: 167

Date: Wed, 28 Oct 2026 23:18:13 GMT

{"timestamp":"2026-10-28T18:18:13.1124324","status":403,"error":"Forbidden","mensaje":"No tienes permisos para acceder a este recurso","ruta":"/api/v1/admin/usuarios"}

# Ataque 6: Registrarse enviando "rol": "ADMIN"
## Error HTTP estimado: 400 [Sintaxis incorrecta]
### Petición: POST /api/v1/auth/registro
### Fecha: 2026-10-28 21:50

## Comando(s) usado(s)

URL=http://localhost:8080/api/v1

st() { curl -s -o /dev/null -w "%{http_code}\n" "$@"; }

curl -i -X POST http://localhost:8080/api/v1/auth/registro   -H "Content-Type: application/json"   -d '{"nombre":"TRABAJADOR","correo":"trabajador@example.com","telefono":"3000000350","password_hash":"ContrasenaDemo123!","rol":"ADMIN","fecha_creacion":"2026-10-25"}'

## Respuesta obtenida

HTTP/1.1 400
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: SAMEORIGIN
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 29 Oct 2026 02:50:17 GMT
Connection: close

{"timestamp":"2026-10-28T21:50:17.9292459","status":400,"error":"Bad Request","mensaje":"Rol no permitido al registrar","ruta":"/api/v1/auth/registro"}

# Ataque 7: Hacer login con correo inexistente y con uno existente pero con clave errónea
## Error HTTP estimado: 401 [No autorizado]
### Petición: POST /api/v1/auth/login
### Fecha: 2026-10-28 22:01

## Comando(s) usado(s)

URL=http://localhost:8080/api/v1

login() { curl -s -i -X POST URL/auth/login -H "Content-Type: application/json" -d "{\"correo\":\"1\",\"password\":\"2\"}"; }

login nadie@example.com ContrasenaDemo123!
login trabajador@example.com claveMala123

*Aclaración: faltan $ en los números pero markdown da errores* 

## Respuesta obtenida

sleep 70
st -H "Authorization: Bearer $TOKEN" $URL/solicitudes
401

curl -s -H "Authorization: Bearer $TOKEN" $URL/solicitudes
{"timestamp":"2026-10-28T22:01:02.6564395","status":401,"error":"Unauthorized","mensaje":"Token inválido o expirado","ruta":"/api/v1/solicitudes"}




