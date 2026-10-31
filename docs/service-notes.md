# Notes — Atelier 4 : couche Service, IoC et DI

## A. Modes d'injection comparés

| Critère | Constructeur | Attribut (`@Autowired`) |
|---|---|---|
| Champ `final` possible | Oui | Non |
| Dépendance visible dans la signature de la classe | Oui | Non (cachée dans un champ privé) |
| Utilisable avec `new` hors conteneur sans dépendance `null` | Il faut fournir la dépendance : l'objet est toujours complet | Oui, mais la dépendance reste `null` (NullPointerException) |
| Signalé par SonarQube for IDE | Non | Oui (règle S6813, à confirmer dans votre IDE) |

## B. Services

| Service | Dépendances injectées | Mode | Justification |
|---|---|---|---|
| AgenceServiceImpl | IAgenceRepository | Constructeur (`@RequiredArgsConstructor`, champ `final`) | Dépendance obligatoire et typée par l'interface, objet jamais incomplet, aucun `new`. |
| EmployeServiceImpl | IEmployeRepository | Constructeur (`@RequiredArgsConstructor`, champ `final`) | Dépendance obligatoire et typée par l'interface, objet jamais incomplet, aucun `new`. |
| VehiculeServiceImpl | IVehiculeRepository | Constructeur (`@RequiredArgsConstructor`, champ `final`) | Dépendance obligatoire et typée par l'interface, objet jamais incomplet, aucun `new`. |
| EquipementServiceImpl | IEquipementRepository | Constructeur (`@RequiredArgsConstructor`, champ `final`) | Dépendance obligatoire et typée par l'interface, objet jamais incomplet, aucun `new`. |
| ClientServiceImpl | IClientRepository | Constructeur (`@RequiredArgsConstructor`, champ `final`) | Dépendance obligatoire et typée par l'interface, objet jamais incomplet, aucun `new`. |
| ReservationServiceImpl | IReservationRepository | Constructeur (`@RequiredArgsConstructor`, champ `final`) | Dépendance obligatoire et typée par l'interface, objet jamais incomplet, aucun `new`. |
| ContratServiceImpl | IContratRepository | Constructeur (`@RequiredArgsConstructor`, champ `final`) | Dépendance obligatoire et typée par l'interface, objet jamais incomplet, aucun `new`. |
| PaiementServiceImpl | IPaiementRepository | Constructeur (`@RequiredArgsConstructor`, champ `final`) | Lecture seule : un paiement se crée, se modifie et se supprime à travers son contrat. |
| MaintenanceServiceImpl | IMaintenanceRepository | Constructeur (`@RequiredArgsConstructor`, champ `final`) | Dépendance obligatoire et typée par l'interface, objet jamais incomplet, aucun `new`. |

Chaque service est un singleton sans état : aucun attribut modifiable. Les règles de gestion
(identifiant refusé à la création, champs obligatoires, valeurs négatives, ordre des dates)
sont levées en `IllegalArgumentException` ; l'absence d'une ressource en `ResourceNotFoundException`.

## C. Messages d'erreur du conteneur

1. **Message 1 — `NoSuchBeanDefinitionException`** (aucun bean `IContratService`) : `ContratServiceImpl` n'est pas annotée `@Service` (ou une autre annotation de stéréotype), ou elle est dans un package hors de `tn.esprit.autoloc` donc jamais scannée. Correction : ajouter `@Service` et vérifier le package.
2. **Message 2 — `NoUniqueBeanDefinitionException`** (2 beans : `emailNotificateur`, `smsNotificateur`) : (a) `@Primary` **sur la classe** du bean à utiliser par défaut ; (b) `@Qualifier("emailNotificateur")` **sur le paramètre du constructeur** du contrôleur, pour un choix explicite.
3. **Message 3 — dépendance circulaire** (`clientServiceImpl` ↔ `reservationServiceImpl`) : avec l'injection par constructeur, chaque bean doit exister complet pour construire l'autre : impossible de créer l'un sans l'autre. Piste : extraire la responsabilité commune dans un troisième service dont dépendent les deux, ou faire passer l'un par le repository de l'autre.

## D. Anomalies SonarQube for IDE

À confirmer et compléter avec ce que votre IDE affiche réellement (au moins trois). Les deux premières
sont celles des expériences de l'atelier.

| Anomalie | Règle / explication | Correction apportée |
|---|---|---|
| Injection par attribut (`@Autowired` sur un champ) | S6813 (à vérifier) : préférer l'injection par constructeur | Retour à `@RequiredArgsConstructor` et champ `final` |
| `RuntimeException` levée à la place de l'exception métier | S112 (à vérifier) : ne pas lever d'exception générique | Retour à `ResourceNotFoundException` |
| à compléter | à compléter | à compléter |

## E. Questions de compréhension

1. **Où est le `new` de `ContratServiceImpl` ?** Dans le conteneur Spring : au démarrage, le scan détecte `@Service`, puis le conteneur appelle le constructeur et lui fournit le proxy du repository.
2. **Pourquoi dépendre de `IContratService` ?** Pour ne dépendre que d'une abstraction : on peut changer l'implémentation sans toucher au contrôleur, et on respecte les couches (le contrôleur ne connaît ni le repository ni l'implémentation).
3. **Pourquoi un singleton reste-t-il sans état ?** Il est partagé par tous les threads. Exemple de bug : un attribut `contratCourant` modifié par une requête puis lu par une autre fournit les données d'un autre utilisateur.
4. **Pourquoi `update` charge-t-il puis recopie ?** `save` avec l'objet reçu ferait un `merge` qui écraserait toutes les colonnes absentes du JSON (elles deviendraient `null`) et, avec `orphanRemoval`, pourrait vider la collection de paiements.
5. **Différences.** `@Component` se place sur une classe qu'on écrit ; `@Bean` sur une méthode, pour une classe qu'on ne peut pas annoter (bibliothèque, JDK). `@Primary` désigne le candidat par défaut, posé sur la classe ; `@Qualifier` choisit explicitement un bean, posé à l'injection. `IPaiementService` n'expose que la lecture car un paiement n'existe qu'à travers son contrat (composition) : créer ou retirer un paiement passe par le `Contrat`.
