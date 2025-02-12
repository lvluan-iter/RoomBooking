package com.example.RoomBooking.services;

import com.example.RoomBooking.dto.TourRequestDTO;
import com.example.RoomBooking.models.Image;
import com.example.RoomBooking.models.Property;
import com.example.RoomBooking.models.TourRequest;
import com.example.RoomBooking.repositories.PropertyRepository;
import com.example.RoomBooking.repositories.TourRequestRepository;
import com.example.RoomBooking.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TourRequestService {

    private final TourRequestRepository tourRequestRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    @Autowired
    public TourRequestService(TourRequestRepository tourRequestRepository, EmailService emailService, UserRepository userRepository, PropertyRepository propertyRepository) {
        this.tourRequestRepository = tourRequestRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
    }

    public TourRequestDTO createTourRequest(TourRequestDTO requestDTO) {
        TourRequest tourRequest = mapToEntity(requestDTO);
        tourRequest.setStatus(TourRequest.TourStatus.pending);
        tourRequest.setCreatedAt(LocalDateTime.now());
        TourRequest savedRequest = tourRequestRepository.save(tourRequest);
        TourRequestDTO savedDTO = mapToDTO(savedRequest);

        emailService.sendConfirmationEmail(savedDTO);

        return savedDTO;
    }

    public List<TourRequestDTO> getAllTourRequests() {
        return tourRequestRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Optional<TourRequestDTO> getTourRequestById(Long id) {
        return tourRequestRepository.findById(id).map(this::mapToDTO);
    }

    public List<TourRequestDTO> getTourRequestsByPropertyId(Long propertyId) {
        return tourRequestRepository.findByPropertyId(propertyId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<TourRequestDTO> getTourRequestsByEmail(String email) {
        return tourRequestRepository.findByEmail(email).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<TourRequestDTO> getTourRequestsByUserId(Long userId) {
        List<Long> userPropertyIds = propertyRepository.findPropertyIdsByUserId(userId);

        return userPropertyIds.stream()
                .flatMap(propertyId -> tourRequestRepository.findByPropertyId(propertyId).stream())
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<TourRequestDTO> getTourRequestsByStatus(TourRequest.TourStatus status) {
        return tourRequestRepository.findByStatus(status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Optional<TourRequestDTO> updateTourRequestStatus(Long id, TourRequest.TourStatus newStatus) {
        return tourRequestRepository.findById(id)
                .map(request -> {
                    TourRequest.TourStatus oldStatus = request.getStatus();
                    request.setStatus(newStatus);
                    TourRequest updatedRequest = tourRequestRepository.save(request);
                    TourRequestDTO updatedDTO = mapToDTO(updatedRequest);
                    emailService.sendStatusUpdateEmail(updatedDTO, oldStatus, newStatus);
                    return updatedDTO;
                });
    }

    public Optional<TourRequestDTO> updateTourRequestDate(Long id, TourRequestDTO dto) {
        return tourRequestRepository.findById(id)
                .map(request -> {
                    request.setAppointmentDate(dto.getAppointmentDate());
                    request.setAppointmentTime(dto.getAppointmentTime());
                    request.setStatus(TourRequest.TourStatus.pending);
                    TourRequest updatedRequest = tourRequestRepository.save(request);
                    return mapToDTO(updatedRequest);
                });
    }

    public boolean deleteTourRequest(Long id) {
        return tourRequestRepository.findById(id)
                .map(request -> {
                    tourRequestRepository.delete(request);
                    return true;
                })
                .orElse(false);
    }

    private TourRequestDTO mapToDTO(TourRequest tourRequest) {
        TourRequestDTO dto = new TourRequestDTO();
        dto.setId(tourRequest.getId());
        dto.setPropertyId(tourRequest.getProperty().getId());
        dto.setTitle(tourRequest.getProperty().getTitle());
        dto.setUrl(tourRequest.getProperty().getImages().stream()
                .map(Image::getImageUrl)
                .findFirst()
                .orElse(null));
        dto.setPhoneNumber(tourRequest.getPhoneNumber());
        dto.setEmail(tourRequest.getEmail());
        dto.setAppointmentDate(tourRequest.getAppointmentDate());
        dto.setAppointmentTime(tourRequest.getAppointmentTime());
        dto.setStatus(tourRequest.getStatus());
        dto.setCreatedAt(tourRequest.getCreatedAt());
        return dto;
    }

    private TourRequest mapToEntity(TourRequestDTO dto) {
        TourRequest tourRequest = new TourRequest();
        Property property = propertyRepository.findById(dto.getPropertyId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid property ID: " + dto.getPropertyId()));
        tourRequest.setProperty(property);
        tourRequest.setPhoneNumber(dto.getPhoneNumber());
        tourRequest.setEmail(dto.getEmail());
        tourRequest.setAppointmentDate(dto.getAppointmentDate());
        tourRequest.setAppointmentTime(dto.getAppointmentTime());
        return tourRequest;
    }

    @Scheduled(cron = "0 0 20 * * *")
    public void sendReminderEmails() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<TourRequest> tomorrowTours = tourRequestRepository.findByAppointmentDateAndConfirm(tomorrow);

        for (TourRequest tour : tomorrowTours) {
            TourRequestDTO tourDTO = mapToDTO(tour);

            emailService.sendReminderEmail(tourDTO, tourDTO.getEmail());

            userRepository.findById(tour.getProperty().getUser().getId())
                    .ifPresent(propertyOwner -> {
                emailService.sendReminderEmail(tourDTO, propertyOwner.getEmail());
            });
        }
    }
}