package com.skuli.student.internal.mapper;

import com.skuli.student.api.dto.StudentDto;
import com.skuli.student.internal.domain.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps {@link Student} to/from {@link StudentDto}.
 */
@Mapper(componentModel = "spring")
public interface StudentMapper {

    StudentDto toDto(Student entity);

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Student toEntity(StudentDto dto);
}
