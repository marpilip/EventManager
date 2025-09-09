package event.eventmanagertask.event.fields;

import event.eventmanagertask.entity.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class EventChangeMessage {
    private Long eventId;
    private List<Long> users;
    private Long ownerId;
    private Long changedById;

    private EventFieldChange<String> name;
    private EventFieldChange<Integer> maxPlaces;
    private EventFieldChange<LocalDateTime> date;
    private EventFieldChange<BigDecimal> cost;
    private EventFieldChange<Integer> duration;
    private EventFieldChange<Integer> locationId;
    private EventFieldChange<EventStatus> status;

    public EventChangeMessage() {
    }

    public EventChangeMessage(Long eventId, List<Long> users, Long ownerId,
                              Long changedById) {
        this.eventId = eventId;
        this.users = users;
        this.ownerId = ownerId;
        this.changedById = changedById;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public List<Long> getUsers() {
        return users;
    }

    public void setUsers(List<Long> users) {
        this.users = users;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getChangedById() {
        return changedById;
    }

    public void setChangedById(Long changedById) {
        this.changedById = changedById;
    }

    public EventFieldChange<String> getName() {
        return name;
    }

    public void setName(EventFieldChange<String> name) {
        this.name = name;
    }

    public EventFieldChange<Integer> getMaxPlaces() {
        return maxPlaces;
    }

    public void setMaxPlaces(EventFieldChange<Integer> maxPlaces) {
        this.maxPlaces = maxPlaces;
    }

    public EventFieldChange<LocalDateTime> getDate() {
        return date;
    }

    public void setDate(EventFieldChange<LocalDateTime> date) {
        this.date = date;
    }

    public EventFieldChange<BigDecimal> getCost() {
        return cost;
    }

    public void setCost(EventFieldChange<BigDecimal> cost) {
        this.cost = cost;
    }

    public EventFieldChange<Integer> getDuration() {
        return duration;
    }

    public void setDuration(EventFieldChange<Integer> duration) {
        this.duration = duration;
    }

    public EventFieldChange<Integer> getLocationId() {
        return locationId;
    }

    public void setLocationId(EventFieldChange<Integer> locationId) {
        this.locationId = locationId;
    }

    public EventFieldChange<EventStatus> getStatus() {
        return status;
    }

    public void setStatus(EventFieldChange<EventStatus> status) {
        this.status = status;
    }
}