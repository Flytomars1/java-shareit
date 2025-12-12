package ru.practicum.shareit.itemrequest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.itemrequest.model.ItemRequest;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findByRequesterIdOrderByIdDesc(Long requesterId);

    List<ItemRequest> findByRequesterIdNotOrderByIdDesc(Long requesterId);
}