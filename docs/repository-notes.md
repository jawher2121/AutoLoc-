# Notes — Atelier 3 : couche Repository

## A. Choix d'interface

| Interface | Étend | Justification |
|---|---|---|
| IAgenceRepository | JpaRepository<Agence, Long> | CRUD complet, listes (List), tri, pagination et flush : l’interface unique retenue pour AutoLoc. |
| IEmployeRepository | JpaRepository<Employe, Long> | CRUD complet, listes (List), tri, pagination et flush : l’interface unique retenue pour AutoLoc. |
| IVehiculeRepository | JpaRepository<Vehicule, Long> | CRUD complet, listes (List), tri, pagination et flush : l’interface unique retenue pour AutoLoc. |
| IEquipementRepository | JpaRepository<Equipement, Long> | CRUD complet, listes (List), tri, pagination et flush : l’interface unique retenue pour AutoLoc. |
| IClientRepository | JpaRepository<Client, Long> | CRUD complet, listes (List), tri, pagination et flush : l’interface unique retenue pour AutoLoc. |
| IReservationRepository | JpaRepository<Reservation, Long> | CRUD complet, listes (List), tri, pagination et flush : l’interface unique retenue pour AutoLoc. |
| IContratRepository | JpaRepository<Contrat, Long> | CRUD complet, findAll renvoie une List, saveAndFlush disponible ; permet d'observer cascade et orphanRemoval. |
| IPaiementRepository | JpaRepository<Paiement, Long> | Même interface par cohérence ; sert surtout à lire les paiements, car la création et la suppression passent par le Contrat (composition). |
| IMaintenanceRepository | JpaRepository<Maintenance, Long> | CRUD complet, listes (List), tri, pagination et flush : l’interface unique retenue pour AutoLoc. |

Aucune implémentation n'est écrite : Spring Data génère un proxy au démarrage (`Found 9 JPA repository interfaces`).
`@Repository` est inutile sur ces interfaces.

## B. Points de compréhension

- **save()** : identifiant `null` → `persist` (INSERT, id attribué par MySQL) ; identifiant renseigné → `merge` (SELECT puis UPDATE si changement). On utilise l'objet retourné.
- **deleteById()** : ne lève aucune exception si l'id n'existe pas (Spring Data 3) ; il passe par le contexte de persistance, donc cascade REMOVE et orphanRemoval s'appliquent.
- **findAll()** : `Iterable` avec `CrudRepository`, `List` avec `ListCrudRepository` et `JpaRepository`.
- **Suppressions en lot** (`deleteAllInBatch`, `deleteAllByIdInBatch`) : une seule requête DELETE qui contourne le contexte de persistance. La cascade et orphanRemoval ne s'appliquent pas : supprimer ainsi des contrats laisserait des paiements orphelins ou échouerait sur la clé étrangère.
- **PagingAndSortingRepository** n'étend plus `CrudRepository` depuis Spring Data 3 : `JpaRepository` réunit les deux.

## C. Anomalies SonarQube for IDE

À compléter avec les anomalies réellement relevées dans votre IDE (au moins trois), après analyse du projet.

| Anomalie SonarQube for IDE | Règle / explication | Correction apportée |
|---|---|---|
| à compléter | à compléter | à compléter |
| à compléter | à compléter | à compléter |
| à compléter | à compléter | à compléter |
