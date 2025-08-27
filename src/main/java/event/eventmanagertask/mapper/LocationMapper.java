package event.eventmanagertask.mapper;

import event.eventmanagertask.dto.LocationDto;
import event.eventmanagertask.entity.LocationEntity;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {
    public LocationEntity convertFromDto(LocationDto locationDto) {
        LocationEntity entity = new LocationEntity();
        entity.setName(locationDto.getName());
        entity.setAddress(locationDto.getAddress());
        entity.setCapacity(locationDto.getCapacity());
        entity.setDescription(locationDto.getDescription());

        return entity;
    }

    public LocationDto convertToDto(LocationEntity location) {
        LocationDto dto = new LocationDto();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setAddress(location.getAddress());
        dto.setCapacity(location.getCapacity());
        dto.setDescription(location.getDescription());
        return dto;
    }
}
