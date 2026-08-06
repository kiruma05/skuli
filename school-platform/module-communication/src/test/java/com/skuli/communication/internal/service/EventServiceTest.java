package com.skuli.communication.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.skuli.common.error.ResourceNotFoundException;
import com.skuli.common.security.TenantContext;
import com.skuli.communication.api.dto.EventDto;
import com.skuli.communication.internal.domain.Event;
import com.skuli.communication.internal.mapper.EventMapperImpl;
import com.skuli.communication.internal.repository.EventRepository;
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
class EventServiceTest {

    private static final String TENANT = "default";

    @Mock
    private EventRepository repository;

    private EventService service;

    @BeforeEach
    void setUp() {
        service = new EventService(repository, new EventMapperImpl());
        TenantContext.set(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private static EventDto dto() {
        return new EventDto(999, "Sports Day", "Annual sports day",
                Instant.parse("2026-05-01T09:00:00Z"), Instant.parse("2026-05-01T15:00:00Z"), null);
    }

    @Test
    void create_persists_andIgnoresClientId() {
        AtomicReference<Integer> idAtSave = new AtomicReference<>();
        when(repository.save(any(Event.class))).thenAnswer(inv -> {
            Event e = inv.getArgument(0);
            idAtSave.set(e.getId());
            e.setId(2);
            return e;
        });

        EventDto result = service.create(dto());

        assertThat(idAtSave.get()).isNull();
        assertThat(result.id()).isEqualTo(2);
        assertThat(result.title()).isEqualTo("Sports Day");
    }

    @Test
    void get_missing_throwsNotFound() {
        when(repository.findByIdAndTenantId(7, TENANT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(7)).isInstanceOf(ResourceNotFoundException.class);
    }
}
