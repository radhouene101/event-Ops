package tn.esprit.eventsproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.eventsproject.controllers.EventRestController;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.services.EventServicesImpl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventRestController.class)
 class EventRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventServicesImpl eventServices;

    @Autowired
    private ObjectMapper objectMapper;


   @Test
    void testAddParticipant() throws Exception {
      // Arrange
      Participant mockParticipant = new Participant();
      mockParticipant.setIdPart(1); // Set expected ID
      mockParticipant.setNom("John");

      // Mock behavior for service
      when(eventServices.addParticipant(any(Participant.class))).thenReturn(mockParticipant);

      // Act & Assert
      mockMvc.perform(post("/event/addPart")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(new Participant())))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.idPart").value(1)) // Assert ID matches
              .andExpect(jsonPath("$.nom").value("John")); // Assert name matches
   }

   @Test
   void testAddEventPart() throws Exception {

      Event event = new Event();
      event.setIdEvent(1);
      event.setDescription("Sample Event");

      when(eventServices.addAffectEvenParticipant(any(Event.class), eq(1))).thenReturn(event);

      Event inputEvent = new Event();

      mockMvc.perform(post("/event/addEvent/1")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(inputEvent)))
              .andDo(print())
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.idEvent").value(1))
              .andExpect(jsonPath("$.description").value("Sample Event"));
   }


   @Test
    void testAddEvent() throws Exception {
      // Arrange
      Event mockEvent = new Event();
      mockEvent.setIdEvent(1); // Set expected ID
      mockEvent.setDescription("Test Event");


      when(eventServices.addAffectEvenParticipant(any(Event.class))).thenReturn(mockEvent);

      // Act & Assert
      mockMvc.perform(post("/event/addEvent")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(new Event())))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.idEvent").value(1)) // Assert ID matches
              .andExpect(jsonPath("$.description").value("Test Event")); // Assert description matches
   }


   @Test
     void testAddAffectLog() throws Exception {
        // Arrange
        Logistics logistics = new Logistics(1, "Logistics Description", true, 100, 2);
        when(eventServices.addAffectLog(any(Logistics.class), eq("Event Description"))).thenReturn(logistics);

        // Act & Assert
        mockMvc.perform(put("/event/addAffectLog/Event Description")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logistics)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idLog").value(1))
                .andExpect(jsonPath("$.description").value("Logistics Description"))
                .andExpect(jsonPath("$.reserve").value(true));
    }

    @Test
     void testGetLogistiquesDates() throws Exception {
        // Arrange
        Logistics logistics1 = new Logistics(1, "Logistics 1", true, 50, 1);
        Logistics logistics2 = new Logistics(2, "Logistics 2", false, 100, 3);

        List<Logistics> logisticsList = Arrays.asList(logistics1, logistics2);

        when(eventServices.getLogisticsDates(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31)))
                .thenReturn(logisticsList);

        // Act & Assert
        mockMvc.perform(get("/event/getLogs/2024-01-01/2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idLog").value(1))
                .andExpect(jsonPath("$[0].description").value("Logistics 1"))
                .andExpect(jsonPath("$[0].reserve").value(true))
                .andExpect(jsonPath("$[1].idLog").value(2))
                .andExpect(jsonPath("$[1].description").value("Logistics 2"))
                .andExpect(jsonPath("$[1].reserve").value(false));
    }
}
