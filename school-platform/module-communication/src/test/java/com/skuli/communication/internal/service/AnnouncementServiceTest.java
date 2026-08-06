package com.skuli.communication.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.skuli.common.error.ResourceNotFoundException;
import com.skuli.common.security.TenantContext;
import com.skuli.communication.api.dto.AnnouncementDto;
import com.skuli.communication.internal.domain.Announcement;
import com.skuli.communication.internal.mapper.AnnouncementMapperImpl;
import com.skuli.communication.internal.repository.AnnouncementRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    private static final String TENANT = "default";

    @Mock
    private AnnouncementRepository repository;

    private AnnouncementService service;

    @BeforeEach
    void setUp() {
        service = new AnnouncementService(repository, new AnnouncementMapperImpl());
        TenantContext.set(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private static AnnouncementDto dto() {
        return new AnnouncementDto(999, "Closure", "School closed Friday",
                Instant.parse("2026-05-01T09:00:00Z"), 3);
    }

    @Test
    void create_persists_andIgnoresClientId() {
        AtomicReference<Integer> idAtSave = new AtomicReference<>();
        when(repository.save(any(Announcement.class))).thenAnswer(inv -> {
            Announcement a = inv.getArgument(0);
            idAtSave.set(a.getId());
            a.setId(9);
            return a;
        });

        AnnouncementDto result = service.create(dto());

        assertThat(idAtSave.get()).isNull();
        assertThat(result.id()).isEqualTo(9);
        assertThat(result.title()).isEqualTo("Closure");
        assertThat(result.classId()).isEqualTo(3);
    }

    @Test
    void get_missing_throwsNotFound() {
        when(repository.findByIdAndTenantId(7, TENANT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(7)).isInstanceOf(ResourceNotFoundException.class);
    }
}
