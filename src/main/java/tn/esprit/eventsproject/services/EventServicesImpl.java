package tn.esprit.eventsproject.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventServicesImpl implements IEventServices {

    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final LogisticsRepository logisticsRepository;

    @Override
    public Participant addParticipant(Participant participant) {
        Objects.requireNonNull(participant, "Participant cannot be null");
        return participantRepository.save(participant);
    }

    @Override
    public Event addAffectEvenParticipant(Event event, int idParticipant) {
        Objects.requireNonNull(event, "Event cannot be null");

        Participant participant = participantRepository.findById(idParticipant)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found with ID: " + idParticipant));

        participant.setEvents(Optional.ofNullable(participant.getEvents()).orElse(new HashSet<>()));
        participant.getEvents().add(event);

        return eventRepository.save(event);
    }

    @Override
    public Event addAffectEvenParticipant(Event event) {
        Objects.requireNonNull(event, "Event cannot be null");

        Set<Participant> participants = Optional.ofNullable(event.getParticipants())
                .orElseThrow(() -> new IllegalArgumentException("Event must have participants"));

        participants.forEach(participant -> {
            Participant existingParticipant = participantRepository.findById(participant.getIdPart())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Participant not found with ID: " + participant.getIdPart()));

            existingParticipant.setEvents(Optional.ofNullable(existingParticipant.getEvents()).orElse(new HashSet<>()));
            existingParticipant.getEvents().add(event);
        });

        return eventRepository.save(event);
    }

    @Override
    public Logistics addAffectLog(Logistics logistics, String descriptionEvent) {
        Objects.requireNonNull(logistics, "Logistics cannot be null");
        Objects.requireNonNull(descriptionEvent, "Event description cannot be null");

        Event event = Optional.ofNullable(eventRepository.findByDescription(descriptionEvent))
                .orElseThrow(() -> new IllegalArgumentException("Event not found with description: " + descriptionEvent));

        Set<Logistics> logisticsSet = Optional.ofNullable(event.getLogistics()).orElse(new HashSet<>());
        logisticsSet.add(logistics);
        event.setLogistics(logisticsSet);
        eventRepository.save(event);

        return logisticsRepository.save(logistics);
    }

    @Override
    public List<Logistics> getLogisticsDates(LocalDate dateDebut, LocalDate dateFin) {
        Objects.requireNonNull(dateDebut, "Start date cannot be null");
        Objects.requireNonNull(dateFin, "End date cannot be null");

        List<Event> events = eventRepository.findByDateDebutBetween(dateDebut, dateFin);

        List<Logistics> logisticsList = new ArrayList<>();
        events.forEach(event -> {
            Optional.ofNullable(event.getLogistics()).ifPresent(logisticsSet ->
                    logisticsSet.stream()
                            .filter(Logistics::isReserve)
                            .forEach(logisticsList::add)
            );
        });

        return logisticsList;
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    @Override
    public void calculCout() {
        List<Event> events = eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                "Tounsi", "Ahmed", Tache.ORGANISATEUR);

        events.forEach(event -> {
            log.info("Calculating cost for event: {}", event.getDescription());
            float totalCost = Optional.ofNullable(event.getLogistics())
                    .orElse(Collections.emptySet())
                    .stream()
                    .filter(Logistics::isReserve)
                    .map(logistics -> logistics.getPrixUnit() * logistics.getQuantite())
                    .reduce(0f, Float::sum);

            event.setCout(totalCost);
            eventRepository.save(event);
            log.info("Event '{}' total cost: {}", event.getDescription(), totalCost);
        });
    }
}
