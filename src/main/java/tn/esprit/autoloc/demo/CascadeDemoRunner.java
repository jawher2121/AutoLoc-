package tn.esprit.autoloc.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.LazyInitializationException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.ModePaiement;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.repository.IContratRepository;
import tn.esprit.autoloc.repository.IPaiementRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Démonstration (Atelier 2) du cascade, de orphanRemoval et du LAZY sur Contrat -> Paiement.
 * Exécutée uniquement avec le profil Spring "demo" ; elle nettoie ses données à la fin.
 */
@Slf4j
@Component
@Profile("demo")
@RequiredArgsConstructor
public class CascadeDemoRunner implements CommandLineRunner {

    private final IContratRepository contratRepository;
    private final IPaiementRepository paiementRepository;
    private final TransactionTemplate tx;

    @Override
    public void run(String... args) {
        long contratsAvant = contratRepository.count();
        long paiementsAvant = paiementRepository.count();
        log.info("=== DEMO Contrat -> Paiement : cascade, orphanRemoval, LAZY ===");

        // 1. CASCADE PERSIST : on ne sauvegarde que le contrat, les 2 paiements suivent.
        Long idContrat = tx.execute(status -> {
            Contrat contrat = new Contrat();
            contrat.setDateSignature(LocalDate.now());
            contrat.setMontantTotal(new BigDecimal("300.00"));
            contrat.setValide(true);
            contrat.addPaiement(nouveauPaiement("100.00", ModePaiement.CARTE));
            contrat.addPaiement(nouveauPaiement("200.00", ModePaiement.ESPECES));
            return contratRepository.save(contrat).getIdContrat();
        });
        log.info("1) save(contrat) seul -> +{} contrat, +{} paiements en base (cascade PERSIST)",
                contratRepository.count() - contratsAvant,
                paiementRepository.count() - paiementsAvant);

        // 2. ORPHAN REMOVAL : retirer un paiement de la liste le supprime en base.
        tx.executeWithoutResult(status -> {
            Contrat contrat = contratRepository.findById(idContrat).orElseThrow();
            contrat.removePaiement(contrat.getPaiements().get(0));
        });
        log.info("2) removePaiement(...) -> reste {} paiement(s) en base (orphanRemoval)",
                paiementRepository.count() - paiementsAvant);

        // 3. LAZY : la liste n'est pas chargée avec le contrat ; accès hors transaction = erreur.
        Contrat detache = contratRepository.findById(idContrat).orElseThrow();
        try {
            detache.getPaiements().size();
            log.info("3) (inattendu) accès à la liste hors transaction sans erreur");
        } catch (LazyInitializationException e) {
            log.info("3) getPaiements() hors transaction -> LazyInitializationException (LAZY)");
        }

        // 4. CASCADE REMOVE : supprimer le contrat supprime ses paiements restants.
        tx.executeWithoutResult(status -> contratRepository.deleteById(idContrat));
        log.info("4) deleteById(contrat) -> {} contrat(s), {} paiement(s) en plus qu'au départ (cascade REMOVE)",
                contratRepository.count() - contratsAvant,
                paiementRepository.count() - paiementsAvant);
        log.info("=== FIN DE LA DEMO ===");
    }

    private Paiement nouveauPaiement(String montant, ModePaiement mode) {
        Paiement paiement = new Paiement();
        paiement.setMontant(new BigDecimal(montant));
        paiement.setDatePaiement(LocalDate.now());
        paiement.setModePaiement(mode);
        return paiement;
    }
}
