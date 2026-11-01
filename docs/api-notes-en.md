# Notes — Atelier 5: REST API, Postman and Swagger

## A. Endpoints

The `/api` prefix is set once in `application.properties` (`server.servlet.context-path=/api`), so the controllers
only write `/contrats`, `/agences`, and so on. The controllers never create errors themselves: when a service throws
`ResourceNotFoundException`, `GlobalExceptionHandler` answers 404, and for `IllegalArgumentException` it answers 400.
A `POST` on `/api/paiements` gets 405 (Method Not Allowed), because that controller has no POST method.

| Controller | Verb and route | Service called | Success | Errors |
|---|---|---|---|---|
| AgenceController | GET /api/agences | findAll() | 200 | — |
| AgenceController | GET /api/agences/{id} | findById(id) | 200 | 404 |
| AgenceController | POST /api/agences | create(entity) | 201 | 400 |
| AgenceController | PUT /api/agences/{id} | update(id, entity) | 200 | 404, 400 |
| AgenceController | DELETE /api/agences/{id} | deleteById(id) | 204 | 404 |
| EmployeController | GET /api/employes | findAll() | 200 | — |
| EmployeController | GET /api/employes/{id} | findById(id) | 200 | 404 |
| EmployeController | POST /api/employes | create(entity) | 201 | 400 |
| EmployeController | PUT /api/employes/{id} | update(id, entity) | 200 | 404, 400 |
| EmployeController | DELETE /api/employes/{id} | deleteById(id) | 204 | 404 |
| VehiculeController | GET /api/vehicules | findAll() | 200 | — |
| VehiculeController | GET /api/vehicules/{id} | findById(id) | 200 | 404 |
| VehiculeController | POST /api/vehicules | create(entity) | 201 | 400 |
| VehiculeController | PUT /api/vehicules/{id} | update(id, entity) | 200 | 404, 400 |
| VehiculeController | DELETE /api/vehicules/{id} | deleteById(id) | 204 | 404 |
| EquipementController | GET /api/equipements | findAll() | 200 | — |
| EquipementController | GET /api/equipements/{id} | findById(id) | 200 | 404 |
| EquipementController | POST /api/equipements | create(entity) | 201 | 400 |
| EquipementController | PUT /api/equipements/{id} | update(id, entity) | 200 | 404, 400 |
| EquipementController | DELETE /api/equipements/{id} | deleteById(id) | 204 | 404 |
| ClientController | GET /api/clients | findAll() | 200 | — |
| ClientController | GET /api/clients/{id} | findById(id) | 200 | 404 |
| ClientController | POST /api/clients | create(entity) | 201 | 400 |
| ClientController | PUT /api/clients/{id} | update(id, entity) | 200 | 404, 400 |
| ClientController | DELETE /api/clients/{id} | deleteById(id) | 204 | 404 |
| ReservationController | GET /api/reservations | findAll() | 200 | — |
| ReservationController | GET /api/reservations/{id} | findById(id) | 200 | 404 |
| ReservationController | POST /api/reservations | create(entity) | 201 | 400 |
| ReservationController | PUT /api/reservations/{id} | update(id, entity) | 200 | 404, 400 |
| ReservationController | DELETE /api/reservations/{id} | deleteById(id) | 204 | 404 |
| ContratController | GET /api/contrats | findAll() | 200 | — |
| ContratController | GET /api/contrats/{id} | findById(id) | 200 | 404 |
| ContratController | POST /api/contrats | create(entity) | 201 | 400 |
| ContratController | PUT /api/contrats/{id} | update(id, entity) | 200 | 404, 400 |
| ContratController | DELETE /api/contrats/{id} | deleteById(id) | 204 | 404 |
| PaiementController | GET /api/paiements | findAll() | 200 | — |
| PaiementController | GET /api/paiements/{id} | findById(id) | 200 | 404 |
| MaintenanceController | GET /api/maintenances | findAll() | 200 | — |
| MaintenanceController | GET /api/maintenances/{id} | findById(id) | 200 | 404 |
| MaintenanceController | POST /api/maintenances | create(entity) | 201 | 400 |
| MaintenanceController | PUT /api/maintenances/{id} | update(id, entity) | 200 | 404, 400 |
| MaintenanceController | DELETE /api/maintenances/{id} | deleteById(id) | 204 | 404 |

## B. Jackson annotations

**The problem.** A `Contrat` contains its list of `Paiement`, and each `Paiement` points back to its `Contrat`.
When Jackson turns this into JSON, it goes contract → payments → contract → payments... forever, and the
application crashes with a `StackOverflowError`. Every two-way link needs one side to be cut.

| Link | Annotation | Why |
|---|---|---|
| Contrat.paiements ↔ Paiement.contrat | `@JsonManagedReference("contrat-paiements")` on the list, `@JsonBackReference("contrat-paiements")` on the parent | The payments are created inside the contract's JSON, so Jackson must write them and also reconnect each payment to its contract when reading. |
| Agence.vehicules and Agence.employes | `@JsonIgnore` on the lists | A vehicle or employee is created alone and names its agency by id, so the agency never needs to list them. |
| Client.reservations | `@JsonIgnore` on the list | Same reason: a reservation names its client by id (`{"client": {"idClient": 1}}`). |
| Vehicule.maintenances | `@JsonIgnore` on the list | Same reason. |
| Equipement.vehicules | `@JsonIgnore` on the list | Inverse side of the many-to-many link; avoids a Vehicule ↔ Equipement loop. |
| Reservation.contrat | `@JsonIgnore` | Inverse side of the one-to-one link; the contract names its reservation (the owner side, with no annotation). |

**Difference between the two.** `@JsonBackReference` hides the field when writing JSON but **fills it in again when
reading JSON** (the child gets its parent back). It fits a composition, where the child is created inside the parent's JSON.
`@JsonIgnore` simply ignores the field in both directions. It fits a list on the inverse side, where the child is created alone and
points to its parent by id. On `Reservation.client`, `@JsonBackReference` would be a mistake: the client would be ignored when reading, and a
POST would create a reservation with no client.

**Temporary setting.** `spring.jackson.serialization.fail-on-empty-beans=false` lets the lazy-loaded links be written as JSON
(they appear with `"hibernateLazyInitializer": {}`). It will disappear when DTOs are introduced.

## C. Postman and Swagger

- **Postman is for testing.** Its requests can be replayed, and each one checks the answer with a test. In this project the whole collection
  (73 tests) ran in about 5 seconds with no failure. One scenario proves the cascade: a contract is created with two payments in a single
  request, then deleted, and the payment can no longer be found (404).
- **Swagger UI is for documenting and exploring.** It is generated from the code, so it never goes out of date. For example, its
  Schemas section describes `Contrat` straight from the entity, and the `@ApiResponse` annotations show the 404 and 400 answers of each endpoint.

## D. SonarQube for IDE findings

Complete this with what your own panel shows after you analyze `web.controller`.

| Finding | Rule / explanation | Fix applied |
|---|---|---|
| Persistent entities used as `@RequestBody` arguments (to confirm in your panel) | S4684: a client could set any field of an entity, and these objects are written to the database | Not fixed on purpose: the TP exposes entities temporarily, and DTOs replace them in Chapter 10 |
| to complete | to complete | to complete |

## E. Comprehension questions

1. **Which component receives all requests?** The `DispatcherServlet`, the single entry point. It asks the `HandlerMapping`, which finds the controller method from the HTTP verb and the URL.
2. **Why does the controller depend on `IContratService`?** A controller should stay thin: it passes the work to the service and nothing else. Depending on the interface keeps the layers separate, so the controller knows neither the repository nor the implementation.
3. **Why 201, 204, 404 and not 500?** A POST creates something, so it answers 201 Created. A successful DELETE has nothing to return, so it answers 204 No Content. A missing resource is the client's mistake, so the handler turns it into 404. A 500 would mean a bug on the server.
4. **Why is Paiement read-only?** A payment only exists as part of its contract (composition), so it is created or removed through the `Contrat`. A `POST` on `/api/paiements` is refused with 405 Method Not Allowed.
5. **What are the risks of exposing entities, and why two Jackson styles?** Exposing entities ties the API to the database, shows internal fields, can loop forever, and breaks on lazy-loaded proxies. DTOs (Chapter 10) solve this. `Contrat.paiements` uses `@JsonManagedReference` because payments are created inside the contract's JSON (composition). `Agence.vehicules` uses `@JsonIgnore` because a vehicle is created alone and names its agency by id.
