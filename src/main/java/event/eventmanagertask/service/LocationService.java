package event.eventmanagertask.service;

import event.eventmanagertask.dto.LocationDto;
import event.eventmanagertask.entity.Location;
import event.eventmanagertask.repository.LocationRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class LocationService {
    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public LocationDto createLocation(LocationDto locationDto) {
        Location location = convertFromDto(locationDto);
        Location saved = locationRepository.save(location);

        return convertToDto(saved);
    }

    public LocationDto getLocationById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Локация не найдена по id: " + id));

        return convertToDto(location);
    }

    public List<LocationDto> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public LocationDto updateLocation(Long id, LocationDto locationDto) throws BadRequestException {
        Location existedLocation = locationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Локация не найдена по id: " + id));

        if (locationDto.getCapacity() < existedLocation.getCapacity()) {
            throw new BadRequestException("Новая локация должна вмещать не меньше людей, чем предыдущая");
        }

        existedLocation.setName(locationDto.getName());
        existedLocation.setAddress(locationDto.getAddress());
        existedLocation.setCapacity(locationDto.getCapacity());
        existedLocation.setDescription(locationDto.getDescription());

        Location updated = locationRepository.save(existedLocation);

        return convertToDto(updated);
    }

    public void deleteLocation(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Локация не найдена по id: " + id));

        //TODO if location already has event

        locationRepository.delete(location);
    }

    public Location convertFromDto(LocationDto locationDto) {
        Location entity = new Location();
        entity.setName(locationDto.getName());
        entity.setAddress(locationDto.getAddress());
        entity.setCapacity(locationDto.getCapacity());
        entity.setDescription(locationDto.getDescription());

        return entity;
    }

    public LocationDto convertToDto(Location location) {
        LocationDto dto = new LocationDto();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setAddress(location.getAddress());
        dto.setCapacity(location.getCapacity());
        dto.setDescription(location.getDescription());
        return dto;
    }
}
