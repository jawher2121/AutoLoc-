package tn.esprit.autoloc.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IReservationRepository;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements IReservationService {

    private final IReservationRepository reservationRepository;

    @Override
    public Reservation create(Reservation reservation) {
        ValidationUtils.exigerNouvelle(reservation.getIdReservation(), "réservation");
        verifier(reservation);
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }

    @Override
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Override
    public Reservation update(Long id, Reservation reservation) {
        Reservation existant = findById(id);
        verifier(reservation);
        existant.setDateDebut(reservation.getDateDebut());
        existant.setDateFin(reservation.getDateFin());
        existant.setStatut(reservation.getStatut());
        if (reservation.getClient() != null) {
            existant.setClient(reservation.getClient());
        }
        if (reservation.getVehicule() != null) {
            existant.setVehicule(reservation.getVehicule());
        }
        return reservationRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reservation", id);
        }
        reservationRepository.deleteById(id);
    }

    private void verifier(Reservation reservation) {
        ValidationUtils.exigerNonNul(reservation.getDateDebut(), "dateDebut");
        ValidationUtils.exigerNonNul(reservation.getDateFin(), "dateFin");
        ValidationUtils.exigerNonNul(reservation.getStatut(), "statut");
        ValidationUtils.exigerOrdreDates(reservation.getDateDebut(), reservation.getDateFin());
    }
}
