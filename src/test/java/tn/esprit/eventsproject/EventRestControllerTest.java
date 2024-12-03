package tn.esprit.eventsproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.eventsproject.controllers.EventRestController;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.services.IEventServices;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventRestController.class)
 class EventRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IEventServices eventServices;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
     void testAddParticipant_Success() throws Exception {
        // Arrange
        Participant inputParticipant = new Participant();
        inputParticipant.setNom("John");
        inputParticipant.setPrenom("Doe");

        Participant savedParticipant = new Participant();
        savedParticipant.setIdPart(1);
        savedParticipant.setNom("John");
        savedParticipant.setPrenom("Doe");

        Mockito.when(eventServices.addParticipant(any(Participant.class))).thenReturn(savedParticipant);

        // Act & Assert
        mockMvc.perform(post("/event/addPart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputParticipant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPart").value(1))
                .andExpect(jsonPath("$.nom").value("John"))
                .andExpect(jsonPath("$.prenom").value("Doe"));

        // Verify that the service was called with the correct participant
        ArgumentCaptor<Participant> participantCaptor = ArgumentCaptor.forClass(Participant.class);
        verify(eventServices).addParticipant(participantCaptor.capture());
        Participant capturedParticipant = participantCaptor.getValue();
        assert capturedParticipant.getNom().equals("John");
        assert capturedParticipant.getPrenom().equals("Doe");
    }

    @Test
     void testAddParticipant_InvalidInput() throws Exception {
        // Arrange: Empty participant (invalid input)
        Participant inputParticipant = new Participant();

        // Act & Assert
        mockMvc.perform(post("/event/addPart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputParticipant)))
                .andExpect(status().isOk()); // Since no validation, it will return 200 OK
    }

    @Test
     void testAddEvent_Success() throws Exception {
        // Arrange
        Event inputEvent = new Event();
        inputEvent.setDescription("Sample Event");

        Event savedEvent = new Event();
        savedEvent.setIdEvent(1);
        savedEvent.setDescription("Sample Event");

        Mockito.when(eventServices.addAffectEvenParticipant(any(Event.class))).thenReturn(savedEvent);

        // Act & Assert
        mockMvc.perform(post("/event/addEvent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEvent").value(1))
                .andExpect(jsonPath("$.description").value("Sample Event"));

        // Verify service call
        verify(eventServices).addAffectEvenParticipant(any(Event.class));
    }

    @Test
     void testAddEventPart_Success() throws Exception {
        // Arrange
        int participantId = 1;
        Event inputEvent = new Event();
        inputEvent.setDescription("Sample Event");

        Event savedEvent = new Event();
        savedEvent.setIdEvent(1);
        savedEvent.setDescription("Sample Event");

        Mockito.when(eventServices.addAffectEvenParticipant(any(Event.class), eq(participantId))).thenReturn(savedEvent);

        // Act & Assert
        mockMvc.perform(post("/event/addEvent/{id}", participantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEvent").value(1))
                .andExpect(jsonPath("$.description").value("Sample Event"));

        // Verify service call
        verify(eventServices).addAffectEvenParticipant(any(Event.class), eq(participantId));
    }


    @Test
     void testAddAffectLog_Success() throws Exception {
        // Arrange
        String descriptionEvent = "Event Description";
        Logistics inputLogistics = new Logistics();
        inputLogistics.setDescription("Logistics Description");
        inputLogistics.setReserve(true);

        Logistics savedLogistics = new Logistics();
        savedLogistics.setIdLog(1);
        savedLogistics.setDescription("Logistics Description");
        savedLogistics.setReserve(true);

        Mockito.when(eventServices.addAffectLog(any(Logistics.class), eq(descriptionEvent))).thenReturn(savedLogistics);

        // Act & Assert
        mockMvc.perform(put("/event/addAffectLog/{description}", descriptionEvent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputLogistics)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idLog").value(1))
                .andExpect(jsonPath("$.description").value("Logistics Description"))
                .andExpect(jsonPath("$.reserve").value(true));

        // Verify service call
        verify(eventServices).addAffectLog(any(Logistics.class), eq(descriptionEvent));
    }



    @Test
     void testGetLogistiquesDates_Success() throws Exception {
        // Arrange
        String dateDebut = "2024-01-01";
        String dateFin = "2024-01-31";

        Logistics logistics1 = new Logistics();
        logistics1.setIdLog(1);
        logistics1.setDescription("Logistics 1");
        logistics1.setReserve(true);

        Logistics logistics2 = new Logistics();
        logistics2.setIdLog(2);
        logistics2.setDescription("Logistics 2");
        logistics2.setReserve(false);

        List<Logistics> logisticsList = Arrays.asList(logistics1, logistics2);

        Mockito.when(eventServices.getLogisticsDates(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(logisticsList);

        // Act & Assert
        mockMvc.perform(get("/event/getLogs/{d1}/{d2}", dateDebut, dateFin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].idLog").value(1))
                .andExpect(jsonPath("$[0].description").value("Logistics 1"))
                .andExpect(jsonPath("$[0].reserve").value(true))
                .andExpect(jsonPath("$[1].idLog").value(2))
                .andExpect(jsonPath("$[1].description").value("Logistics 2"))
                .andExpect(jsonPath("$[1].reserve").value(false));

        // Verify service call with correct dates
        verify(eventServices).getLogisticsDates(LocalDate.parse(dateDebut), LocalDate.parse(dateFin));
    }

    @Test
     void testGetLogistiquesDates_InvalidDate() throws Exception {
        // Arrange
        String invalidDate = "invalid-date";
        String dateFin = "2024-01-31";

        // Act & Assert
        mockMvc.perform(get("/event/getLogs/{d1}/{d2}", invalidDate, dateFin))
                .andExpect(status().isBadRequest());
    }
}
