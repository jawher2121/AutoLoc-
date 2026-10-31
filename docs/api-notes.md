# Notes — Atelier 5 : API REST, Postman et Swagger

## A. Endpoints

Le préfixe `/api` est défini une seule fois (`server.servlet.context-path=/api`). Les erreurs sont
produites par `GlobalExceptionHandler` : `ResourceNotFoundException` → 404, `IllegalArgumentException` → 400.
Un POST sur `/api/paiements` reçoit 405 (Method Not Allowed).

| Contrôleur | Verbe et route | Service appelé | Succès | Erreurs |
|---|---|---|---|---|
| AgenceController | GET /api/agences | findAll() | 200 | — |
| AgenceController | GET /api/agences/{id} | findById(id) | 200 | 404 |
| AgenceController | POST /api/agences | create(entité) | 201 | 400 |
| AgenceController | PUT /api/agences/{id} | update(id, entité) | 200 | 404, 400 |
| AgenceController | DELETE /api/agences/{id} | deleteById(id) | 204 | 404 |
| EmployeController | GET /api/employes | findAll() | 200 | — |
| EmployeController | GET /api/employes/{id} | findById(id) | 200 | 404 |
| EmployeController | POST /api/employes | create(entité) | 201 | 400 |
| EmployeController | PUT /api/employes/{id} | update(id, entité) | 200 | 404, 400 |
| EmployeController | DELETE /api/employes/{id} | deleteById(id) | 204 | 404 |
| VehiculeController | GET /api/vehicules | findAll() | 200 | — |
| VehiculeController | GET /api/vehicules/{id} | findById(id) | 200 | 404 |
| VehiculeController | POST /api/vehicules | create(entité) | 201 | 400 |
| VehiculeController | PUT /api/vehicules/{id} | update(id, entité) | 200 | 404, 400 |
| VehiculeController | DELETE /api/vehicules/{id} | deleteById(id) | 204 | 404 |
| EquipementController | GET /api/equipements | findAll() | 200 | — |
| EquipementController | GET /api/equipements/{id} | findById(id) | 200 | 404 |
| EquipementController | POST /api/equipements | create(entité) | 201 | 400 |
| EquipementController | PUT /api/equipements/{id} | update(id, entité) | 200 | 404, 400 |
| EquipementController | DELETE /api/equipements/{id} | deleteById(id) | 204 | 404 |
| ClientController | GET /api/clients | findAll() | 200 | — |
| ClientController | GET /api/clients/{id} | findById(id) | 200 | 404 |
| ClientController | POST /api/clients | create(entité) | 201 | 400 |
| ClientController | PUT /api/clients/{id} | update(id, entité) | 200 | 404, 400 |
| ClientController | DELETE /api/clients/{id} | deleteById(id) | 204 | 404 |
| ReservationController | GET /api/reservations | findAll() | 200 | — |
| ReservationController | GET /api/reservations/{id} | findById(id) | 200 | 404 |
| ReservationController | POST /api/reservations | create(entité) | 201 | 400 |
| ReservationController | PUT /api/reservations/{id} | update(id, entité) | 200 | 404, 400 |
| ReservationController | DELETE /api/reservations/{id} | deleteById(id) | 204 | 404 |
| ContratController | GET /api/contrats | findAll() | 200 | — |
| ContratController | GET /api/contrats/{id} | findById(id) | 200 | 404 |
| ContratController | POST /api/contrats | create(entité) | 201 | 400 |
| ContratController | PUT /api/contrats/{id} | update(id, entité) | 200 | 404, 400 |
| ContratController | DELETE /api/contrats/{id} | deleteById(id) | 204 | 404 |
| PaiementController | GET /api/paiements | findAll() | 200 | — |
| PaiementController | GET /api/paiements/{id} | findById(id) | 200 | 404 |
| MaintenanceController | GET /api/maintenances | findAll() | 200 | — |
| MaintenanceController | GET /api/maintenances/{id} | findById(id) | 200 | 404 |
| MaintenanceController | POST /api/maintenances | create(entité) | 201 | 400 |
| MaintenanceController | PUT /api/maintenances/{id} | update(id, entité) | 200 | 404, 400 |
| MaintenanceController | DELETE /api/maintenances/{id} | deleteById(id) | 204 | 404 |

## B. Annotations Jackson

| Association | Annotation | Justification |
|---|---|---|
| Contrat.paiements ↔ Paiement.contrat | `@JsonManagedReference("contrat-paiements")` côté liste, `@JsonBackReference("contrat-paiements")` côté parent | Composition : les paiements se créent dans le JSON de leur contrat, et le lien retour est rétabli à la lecture sans boucle. |
| Agence.vehicules, Agence.employes | `@JsonIgnore` sur la liste | L'enfant désigne son agence par identifiant ; la liste côté inverse ferait boucler la sérialisation. |
| Client.reservations | `@JsonIgnore` sur la liste | Même raison ; une réservation désigne son client par `{"client": {"idClient": 1}}`. |
| Vehicule.maintenances | `@JsonIgnore` sur la liste | Même raison. |
| Equipement.vehicules | `@JsonIgnore` sur la liste | Côté inverse du ManyToMany ; évite la boucle Vehicule ↔ Equipement. |
| Reservation.contrat | `@JsonIgnore` | Côté inverse du OneToOne ; le contrat désigne sa réservation (côté propriétaire, sans annotation). |

**Différence.** `@JsonBackReference` supprime la propriété à la sérialisation et la **réaffecte automatiquement
au parent à la désérialisation** : adaptée à une composition où l'enfant est créé dans le JSON du parent.
`@JsonIgnore` ignore la propriété dans les deux sens : adaptée à une liste côté inverse, quand l'enfant est créé seul
et désigne son parent par un identifiant. Sur `Reservation.client`, `@JsonBackReference` serait une erreur : la propriété
serait ignorée en lecture et le POST créerait une réservation sans client.

Propriété provisoire : `spring.jackson.serialization.fail-on-empty-beans=false` (proxies LAZY sérialisés avec
`"hibernateLazyInitializer": {}`), appelée à disparaître avec les DTO.

## C. Postman et Swagger

- **Postman** sert à tester : requêtes rejouables avec assertions. Exemple de l'atelier : le scénario « Créer un contrat avec ses paiements » puis « Supprimer » et « Consulter un paiement » → 404, qui prouve la cascade REMOVE.
- **Swagger UI** sert à documenter et explorer : le contrat est synchronisé avec le code. Exemple : la section Schemas décrit `Contrat` à partir de l'entité, et les `@ApiResponse` affichent les codes 404 et 400 de chaque endpoint.

## D. Anomalies SonarQube for IDE

À compléter avec les anomalies réellement relevées sur `web.controller` (au moins deux).

| Anomalie | Règle / explication | Correction apportée |
|---|---|---|
| à compléter | à compléter | à compléter |
| à compléter | à compléter | à compléter |

## E. Questions de compréhension

1. **Quel composant reçoit les requêtes ?** Le `DispatcherServlet`, point d'entrée unique ; il interroge le `HandlerMapping`, qui retrouve la méthode du contrôleur à partir du verbe HTTP et de l'URL.
2. **Pourquoi dépendre de `IContratService` ?** Le contrôleur reste mince : il délègue la logique métier au service, par interface (faible couplage), sans accéder au repository ni connaître l'implémentation.
3. **201, 204, 404 et 500.** POST crée une ressource → 201 Created ; DELETE réussi n'a rien à renvoyer → 204 No Content. Une ressource absente est une erreur du client, traduite en 404 par le `GlobalExceptionHandler` ; un 500 signalerait un bug du serveur.
4. **Paiement en lecture seule.** Un paiement n'existe qu'attaché à son contrat (composition) : il se crée et se retire via le Contrat. Un POST sur `/api/paiements` reçoit 405 Method Not Allowed.
5. **Risques de l'exposition des entités.** Couplage entre l'API et la base, champs internes visibles, boucles de sérialisation, problèmes de proxies LAZY ; les DTO (Chapitre 10) règlent cela. `Contrat.paiements` utilise `@JsonManagedReference` car les paiements sont créés dans le JSON de leur contrat (composition), alors que `Agence.vehicules` utilise `@JsonIgnore` car un véhicule est créé seul et désigne son agence par identifiant.
