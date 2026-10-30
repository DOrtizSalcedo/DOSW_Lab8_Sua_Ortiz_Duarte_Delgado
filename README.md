# DOSW - Laboratorio 8

OficioYa protegido con Spring Security y JWT. API REST con registro, login, perfiles, administración y solicitudes de servicio, con autorización por rol y por propiedad.

## Cómo ejecutar

Requisitos: Java 21, Maven y PostgreSQL con una base de datos llamada `oficioya_security`.

```bash
cd oficioya
mvn spring-boot:run
```

La API queda en `http://localhost:8080/api/v1`. Al arrancar, `AdminSeeder` crea el usuario `ADMIN` con `ADMIN_CORREO` y `ADMIN_PASSWORD` si todavía no existe.

## Variables de entorno

| Variable | Obligatoria | Descripción |
| --- | --- | --- |
| `DB_URL` | No | URL JDBC de PostgreSQL. Por defecto `jdbc:postgresql://localhost:5432/oficioya_security` |
| `DB_USER` | No | Usuario de PostgreSQL. Por defecto `postgres` |
| `DB_PASSWORD` | Sí | Contraseña de PostgreSQL |
| `JWT_SECRET` | Sí | Clave para firmar los tokens (HS256), de 32 bytes o más. Se puede generar con `openssl rand -base64 48` |
| `ADMIN_CORREO` | Sí | Correo del administrador inicial |
| `ADMIN_PASSWORD` | Sí | Contraseña del administrador inicial |

Ningún valor real se guarda en el repositorio.

## Contrato de endpoints

Todas las rutas usan el prefijo `/api/v1`. Las protegidas exigen el header `Authorization: Bearer <token>`, y todos los errores responden con el mismo formato (`timestamp`, `status`, `error`, `mensaje`, `ruta`).

| Método | Ruta | Acceso | Éxito | Errores |
| --- | --- | --- | --- | --- |
| `POST` | `/auth/registro` | Público | `201` | `400`, `409` |
| `POST` | `/auth/login` | Público | `200` con JWT | `400`, `401` |
| `GET` | `/trabajadores` | Autenticado | `200` con perfiles públicos | `401` |
| `GET` | `/trabajadores/{id}` | Autenticado | `200` con perfil público | `401`, `404` |
| `GET` | `/admin/usuarios` | `ADMIN` | `200` con datos de contacto | `401`, `403` |
| `POST` | `/solicitudes` | `CONTRATANTE` | `201` | `400`, `401`, `403`, `404` |
| `GET` | `/solicitudes` | `CONTRATANTE`, `TRABAJADOR` | `200` solo las propias | `401`, `403` |
| `GET` | `/solicitudes/{id}` | Parte de la solicitud | `200` | `401`, `403`, `404` |
| `PATCH` | `/solicitudes/{id}/aceptar` | `TRABAJADOR` asignado | `200` | `401`, `403`, `404`, `409` |
| `PATCH` | `/solicitudes/{id}/rechazar` | `TRABAJADOR` asignado | `200` | `401`, `403`, `404`, `409` |
| `PATCH` | `/solicitudes/{id}/cancelar` | `CONTRATANTE` autor | `200` | `401`, `403`, `404`, `409` |

## Pruebas

```bash
cd oficioya
mvn clean verify
```

Las pruebas usan H2 en memoria (perfil `test`), así que no necesitan PostgreSQL.

Resultado: **41 pruebas, 0 fallos, 0 errores, `BUILD SUCCESS`**.

| Clase de prueba | Pruebas |
| --- | ---: |
| `AuthControllerTest` | 20 |
| `SolicitudesSeguridadTest` (PT-13 a PT-17) | 7 |
| `JwtServiceTest` | 6 |
| `UsuarioServiceTest` | 4 |
| `PerfilesSeguridadTest` | 3 |
| `OficioyaApplicationTests` | 1 |

## Documentación

- Evidencias:
  - [Contraseñas hasheadas en PostgreSQL](docs/evidence/01-password-hash.txt)
  - [Payload del JWT decodificado](docs/evidence/02-jwt-payload.txt) y [captura en jwt.io](docs/evidence/02-jwt-io.png)
  - [Respuestas 401, 403 y 404 y los ataques del bloque 8](docs/ataques.md)
  - [Resultado de `mvn clean verify`](docs/evidence/07-mvn-clean-verify.txt)
- [Matriz OWASP](docs/matiz-owasp.md)
- [Matriz de acceso del dominio de usuarios](docs/matriz-acceso.md)
- [Informe de seguridad](docs/evidence/informe-seguridad.md)
