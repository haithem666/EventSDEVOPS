package tn.esprit.eventsproject.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tn.esprit.eventsproject.entities.*;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;

class EventServicesImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogisticsRepository logisticsRepository;

    @InjectMocks
    private EventServicesImpl eventServices;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddParticipant() {
        Participant participant = new Participant(1, "Ahmed", "Tounsi", Tache.ORGANISATEUR, null);
        when(participantRepository.save(participant)).thenReturn(participant);

        Participant result = eventServices.addParticipant(participant);

        assertNotNull(result);
        assertEquals(1, result.getIdPart());
        assertEquals("Ahmed", result.getNom());
        verify(participantRepository, times(1)).save(participant);
    }

    @Test
    void testAddAffectEvenParticipantById() {
        Participant participant = new Participant(1, "Ahmed", "Tounsi", Tache.ORGANISATEUR, new HashSet<>());
        Event event = new Event(1, "Event Description", null, null, null, new HashSet<>(), 0f);

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(eventRepository.save(event)).thenReturn(event);

        Event result = eventServices.addAffectEvenParticipant(event, 1);

        assertNotNull(result);
        assertTrue(participant.getEvents().contains(event));
        verify(participantRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testAddAffectEvenParticipantWithSet() {
        Participant participant = new Participant(1, "Ahmed", "Tounsi", Tache.ORGANISATEUR, new HashSet<>());
        Event event = new Event(1, "Event Description", null, null, Set.of(participant), new HashSet<>(), 0f);

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(eventRepository.save(event)).thenReturn(event);

        Event result = eventServices.addAffectEvenParticipant(event);

        assertNotNull(result);
        assertTrue(participant.getEvents().contains(event));
        verify(participantRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testAddAffectLog() {
        Logistics logistics = new Logistics(1, "Logistic 1", 10, 100f, true);
        Event event = new Event(1, "Event Description", null, null, null, new HashSet<>(), 0f);

        when(eventRepository.findByDescription("Event Description")).thenReturn(event);
        when(logisticsRepository.save(logistics)).thenReturn(logistics);

        Logistics result = eventServices.addAffectLog(logistics, "Event Description");

        assertNotNull(result);
        assertTrue(event.getLogistics().contains(logistics));
        verify(eventRepository, times(1)).findByDescription("Event Description");
        verify(logisticsRepository, times(1)).save(logistics);
    }

    @Test
    void testGetLogisticsDates() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        Logistics logistics = new Logistics(1, "Logistic 1", 10, 100f, true);
        Event event = new Event(1, "Event Description", startDate, endDate, null, Set.of(logistics), 0f);

        when(eventRepository.findByDateDebutBetween(startDate, endDate)).thenReturn(List.of(event));

        List<Logistics> result = eventServices.getLogisticsDates(startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(logistics));
        verify(eventRepository, times(1)).findByDateDebutBetween(startDate, endDate);
    }

    @Test
    void testCalculCout() {
        Logistics logistics = new Logistics(1, "Logistic 1", 10, 100f, true);
        Event event = new Event(1, "Event Description", null, null, null, Set.of(logistics), 0f);

        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                "Tounsi", "Ahmed", Tache.ORGANISATEUR)).thenReturn(List.of(event));

        eventServices.calculCout();

        assertEquals(1000f, event.getCout());
        verify(eventRepository, times(1)).save(event);
    }
}
