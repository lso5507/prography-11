package com.prography.backend.repository;

import com.prography.backend.entity.QrCode;
import com.prography.backend.entity.SessionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrCodeRepository extends JpaRepository<QrCode, Long> {
    Optional<QrCode> findByHashValue(String hashValue);

    Optional<QrCode> findFirstBySessionAndActiveTrue(SessionEntity session);
}
