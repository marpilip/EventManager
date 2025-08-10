package event.eventmanagertask.service;

import event.eventmanagertask.dto.LocationDto;
import event.eventmanagertask.entity.Location;
import event.eventmanagertask.mapper.LocationMapper;
import event.eventmanagertask.repository.LocationRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public LocationService(LocationRepository locationRepository, LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
    }

    public LocationDto createLocation(LocationDto locationDto) {
        Location location = locationMapper.convertFromDto(locationDto);
        Location saved = locationRepository.save(location);

        return locationMapper.convertToDto(saved);
    }

    public LocationDto getLocationById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Локация не найдена по id: " + id));

        return locationMapper.convertToDto(location);
    }

    public List<LocationDto> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(locationMapper::convertToDto)
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

        return locationMapper.convertToDto(updated);
    }

    public void deleteLocation(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Локация не найдена по id: " + id));

        //TODO if location already has event

        locationRepository.delete(location);
    }
}
