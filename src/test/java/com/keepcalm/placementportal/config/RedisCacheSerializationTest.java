package com.keepcalm.placementportal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepcalm.placementportal.api.PagedResponse;
import com.keepcalm.placementportal.models.company.CompanyDtos.CompanyDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RedisCacheSerializationTest {
    @Test
    void preservesDtoTypesInsidePagedResponses() {
        var serializer = new RedisConfig().cacheSerializer(new ObjectMapper().findAndRegisterModules());
        PagedResponse<CompanyDto> source = new PagedResponse<>(
                List.of(new CompanyDto(4L, "Example", "Software", null, null, null, 0)), 0, 20, 1, 1);

        Object restored = serializer.deserialize(serializer.serialize(source));

        PagedResponse<?> page = assertInstanceOf(PagedResponse.class, restored);
        CompanyDto company = assertInstanceOf(CompanyDto.class, page.content().get(0));
        assertEquals("Example", company.name());
    }
}
