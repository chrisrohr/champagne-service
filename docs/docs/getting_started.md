# Getting Started

## Prerequisites

* Postgres database (tested on 15.x)
* Java 17+ (if building/running from source)
* Node 18+ (if building/running from source)

## Configuration

The service component will need some configuration items set in order to run successfully. These should be set in a file
named `config.yml`. The following configuration items are available:

| Name                       | Default                | Required | Description                                                                                                                                         |
|----------------------------|------------------------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| database                   | none                   | yes      | Database connection information (See https://www.dropwizard.io/en/stable/manual/configuration.html#database)                                        |
| jwtCacheSpec               | `expireAfterWrite=60m` | yes      | Configuration for the JWT tokens being generated. Default expires after 60 minutes                                                                  |
| jwtCookieAuth              | none                   | no       | JWT cookie configuration information (See https://github.com/dhatim/dropwizard-jwt-cookie-authentication#edit-you-apps-dropwizard-yaml-config-file) |
| auditCleanup.initialDelay  | 30s                    | no       | Initial time to wait for cleanup job to start after server startup                                                                                  |
| auditCleanup.intervalDelay | 1d                     | no       | How often to run the cleanup job                                                                                                                    |
| auditRecordsMaxRetain      | 30d                    | no       | How long audit records should be retained.                                                                                                          |

## Building and Running from Source

* Clone the repo from [https://github.com/kiwiproject/champagne-service.git](https://github.com/kiwiproject/champagne-service.git)

### Service side

* Build the service:

```shell
cd service
mvn package --DskipTests
``` 

* Setup the database:

```shell
java -jar target/champagne-service-<version>.jar db migrate config.yml
```

* Run the service:

```shell
java -jar target/champagne-service-<version>.jar server config.yml
```

### UI side

* Run the UI

```shell
cd ui
npm run dev
```

* Access the UI

Open your browser and go to `http://localhost:5173`
