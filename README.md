# CVDS G3 - Módulo de Préstamos Deportivos en el Coliseo - Mod #4 

## Equipo de Desarrollo

### Squad Ares
-  Allan Steef Contreras   
-  David Santiago Castro
-  Juan Esteban Cely
-  Juan David Zambrano


## Introducción


Este repositorio contiene un microservicio backend en **Java con Spring Boot**, parte de una arquitectura de microservicios con **API Gateway**. La finalidad de este modulo es facilitar a los miembros de la comunidad institucional la reserva y el préstamo de equipos deportivos disponibles en el coliseo, optimizando el uso y control de estos recursos. Los usuarios pueden solicitar y gestionar sus préstamos desde su perfil institucional, mientras que los funcionarios de bienestar se encargan de administrar la disponibilidad, verificar el estado de los equipos y generar reportes para mejorar la gestión del inventario.

### Funcionalidades principales:

- Solicitud y registro de préstamos.
- Consulta y reserva de equipos disponibles.
- Verificación y control del estado de los equipos.
- Reportes y notificaciones.


## Tecnología y Herramientas Utilizadas 
- **Lenguaje:** Java 17
- **Base de Datos:** MongoDB Atlas
- **Entorno de Desarrollo**: IntelliJ IDEA
- **DevOps:** Azure y GitHub Actions
- **Diagramas:** Astah y Miro



## Instalación
1. Clonar este repositorio:

   ```bash
   $ git clone https://github.com/daviidc29/colliseum.git
   ```

2. Entrar en el directorio:
   ```bash
   > cd colliseum
   ```

3. Ejecutar el proyecto con Maven:
   ```bash
   > mvn spring-boot:run
   ```

4. La aplicación estará corriendo en http://localhost:8080 por defecto.

5. La documentación **Swagger** está disponible en [Documentacion Swagger](https://colliseum-gvh2h4bbd8bgcbfm.brazilsouth-01.azurewebsites.net/swagger-ui/index.html) 




