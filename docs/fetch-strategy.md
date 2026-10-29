# Stratégie de fetch et de cascade — AutoLoc

Toutes les associations sont en `FetchType.LAZY` : on ne charge un objet lié que lorsque le code
en a besoin. Pour `@ManyToOne` et `@OneToOne`, JPA est EAGER par défaut ; on le force donc
explicitement en LAZY pour éviter de charger toute une chaîne d'objets liés.

Règle appliquée pour la cascade : elle n'est utilisée que si l'enfant ne peut pas vivre sans son
parent (composition) ou pour simplifier l'enregistrement d'objets nouveaux. Aucune cascade
`REMOVE` n'est appliquée sur les associations où l'objet lié existe indépendamment.

| Association | Fetch | Cascade | Justification |
|---|---|---|---|
| Contrat → Paiement | LAZY | ALL + orphanRemoval | Un paiement n'existe que rattaché à son contrat (composition) : supprimer le contrat supprime ses paiements, et retirer un paiement de la liste le supprime en base. |
| Agence → Vehicule | LAZY | Aucune | Un véhicule survit à la suppression de son agence (il peut être réaffecté). Une agence peut avoir beaucoup de véhicules : la liste n'est chargée qu'à la demande. |
| Agence → Employe | LAZY | Aucune | Supprimer une agence ne doit pas supprimer ses employés, qui peuvent être réaffectés. Liste chargée à la demande. |
| Vehicule ↔ Equipement | LAZY | Aucune | Les équipements sont partagés entre plusieurs véhicules : une cascade `REMOVE` supprimerait des équipements encore utilisés ailleurs. `Set` pour éviter les doublons ; supprimer un véhicule ne supprime que ses lignes de la table `vehicule_equipement`. |
| Client → Reservation | LAZY | PERSIST | Enregistrer un client avec de nouvelles réservations les enregistre aussi. Pas de `REMOVE` : l'historique des réservations ne doit pas disparaître avec le client. |
| Reservation → Vehicule | LAZY | Aucune | Le véhicule existe indépendamment des réservations qui le concernent. `@ManyToOne` forcé en LAZY (EAGER par défaut). |
| Reservation ↔ Contrat | LAZY | ALL (côté Reservation) | Un contrat n'a de sens que pour sa réservation : il suit son cycle de vie. Remarque : côté inverse d'un `@OneToOne`, Hibernate charge le contrat immédiatement malgré LAZY (il doit savoir s'il existe) ; le LAZY est pleinement effectif du côté propriétaire (Contrat → Reservation). |
| Vehicule → Maintenance | LAZY | PERSIST | Enregistrer un véhicule enregistre ses nouvelles maintenances. Pas de `REMOVE` : l'historique d'entretien est conservé. |
