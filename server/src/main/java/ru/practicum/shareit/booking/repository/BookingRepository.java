package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b " +
            "WHERE b.itemId = :itemId " +
            "AND b.status = :status " +
            "AND b.end < :now " +
            "ORDER BY b.end DESC")
    List<Booking> findLastBookingsByItemIdAndStatus(
            Long itemId, Status status, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.itemId = :itemId " +
            "AND b.status = :status " +
            "AND b.start > :now " +
            "ORDER BY b.start ASC")
    List<Booking> findNextBookingsByItemIdAndStatus(
            Long itemId, Status status, LocalDateTime now);

    List<Booking> findByBookerIdOrderByIdDesc(Long bookerId);

    List<Booking> findByBookerIdAndStatus(Long bookerId, Status status);

    List<Booking> findByBookerIdAndItemId(Long bookerId, Long itemId);

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(
            Long bookerId, Long itemId, Status status, LocalDateTime endBefore);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.bookerId = :userId " +
            "AND b.start <= :now AND b.end > :now " +
            "ORDER BY b.end DESC")
    List<Booking> findCurrentBookingsByBooker(Long userId, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.bookerId = :userId " +
            "AND b.end < :now " +
            "ORDER BY b.end DESC")
    List<Booking> findPastBookingsByBooker(Long userId, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.bookerId = :userId " +
            "AND b.start > :now " +
            "ORDER BY b.end DESC")
    List<Booking> findFutureBookingsByBooker(Long userId, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE i.ownerId = :ownerId " +
            "ORDER BY b.end DESC")
    List<Booking> findByItemOwnerIdOrderByIdDesc(Long ownerId);

    @Query("SELECT b FROM Booking b " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE i.ownerId = :ownerId " +
            "AND b.status = :status " +
            "ORDER BY b.end DESC")
    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, Status status);

    @Query("SELECT b FROM Booking b " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE i.ownerId = :userId " +
            "AND b.start <= :now AND b.end > :now " +
            "ORDER BY b.end DESC")
    List<Booking> findCurrentBookingsByOwner(Long userId, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE i.ownerId = :userId " +
            "AND b.end < :now " +
            "ORDER BY b.end DESC")
    List<Booking> findPastBookingsByOwner(Long userId, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "JOIN Item i ON b.itemId = i.id " +
            "WHERE i.ownerId = :userId " +
            "AND b.start > :now " +
            "ORDER BY b.end DESC")
    List<Booking> findFutureBookingsByOwner(Long userId, LocalDateTime now);
}