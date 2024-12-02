package tn.esprit.eventsproject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import tn.esprit.eventsproject.entities.*;
import tn.esprit.eventsproject.repositories.*;
import tn.esprit.eventsproject.services.IEventServices;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

 class EventServicesImplTest {

   @Mock
   private EventRepository eventRepository;

   @Mock
   private ParticipantRepository participantRepository;

   @Mock
   private LogisticsRepository logisticsRepository;

   @InjectMocks
   private IEventServices eventServices;

   @BeforeEach
   public void setUp() {
      MockitoAnnotations.openMocks(this);
   }

   // Test for addParticipant
   @Test
    void testAddParticipant() {
      // Arrange
      Participant participant = new Participant();
      participant.setIdPart(1);
      participant.setNom("John");
      when(participantRepository.save(participant)).thenReturn(participant);

      // Act
      Participant result = eventServices.addParticipant(participant);

      // Assert
      assertNotNull(result);
      assertEquals("John", result.getNom());
      verify(participantRepository, times(1)).save(participant);
   }

   // Test for addAffectEvenParticipant (Event and Participant by ID)
   @Test
    void testAddAffectEvenParticipant_ById() {
      // Arrange
      Participant participant = new Participant();
      participant.setIdPart(1);
      participant.setEvents(new HashSet<>());

      Event event = new Event();
      event.setIdEvent(100);
      event.setDescription("Sample Event");

      when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
      when(eventRepository.save(event)).thenReturn(event);

      // Act
      Event result = eventServices.addAffectEvenParticipant(event, 1);

      // Assert
      assertNotNull(result);
      assertTrue(participant.getEvents().contains(event));
      verify(participantRepository, times(1)).findById(1);
      verify(eventRepository, times(1)).save(event);
   }

   // Test for addAffectEvenParticipant (Event with Participants)
   @Test
    void testAddAffectEvenParticipant_WithParticipants() {
      // Arrange
      Event event = new Event();
      event.setIdEvent(100);
      event.setDescription("Sample Event");

      Participant participant = new Participant();
      participant.setIdPart(1);
      participant.setEvents(new HashSet<>());
      Set<Participant> set = new HashSet<>();
      set.add(participant);
      event.setParticipants(set);

      when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
      when(eventRepository.save(event)).thenReturn(event);

      // Act
      Event result = eventServices.addAffectEvenParticipant(event);

      // Assert
      assertNotNull(result);
      assertTrue(participant.getEvents().contains(event));
      verify(participantRepository, times(1)).findById(1);
      verify(eventRepository, times(1)).save(event);
   }

   // Test for addAffectLog
   @Test
    void testAddAffectLog() {
      // Arrange
      Event event = new Event();
      event.setIdEvent(1);
      event.setDescription("Sample Event");
      event.setLogistics(new HashSet<>());

      Logistics logistics = new Logistics();
      logistics.setIdLog(1);
      logistics.setReserve(true);
      logistics.setPrixUnit(50);
      logistics.setQuantite(2);

      when(eventRepository.findByDescription("Sample Event")).thenReturn(event);
      when(logisticsRepository.save(logistics)).thenReturn(logistics);

      // Act
      Logistics result = eventServices.addAffectLog(logistics, "Sample Event");

      // Assert
      assertNotNull(result);
      assertTrue(event.getLogistics().contains(logistics));
      verify(eventRepository, times(1)).findByDescription("Sample Event");
      verify(logisticsRepository, times(1)).save(logistics);
   }

   // Test for getLogisticsDates
   @Test
    void testGetLogisticsDates() {
      // Arrange
      LocalDate startDate = LocalDate.of(2024, 1, 1);
      LocalDate endDate = LocalDate.of(2024, 1, 31);

      Event event = new Event();
      event.setIdEvent(1);
      event.setDescription("Sample Event");

      Logistics logistics = new Logistics();
      logistics.setIdLog(1);
      logistics.setReserve(true);
      Set<Logistics> set = new HashSet<>();
      set.add(logistics);
      event.setLogistics(set);
      List<Event> events = new ArrayList<>();
      events.add(event);
      when(eventRepository.findByDateDebutBetween(startDate, endDate)).thenReturn(events);

      // Act
      List<Logistics> result = eventServices.getLogisticsDates(startDate, endDate);

      // Assert
      assertNotNull(result);
      assertEquals(1, result.size());
      assertTrue(result.contains(logistics));
      verify(eventRepository, times(1)).findByDateDebutBetween(startDate, endDate);
   }

   // Test for calculCout
   @Test
    void testCalculCout() {
      // Arrange
      Event event = new Event();
      event.setIdEvent(1);
      event.setDescription("Sample Event");
      event.setLogistics(new HashSet<>());

      Logistics logistics = new Logistics();
      logistics.setIdLog(1);
      logistics.setReserve(true);
      logistics.setPrixUnit(100);
      logistics.setQuantite(2);
      event.getLogistics().add(logistics);
      List<Event> eventList = new ArrayList<>();
      eventList.add(event);
      when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
              "Tounsi", "Ahmed", Tache.ORGANISATEUR))
              .thenReturn(eventList);

      // Act
      eventServices.calculCout();

      // Assert
      assertEquals(200, event.getCout());
      verify(eventRepository, times(1)).findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
              "Tounsi", "Ahmed", Tache.ORGANISATEUR);
      verify(eventRepository, times(1)).save(event);
   }
}
