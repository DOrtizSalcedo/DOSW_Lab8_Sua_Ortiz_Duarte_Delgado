# Matriz de acceso · User domain (OficioYa)

Endpoints de nuestro microservicio de usuarios, con los roles que pueden usarlos, la regla de propiedad o de datos ocultos y el código HTTP cuando no se cumple.

| Endpoint | Roles permitidos | Regla de propiedad o datos ocultos | Código si se incumple |
|---|---|---|---|
| POST /api/v1/users | Registro (coordinado con Identity) | Solo roles CONTRATANTE y TRABAJADOR, nunca ADMIN. Correo único y normalizado | 400 datos o rol inválido; 409 correo repetido |
| GET /api/v1/users/me | Cualquier usuario autenticado | Solo sus propios datos; la identidad sale del token | 401 |
| PUT /api/v1/users/me | Cualquier usuario autenticado | Solo edita su propio perfil y solo campos editables (no correo, estado ni roles) | 401; 400 |
| GET /api/v1/users/{id} | Cualquier usuario autenticado | Perfil básico, sin correo ni teléfono | 401; 404 |
| PUT /api/v1/users/{id}/roles | Dueño de la cuenta | Solo CONTRATANTE o TRABAJADOR, nunca ADMIN | 401; 403 si no es su cuenta o pide ADMIN; 404 |
| PATCH /api/v1/users/{id}/status | ADMIN | Desactiva o reactiva cuentas | 401; 403; 404 |
| GET /api/v1/admin/users/{id} | ADMIN | Perfil completo, con correo y teléfono | 401; 403; 404 |

## Convenciones

- **401**: no hay token o es inválido o está vencido.
- **403**: sabemos quién es, pero su rol no alcanza.
- **404**: el recurso no existe o es ajeno, para no revelar que existe.

Las rutas son una propuesta de nuestro squad; el prefijo y los nombres definitivos se confirman con el Orquestador.


## Arquitectura de seguridad

El JWT lo crea Identity cuando alguien inicia sesión. Después, cada petición pasa primero por el Orquestador, que revisa el token (que la firma sea válida y que no esté vencido). Si el token falla, el Orquestador responde 401 y la petición no llega a los microservicios.

Nuestro microservicio de usuarios aplica las reglas de su parte: cada usuario solo edita su propio perfil y el correo y el teléfono solo los ve el administrador. Todavía falta acordar con el Squad 1 cómo nos llega la identidad del usuario desde el Orquestador.

La clave con la que se firma el token va en la variable de entorno JWT_SECRET, así Identity y el Orquestador usan la misma sin que esté en el repositorio. En el repo solo hay un .env.example con los nombres de las variables, sin valores. Cómo se entrega esa clave en Azure lo van a definir los líderes técnicos con los pipelines.