# Store Locator

Spring Boot store locator API with public store search and JWT-protected admin store management.

## Tech Stack

- Java 17
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Spring Security
- H2 in-memory database
- Maven wrapper

## Features

- List all seeded stores.
- Fetch a store by internal numeric id.
- Search public stores by:
  - city
  - postal code
  - latitude/longitude radius
  - free-form address resolved through the current geocoding service
- Filter coordinate, address, and postal-code searches by store type and services.
- Admin CRUD-style store management with JWT authentication.
- Admin store creation can use explicit coordinates or geocode the submitted address.

## Requirements

- Java 17

Check your Java version:

```bash
java -version
```

If Maven reports `JAVA_HOME` is not configured, run commands with an explicit Java 17 home. On this machine, this works:

```bash
/usr/bin/env JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.19/libexec/openjdk.jdk/Contents/Home ./mvnw test
```

## Run Locally

Start the API:

```bash
./mvnw spring-boot:run
```

The app runs on:

```text
http://localhost:8080
```

H2 console:

```text
http://localhost:8080/h2-console
```

Database connection:

```text
JDBC URL: jdbc:h2:mem:storelocatordb
User: sa
Password:
```

The database is recreated on startup and seeded from `src/main/resources/data.sql`.

## Run Tests

```bash
./mvnw test
```

Current test coverage uses Spring Boot + MockMvc integration tests.

## Public API

### List Stores

```http
GET /stores
```

Example:

```bash
curl http://localhost:8080/stores
```

### Get Store By Numeric Id

```http
GET /stores/{id}
```

Example:

```bash
curl http://localhost:8080/stores/1
```

Returns `404` when the numeric id does not exist.

### Search Stores

```http
GET /stores/search
```

Search mode precedence:

1. `address`
2. `postalCode`
3. `city`
4. `latitude` + `longitude`

If multiple location modes are provided, the first mode in that list is used.

#### City Search

```bash
curl "http://localhost:8080/stores/search?city=Boston"
```

City search performs an exact case-insensitive database match on `address_city`.

#### Postal Code Search

```bash
curl "http://localhost:8080/stores/search?postalCode=02108"
```

Postal-code search performs an exact case-insensitive database match on `address_postal_code`.

#### Coordinate Search

```bash
curl "http://localhost:8080/stores/search?latitude=42.3601&longitude=-71.0589&radiusMiles=10&limit=5"
```

Rules:

- `latitude` and `longitude` must be provided together.
- `latitude` must be between `-90` and `90`.
- `longitude` must be between `-180` and `180`.
- `radiusMiles` defaults to `10`.
- `radiusMiles` must be greater than `0` and less than or equal to `100`.
- `limit` defaults to `10`.
- `limit` must be greater than `0`.

Coordinate search uses a bounding-box query first, then calculates exact distance, sorts nearest-first, and applies the limit.

#### Address Search

```bash
curl "http://localhost:8080/stores/search?address=123%20Main%20St%20Boston%20MA%2002108%20USA&radiusMiles=1"
```

Address search resolves the free-form address through `GeocodingService`, then reuses coordinate search. The current implementation uses `StubGeocodingService`, so only known seeded/demo addresses resolve.

Known demo addresses:

- `123 Main St Boston MA 02108 USA`
- `456 Broadway New York NY 10013 USA`
- `789 Mass Ave Cambridge MA 02139 USA`
- `1 Admin Plaza Seattle WA 98101 USA`

#### Store Type And Service Filters

These filters are supported for coordinate, address, and postal-code searches:

```bash
curl "http://localhost:8080/stores/search?latitude=42.3601&longitude=-71.0589&storeTypes=Flagship&services=pickup"
```

Multiple `storeTypes` use OR logic:

```bash
curl "http://localhost:8080/stores/search?latitude=42.3601&longitude=-71.0589&storeTypes=Flagship&storeTypes=Neighborhood"
```

Multiple `services` use AND logic:

```bash
curl "http://localhost:8080/stores/search?latitude=42.3601&longitude=-71.0589&services=returns&services=repairs"
```

Service matching is full-token matching against pipe-delimited store services such as `pickup|returns|wifi`.

## Admin API

Admin endpoints are under:

```http
/api/admin/stores
```

They require:

```http
Authorization: Bearer <jwt>
```

The app has an `AdminJwtService` that generates and validates one-hour JWTs with the configured demo secret:

```properties
app.admin.security.jwt-secret=change-this-demo-secret-to-a-long-value-1234567890
```

There is not currently a public login endpoint. Tests generate tokens directly through `AdminJwtService`.

### List Admin Stores

```http
GET /api/admin/stores?page=0&size=20
```

Rules:

- `page` minimum is `0`.
- `size` minimum is `1`.
- `size` maximum is `100`.

### Get Admin Store

```http
GET /api/admin/stores/{store_id}
```

Example:

```http
GET /api/admin/stores/S001
```

`store_id` must match `S` followed by digits.

### Create Store

```http
POST /api/admin/stores
```

Example request:

```json
{
  "storeId": "S004",
  "name": "Seattle Store",
  "storeType": "Urban",
  "status": "active",
  "latitude": 47.6062,
  "longitude": -122.3321,
  "address": {
    "street": "1 Admin Plaza",
    "city": "Seattle",
    "state": "WA",
    "postalCode": "98101",
    "country": "USA"
  },
  "phone": "+1-206-555-0104",
  "services": "pickup|delivery",
  "hours": {
    "mon": "09:00-18:00",
    "tue": "09:00-18:00",
    "wed": "09:00-18:00",
    "thu": "09:00-18:00",
    "fri": "09:00-18:00",
    "sat": "10:00-16:00",
    "sun": "Closed"
  }
}
```

Coordinates can be omitted only if the address can be resolved by the configured geocoding service.

### Patch Store

```http
PATCH /api/admin/stores/{store_id}
```

Patchable fields:

- `name`
- `phone`
- `services`
- `status`
- `hours`

Example:

```json
{
  "name": "Boston Store Updated",
  "phone": "+1-617-555-9999",
  "hours": {
    "mon": "07:00-20:00"
  }
}
```

Unknown fields are rejected.

### Deactivate Store

```http
DELETE /api/admin/stores/{store_id}
```

This is a soft delete. It sets `status` to `inactive`.

## Validation Notes

- `storeId` and admin path `store_id` must match `S` followed by digits.
- `storeType` must be one of `Flagship`, `Urban`, or `Neighborhood`.
- `status` must be `active` or `inactive`.
- Hours must be `Closed` or formatted as `HH:mm-HH:mm`.
- Admin create requests reject unknown JSON fields.
- Admin patch requests require at least one supported field.

## Current Limitations

- Geocoding is stubbed for demo addresses. A production implementation should integrate a real provider such as US Census or Nominatim with timeouts, rate-limit handling, and response validation.
- Data is stored in an in-memory H2 database and resets on restart.
- Public city and postal-code search are exact database matches.
- Admin JWT generation is available as an internal service, but there is no login/token endpoint.

## Development Workflow

Before opening a PR or committing changes:

```bash
./mvnw test
git status --short
```

Do not commit local-only files such as `docs/`, `.DS_Store`, or ad hoc CSV imports unless they are explicitly part of the approved task.
