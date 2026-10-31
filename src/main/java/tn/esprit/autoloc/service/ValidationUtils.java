package tn.esprit.autoloc.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Règles de gestion communes aux services : chaque méthode lève une
 * IllegalArgumentException (traduite en 400 à l'Atelier 5) si la règle n'est pas respectée.
 */
final class ValidationUtils {

    private ValidationUtils() {
    }

    static void exigerNouvelle(Long id, String ressource) {
        if (id != null) {
            throw new IllegalArgumentException(
                    "Un nouveau " + ressource + " ne doit pas avoir d'identifiant");
        }
    }

    static void exigerNonVide(String valeur, String champ) {
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException("Le champ '" + champ + "' est obligatoire");
        }
    }

    static void exigerNonNul(Object valeur, String champ) {
        if (valeur == null) {
            throw new IllegalArgumentException("Le champ '" + champ + "' est obligatoire");
        }
    }

    static void exigerPositifOuNul(BigDecimal valeur, String champ) {
        exigerNonNul(valeur, champ);
        if (valeur.signum() < 0) {
            throw new IllegalArgumentException("Le champ '" + champ + "' ne doit pas être négatif");
        }
    }

    static void exigerOrdreDates(LocalDate debut, LocalDate fin) {
        if (debut != null && fin != null && fin.isBefore(debut)) {
            throw new IllegalArgumentException("La date de fin ne peut pas précéder la date de début");
        }
    }
}
