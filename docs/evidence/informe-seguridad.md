# Informe

### 1. ¿Dónde aparecen en su proyecto codificar, cifrar y hashear? ¿Por qué la contraseña se hashea y no se cifra?

*Respuesta:*

Codificar aparece en el JWT: el header y payload van en Base64 y la clave se convierte a bytes con UTF-8 (JwtService). Sin embargo,
solo es una representación que no tiene secreto. Mientras que hashear aparece en las contraseñas: BCryptPasswordEncoder (SecurityConfig) generando
el valor guardado en password_hash y el login compara con matches además de que la firma HS256 del token también usa un hash con clave (HMAC).

No se usa cifrado porque no se gurdan datos reversibles y el cifrado en HTTPS solo existiría en producción; por lo que
la contraseña se hashea para que el servidor no necesite recuperarla, solo comprobar que coincide con la clave del usuario. Mientras que al
cifrar, quien robe la base y la clave, obtendría todas las contraseñas pero con BCrypt es lento y usa "sal" sin tener la posibilidad de devolverse.

### 2. Decodificaron el payload de su token: ¿qué contiene, qué pasó cuando cambiaron el role a ADMIN y por qué el servidor lo detectó?

*Respuesta:*

Al decodificar en jwt.io se vió que contiene 'sub' (correo), 'role', 'iss' (oficioya), 'iat' y 'exp' (expiración de 30 minutos). Luego se cambió
'role' a 'ADMIN' para mantener la firma original y se llamó al endpoint '/api/v1/admin/usuarios': esperando un error 401. Por lo tanto, el servidor
lo detecta ya que la firma es un HMAC calculado sobre header y payload con el JWT_SECRET definido. Así que si se modifica el payload, la firma deja de
coincidir y sin la clave no se puede generar un nueva firma dado que el payload solo se lee mas no modificarse.

### 3. Den un ejemplo de 401, 403 y 404 de su API. ¿Por qué una solicitud ajena responde 404 y no 403?

*Respuesta:*

* **Error 401:** un login con clave errónea o cualquier endpoint sin token o con token vencido
* **Error 403:** un token de CONTRATANTE llamando al endpoint /api/v1/admin/usuarios o un ADMIN llama al endpoint a /solicitudes
* **Error 404:** un contratante pide una solicitud de otro rol

Se responde 404 y no 403 porque el error 403 confirma que la solicitud existe pero pertenece a otro rol, enumerándose los ids.
Así mismo, la consulta ya incluye al participante por lo que la solicitud ajena y existente dan la misma respuesta.

### 4. ¿Por qué el contratante no envía su propio id al crear una solicitud?

*Respuesta:*

Porque cualquier dato del cuerpo lo controla el cliente y se puede falsificar. Si POST /api/v1/solicitudes recibiera un contratanteId, Nicolas
podrí crear solicitudes a nombre de Juan solo cambiando ese número. Por eso SolicitudRequest solo trae trabajadorId y descripcion, y el contratante sale del token: SolicitudController pasa auth.getName() (el sub del JWT, que va firmado) y SolicitudService busca al usuario
con ese correo. Como la firma no se puede alterar sin el JWT_SECRET, el contratante siempre es quien inició sesión.

### 5. ¿Por qué desactivaron CSRF? ¿Qué limitación tiene el cierre de sesión sin revocación de tokens?

*Respuesta:*

-CSRF aprovecha que el navegador envía por sí solo las cookies de sesión aunque la petición la haga otra pgina. Nuestra API no usa cookies: en SecurityConfig lasesión es STATELESS y el token viaja en el header Authorization, que el navegador nunca agrega por su cuenta. Por eso csrf.disable() no abre ese ataque.

-La limitación es que el servidor no guarda los tokens emitidos, así que no puede invalidarlos. Cerrar sesión solo borra el token del cliente: si alguien y lo copió, lo puede seguir usando hasta que venza. Para revocarlo haría falta una lista negra de tokens o tokens de acceso más cortos con un refresh token que sí se pueda revocar.


### 6. ¿Qué pasaría si la clave del JWT se sube a GitHub o es corta? ¿Qué cambiarían para producción?

*Respuesta:* 

Si la clave se sube a GitHub, cualquiera podría fabricar un token con la firma correcta y el rol que quiera, incluso ADMIN, y el servidor no lo distinguiría de uno real. Borrarla en un commit nuevo no basta porque queda en el historial: habría que cambiarla, y con eso todos los tokens anteriores dejan de funcionar. Si es corta, se puede adivinar probando claves con un token copiado; por eso exigimos 32 bytes o más y la leemos de la variable JWT_SECRET. Para producción guardaríamos los secretos en un gestor como Azure Key Vault, usaríamos HTTPS, tokens de acceso cortos con refresh token revocable, y quitaríamos lo que es solo de desarrollo, como la consola de H2 y ddl-auto=update.