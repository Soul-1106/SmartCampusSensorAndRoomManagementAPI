# Smart Campus Sensor and Room Management API

A RESTful web service for managing university campus rooms, their installed environmental sensors, and recording sensor readings. Built using **Java 17**, **Jakarta EE 10 / JAX-RS (Jersey 3)**, and deployed on **Apache Tomcat**.

## Technology Stack

- **Java Version:** 17 or higher
- **Server:** Apache Tomcat 11 (also backwards compatible with Tomcat 10.1.x)
- **Framework:** Jersey 3 (JAX-RS / Jakarta RESTful Web Services)
- **Build Tool:** Maven

## API Overview

The API is served with a base path of `/api/v1`.

### Key Endpoints:
- `GET /api/v1/` - **Discovery Resource**: Returns API metadata, version, and links to available resources.
- `/api/v1/rooms` - **Room Actions**: Sub-resources for adding, deleting, and getting room profiles.
- `/api/v1/sensors` - **Sensor Actions**: Sub-resources for managing individual sensors.
- `/api/v1/readings` - **Reading Actions**: Endpoints to submit and query sensor data.

*(Refer to `org.example.Resources.java` for exact implementation endpoints and payload requirements).*

## How to Build and Run (NetBeans)

NetBeans makes working with this project straightforward using its In-Place / Exploded Deployment:

1. **Add Tomcat to NetBeans:** Go to `Tools` > `Servers` > `Add Server`, choose *Apache Tomcat or TomEE*, and point it to your Tomcat 11 installation directory.
2. **Set Project Target:** Right-click the project `SmartCampusSensorAndRoomManagementAPI` -> `Properties` -> `Run`. Select your **Tomcat 11** server from the dropdown. 
3. **Run:** Click the **Run Project** button (green arrow) in NetBeans. It will automatically build and start the server.
4. **Access the API:** The API will be active depending on your Context Path, for example: `http://localhost:8080/SmartCampusSensorAndRoomManagementAPI-1.0-SNAPSHOT/api/v1/`

## How to Build and Run (Command Line / Manual)

If you are not using an IDE and want to manually build and deploy the `.war` package:

1. Build the project using Maven:
   ```bash
   mvn clean package
   ```
2. Once the build finishes, locate the generated WAR file:
   `target/SmartCampusSensorAndRoomManagementAPI-1.0-SNAPSHOT.war`
3. Copy this `.war` file directly into your Tomcat's `webapps` directory:
   ```bash
   cp target/SmartCampusSensorAndRoomManagementAPI-1.0-SNAPSHOT.war /path/to/tomcat11/webapps/
   ```
4. Start Tomcat (`bin/startup.bat` or `bin/startup.sh`). It will automatically extract and deploy your application.
5. Access your API!

## Development Notes

- **Data Store**: Data is temporarily stored in-memory via the `DataStore` class during the server runtime.
- **Exceptions**: Controlled custom exception mappers (`org.example.Exceptions`) handle HTTP Error Responses with robust messaging.
- WAR final name set to `ROOT` to preserve base URL

## Build and Deploy in NetBeans

1. Open this folder as a Maven project in NetBeans.
2. In Projects view, right-click project -> Clean and Build.
3. Confirm artifact exists at `target/ROOT.war`.
4. Right-click project -> Run (or Deploy) to your configured Tomcat 10.1 server.
5. Open:
   - `http://localhost:8080/api/v1`

## Manual Tomcat Deploy (Alternative)

1. Build project:
   - `mvn clean package`
2. Copy `target/ROOT.war` into Tomcat `webapps`.
3. Start Tomcat.
4. Test discovery endpoint:
   - `GET http://localhost:8080/api/v1`

## Quick API Smoke Test

Create room:

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
```

List rooms:

```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

## Notes

- If your terminal cannot run `mvn`, use NetBeans build actions instead.
- If deployment path becomes `/<project-name>/api/v1`, ensure the WAR is named exactly `ROOT.war`.
- This project is in-memory only; data resets when server restarts.
