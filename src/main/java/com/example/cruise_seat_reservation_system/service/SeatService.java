package com.example.cruise_seat_reservation_system.service;

import com.example.cruise_seat_reservation_system.model.ReservationStatus;
import com.example.cruise_seat_reservation_system.model.SeatReservation;
import com.example.cruise_seat_reservation_system.model.SeatReservationHistory;
import com.example.cruise_seat_reservation_system.repository.SeatReservationHistoryRepository;
import com.example.cruise_seat_reservation_system.repository.SeatReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatReservationRepository seatReservationRepository;

    private final SeatReservationHistoryRepository seatReservationHistoryRepository;

    private static final long EXPIRATION_TIME = 10;

    public Optional<SeatReservation> getSeat(Long seatId) {
        return seatReservationRepository.findById(seatId);
    }

//    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Transactional
    public SeatReservation reserveSeat(Long seatId, Long userId){
        SeatReservation seatReservation = seatReservationRepository.findById(seatId).orElseThrow(() -> new RuntimeException("Seat Not Found"));

        if(seatReservation.getExpirationTime() != null && seatReservation.getExpirationTime().isAfter(LocalDateTime.now())) throw new RuntimeException("Another user has reserved it already");

        SeatReservationHistory seatReservationHistory = new SeatReservationHistory();
        seatReservationHistory.setSeatReservation(seatReservation);
        seatReservationHistory.setOldStatus(seatReservation.getReservationStatus());
        seatReservationHistory.setNewStatus(ReservationStatus.PENDING);
        seatReservationHistory.setChangedByUserId(userId);
        seatReservationHistory.setChangedTimestamp(LocalDateTime.now());

        seatReservation.setReservationStatus(ReservationStatus.PENDING);
        seatReservation.setReservedByUserId(userId);
        seatReservation.setExpirationTime(LocalDateTime.now().plusMinutes(EXPIRATION_TIME));

        return seatReservation;
    }


//    @Transactional
//    public SeatReservation reserveSeat(Long seatId, Long userId) {
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime newExpirationTime = now.plusMinutes(EXPIRATION_TIME);
//
//        // Try to update the seat in one atomic statement.
//        int updatedRows = seatReservationRepository.updateSeatReservationAtomically(
//                seatId,
//                userId,
//                ReservationStatus.PENDING,
//                newExpirationTime,
//                now
//        );
//
//        // If no rows were updated, seat was not free (already reserved by someone else).
//        if (updatedRows == 0) {
//            throw new RuntimeException("Another user has reserved it already or seat not found.");
//        }
//
//        // If here, seat was successfully updated.
//        // Now we can fetch the seat from DB to return it (and to record the new state).
//        SeatReservation seatReservation = seatReservationRepository.findById(seatId)
//                .orElseThrow(() -> new RuntimeException("Seat Not Found after update!"));
//
//        // Create history record – for the old status you can decide how to handle that logic.
//        SeatReservationHistory seatReservationHistory = new SeatReservationHistory();
//        seatReservationHistory.setSeatReservation(seatReservation);
//        seatReservationHistory.setOldStatus(ReservationStatus.AVAILABLE); // or however you want to handle old status
//        seatReservationHistory.setNewStatus(ReservationStatus.PENDING);
//        seatReservationHistory.setChangedByUserId(userId);
//        seatReservationHistory.setChangedTimestamp(now);
//
//        return seatReservation;
//    }
}
