package com.example.RoomBooking.services;

import com.example.RoomBooking.dto.*;
import com.example.RoomBooking.exceptions.ResourceNotFoundException;
import com.example.RoomBooking.models.*;
import com.example.RoomBooking.repositories.*;
import com.example.RoomBooking.specifications.PropertySpecifications;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisCommandExecutionException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AmenityRepository amenityRepository;

    @Autowired
    private TourRequestRepository tourRequestRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private NearbyPlaceRepository nearbyPlaceRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String TEMP_PROPERTY_PREFIX = "temp_property:";
    private static final int TEMP_PROPERTY_EXPIRY = 30;

    @Autowired
    private ObjectMapper objectMapper;

    public Page<PropertyResponse> getAvailableProperties(Pageable pageable) {
        return propertyRepository.findByIsAvailableTrue(pageable).map(this::mapToResponse);
    }

    public List<PropertyResponse> getAllProperties() {
        return propertyRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PropertyResponse> getTop10PopularProperties() {
        return propertyRepository.findTop10PopularProperties().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "propertySearchCache", key = "#searchDTO")
    public Page<PropertyResponse> searchProperties(PropertySearchDTO searchDTO, Pageable pageable) {
        Specification<Property> spec = PropertySpecifications.createSpecification(searchDTO);
        return propertyRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    public Page<PropertyResponse> searchNearBy(String location, Long propertyId, Pageable pageable) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty");
        }

        Page<Property> nearbyProperties = propertyRepository.searchNearBy(location, propertyId, pageable);

        if (nearbyProperties.isEmpty()) {
            throw new ResourceNotFoundException("No available Nearby Property");
        }

        return nearbyProperties.map(this::mapToResponse);
    }

    public PropertyResponse getPropertyById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
        incrementPropertyView(id);
        property.setVisits(property.getVisits() + 1);
        propertyRepository.save(property);
        return mapToResponse(property);
    }

    @Transactional
    public void addProperty(PropertyRequest propertyRequest) {
        Property property = new Property();
        property.setImages(new ArrayList<>());
        property.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        property.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        property.setVisits(0.0);

        updatePropertyFromRequest(property, propertyRequest);

        Property savedProperty = propertyRepository.save(property);

        // Save nearby places
        if (propertyRequest.getNearbyPlaces() != null) {
            for (NearbyPlaceDTO placeDTO : propertyRequest.getNearbyPlaces()) {
                NearbyPlace nearbyPlace = new NearbyPlace();
                nearbyPlace.setProperty(savedProperty);
                nearbyPlace.setName(placeDTO.getName());
                nearbyPlace.setDistance(placeDTO.getDistance());
                nearbyPlace.setUnit(placeDTO.getUnit());
                nearbyPlaceRepository.save(nearbyPlace);
            }
        }
    }

    @Transactional
    public void updateProperty(Long propertyId, PropertyRequest propertyRequest) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        updatePropertyFromRequest(property, propertyRequest);
        property.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        Property savedProperty = propertyRepository.save(property);

        // Update nearby places
        nearbyPlaceRepository.deleteByPropertyId(propertyId);
        if (propertyRequest.getNearbyPlaces() != null) {
            for (NearbyPlaceDTO placeDTO : propertyRequest.getNearbyPlaces()) {
                NearbyPlace nearbyPlace = new NearbyPlace();
                nearbyPlace.setProperty(savedProperty);
                nearbyPlace.setName(placeDTO.getName());
                nearbyPlace.setDistance(placeDTO.getDistance());
                nearbyPlace.setUnit(placeDTO.getUnit());
                nearbyPlaceRepository.save(nearbyPlace);
            }
        }
    }

    @Transactional
    public void deleteProperty(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));
        nearbyPlaceRepository.deleteByPropertyId(propertyId);
        propertyRepository.delete(property);
    }

    public Map<String, Object> getQuickStatsForUser(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        YearMonth currentMonth = YearMonth.from(now);
        YearMonth previousMonth = currentMonth.minusMonths(1);

        Long currentMonthListings = propertyRepository.countByUserIdAndCreatedAtBetween(userId,
                currentMonth.atDay(1).atStartOfDay(), now);
        Long previousMonthListings = propertyRepository.countByUserIdAndCreatedAtBetween(userId,
                previousMonth.atDay(1).atStartOfDay(), currentMonth.atDay(1).atStartOfDay());

        Long currentMonthViews = getViewsForMonth(userId, currentMonth);
        Long previousMonthViews = getViewsForMonth(userId, previousMonth);

        Long currentMonthLikes = getLikesForMonth(userId, currentMonth);
        Long previousMonthLikes = getLikesForMonth(userId, previousMonth);

        Long currentMonthRequests = getRequestsForMonth(userId, currentMonth);
        Long previousMonthRequests = getRequestsForMonth(userId, previousMonth);

        stats.put("totalProperties", currentMonthListings);
        stats.put("totalViews", currentMonthViews);
        stats.put("totalFavorites", currentMonthLikes);
        stats.put("totalRequests", currentMonthRequests);
        stats.put("propertiesGrowth", calculateGrowth(previousMonthListings, currentMonthListings));
        stats.put("viewsGrowth", calculateGrowth(previousMonthViews, currentMonthViews));
        stats.put("favoritesGrowth", calculateGrowth(previousMonthLikes, currentMonthLikes));
        stats.put("requestsGrowth", calculateGrowth(previousMonthRequests, currentMonthRequests));

        return stats;
    }

    public void incrementPropertyView(Long propertyId) {
        String key = String.format("property:%d:views:%s", propertyId, YearMonth.now());

        try {
            redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, 180, TimeUnit.DAYS);
        } catch (RedisCommandExecutionException e) {
            if (e.getMessage().contains("value is not an integer or out of range")) {
                redisTemplate.opsForValue().set(key, "1");
                redisTemplate.expire(key, 180, TimeUnit.DAYS);
            } else {
                throw e;
            }
        }
    }

    private Long getViewsForMonth(Long userId, YearMonth yearMonth) {
        List<Long> userPropertyIds = propertyRepository.findPropertyIdsByUserId(userId);
        if (userPropertyIds.isEmpty()) {
            return 0L;
        }

        return userPropertyIds.stream()
                .mapToLong(id -> {
                    String key = String.format("property:%d:views:%s", id, yearMonth);
                    String value = redisTemplate.opsForValue().get(key);
                    return value != null ? Long.parseLong(value) : 0L;
                })
                .sum();
    }

    public void incrementPropertyLike(Long propertyId) {
        String key = String.format("property:%d:likes:%s", propertyId, YearMonth.now());

        try {
            redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, 180, TimeUnit.DAYS);
        } catch (RedisCommandExecutionException e) {
            if (e.getMessage().contains("value is not an integer or out of range")) {
                redisTemplate.opsForValue().set(key, "1");
                redisTemplate.expire(key, 180, TimeUnit.DAYS);
            } else {
                throw e;
            }
        }
    }

    public void decrementPropertyLike(Long propertyId) {
        String key = String.format("property:%d:likes:%s", propertyId, YearMonth.now());

        try {
            Long currentLikes = redisTemplate.opsForValue().decrement(key);
            if (currentLikes != null && currentLikes < 0) {
                redisTemplate.opsForValue().set(key, "0");
            }
            redisTemplate.expire(key, 180, TimeUnit.DAYS);
        } catch (RedisCommandExecutionException e) {
            if (e.getMessage().contains("value is not an integer or out of range")) {
                redisTemplate.opsForValue().set(key, "0");
                redisTemplate.expire(key, 180, TimeUnit.DAYS);
            } else {
                throw e;
            }
        }
    }

    private Long getLikesForMonth(Long userId, YearMonth yearMonth) {
        List<Long> userPropertyIds = propertyRepository.findPropertyIdsByUserId(userId);
        if (userPropertyIds.isEmpty()) {
            return 0L;
        }

        return userPropertyIds.stream()
                .mapToLong(id -> {
                    String key = String.format("property:%d:likes:%s", id, yearMonth);
                    String value = redisTemplate.opsForValue().get(key);
                    return value != null ? Long.parseLong(value) : 0L;
                })
                .sum();
    }

    public List<PropertyStats> getPropertyStatsForUser(Long userId, YearMonth yearMonth) {
        List<Long> userPropertyIds = propertyRepository.findPropertyIdsByUserId(userId);

        return userPropertyIds.stream()
                .map(propertyId -> {
                    String viewsKey = String.format("property:%d:views:%s", propertyId, yearMonth);
                    String likesKey = String.format("property:%d:likes:%s", propertyId, yearMonth);

                    Long views = Optional.ofNullable(redisTemplate.opsForValue().get(viewsKey))
                            .map(Long::parseLong)
                            .orElse(0L);

                    Long likes = Optional.ofNullable(redisTemplate.opsForValue().get(likesKey))
                            .map(Long::parseLong)
                            .orElse(0L);

                    Long requests = tourRequestRepository.countTourRequestsByPropertyIdAndYearMonth(propertyId, yearMonth.toString());

                    return new PropertyStats(propertyId, views, likes, requests);
                })
                .collect(Collectors.toList());
    }

    public List<PropertyResponse> getPropertyForUser(Long userId) {
        List<Property> userProperty = propertyRepository.findPropertyByUserId(userId);

        return userProperty.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Long getRequestsForMonth(Long userId, YearMonth yearMonth) {
        List<Long> userPropertyIds = propertyRepository.findPropertyIdsByUserId(userId);
        if (userPropertyIds.isEmpty()) {
            return 0L;
        }

        String yearMonthString = yearMonth.toString();

        return userPropertyIds.stream()
                .mapToLong(propertyId -> tourRequestRepository.countTourRequestsByPropertyIdAndYearMonth(propertyId, yearMonthString))
                .sum();
    }

    private double calculateGrowth(Long previousValue, Long currentValue) {
        if (previousValue == 0) {
            return currentValue > 0 ? 100.0 : 0.0;
        }
        return ((currentValue - previousValue) / (double) previousValue) * 100.0;
    }

    private PropertyResponse mapToResponse(Property property) {
        PropertyResponse response = new PropertyResponse();
        response.setId(property.getId());
        response.setTitle(property.getTitle());
        response.setDescription(property.getDescription());
        response.setAddress(property.getAddress());
        response.setPrice(property.getPrice());
        response.setAvailable(property.isAvailable());
        response.setLocation(property.getLocation().getName());
        response.setBedrooms(property.getBedrooms());
        response.setBathrooms(property.getBathrooms());
        response.setArea(property.getArea());
        response.setFurniture(property.getFurniture());
        response.setType(property.getType());
        response.setKeywords(property.getKeywords());
        response.setCategoryId(property.getCategory().getId());
        response.setCategoryName(property.getCategory().getCategoryName());
        response.setUserId(property.getUser().getId());
        response.setCreatedAt(property.getCreatedAt());
        response.setUpdatedAt(property.getUpdatedAt());
        response.setVisits(property.getVisits());
        response.setImageUrls(property.getImages().stream().map(Image::getImageUrl).collect(Collectors.toList()));

        response.setAmenities(property.getAmenities().stream()
                .map(this::mapToAmenityDTO)
                .collect(Collectors.toList()));

        // Map nearby places
        response.setNearbyPlaces(property.getNearbyPlaces().stream()
                .map(this::mapToNearbyPlaceDTO)
                .collect(Collectors.toList()));

        response.setExpirationDate(property.getExpirationDate());
        response.setApproved(property.isApproved());
        response.setPaid(property.isPaid());

        return response;
    }

    private AmenityDTO mapToAmenityDTO(Amenity amenity) {
        AmenityDTO amenityDTO = new AmenityDTO();
        amenityDTO.setId(amenity.getId());
        amenityDTO.setIcon(amenity.getIcon());
        amenityDTO.setName(amenity.getName());
        return  amenityDTO;
    }

    private NearbyPlaceDTO mapToNearbyPlaceDTO(NearbyPlace nearbyPlace) {
        NearbyPlaceDTO dto = new NearbyPlaceDTO();
        dto.setId(nearbyPlace.getId());
        dto.setName(nearbyPlace.getName());
        dto.setDistance(nearbyPlace.getDistance());
        dto.setUnit(nearbyPlace.getUnit());
        return dto;
    }

    private void updatePropertyFromRequest(Property property, PropertyRequest request) {
        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setAddress(request.getAddress());
        property.setPrice(request.getPrice());
        Location location = locationRepository.findByName(request.getLocation())
                .orElseThrow(() -> new ResourceNotFoundException("Not found this location name" + request.getLocation()));
        property.setLocation(location);
        property.setBedrooms(request.getBedrooms());
        property.setBathrooms(request.getBathrooms());
        property.setArea(request.getArea());
        property.setFurniture(request.getFurniture());
        property.setType(request.getType());
        property.setKeywords(request.getKeywords());
        property.setAvailable(request.isAvailable());

        if (request.getAmenities() != null) {
            List<Amenity> amenities = request.getAmenities().stream()
                    .map(amenityDto -> amenityRepository.findById(amenityDto.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Amenity not found with id: " + amenityDto.getId())))
                    .collect(Collectors.toList());
            property.setAmenities(amenities);
        } else {
            property.setAmenities(new ArrayList<>());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        property.setCategory(category);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        property.setUser(user);

        updateImages(property, request.getImageUrls());

        property.setExpirationDate(request.getExpirationDate());
        property.setApproved(request.isApproved());
        property.setPaid(request.isPaid());
    }

    private void updateImages(Property property, List<String> imageUrls) {
        if (property.getImages() == null) {
            property.setImages(new ArrayList<>());
        }

        if (imageUrls != null && !imageUrls.isEmpty()) {
            property.getImages().clear();
            List<Image> newImages = imageUrls.stream()
                    .map(url -> {
                        Image image = new Image();
                        image.setImageUrl(url);
                        image.setProperty(property);
                        return image;
                    })
                    .toList();
            property.getImages().addAll(newImages);
        }
    }

    public List<NearbyPlaceDTO> getNearbyPlacesForProperty(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));
        return property.getNearbyPlaces().stream()
                .map(this::mapToNearbyPlaceDTO)
                .collect(Collectors.toList());
    }

    public String createTempPropertyAndGetRef(PropertyRequest propertyRequest) {
        try {
            String reference = generateUniqueReference();
            String propertyJson = objectMapper.writeValueAsString(propertyRequest);
            String redisKey = TEMP_PROPERTY_PREFIX + reference;

            redisTemplate.opsForValue().set(redisKey, propertyJson, TEMP_PROPERTY_EXPIRY, TimeUnit.MINUTES);

            return reference;
        } catch (Exception e) {
            throw new RuntimeException("Error creating temporary property", e);
        }
    }

    public PropertyRequest getTempProperty(String reference) {
        try {
            String redisKey = TEMP_PROPERTY_PREFIX + reference;
            String propertyJson = redisTemplate.opsForValue().get(redisKey);

            if (propertyJson == null) {
                throw new ResourceNotFoundException("Temporary property not found or expired");
            }

            return objectMapper.readValue(propertyJson, PropertyRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving temporary property", e);
        }
    }

    public void deleteTempProperty(String reference) {
        String redisKey = TEMP_PROPERTY_PREFIX + reference;
        redisTemplate.delete(redisKey);
    }

    private String generateUniqueReference() {
        return UUID.randomUUID().toString();
    }

    @Transactional
    public void togglePropertyVisibility(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        property.setAvailable(!property.isAvailable());
        property.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        propertyRepository.save(property);
    }

    @Transactional
    public void processExtension(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentExpiration = property.getExpirationDate().toLocalDateTime();

        LocalDateTime newExpirationDate = currentExpiration.isBefore(now)
                ? now.plusDays(30)
                : currentExpiration.plusDays(30);

        property.setExpirationDate(Timestamp.valueOf(newExpirationDate));
        property.setApproved(true);
        property.setPaid(true);
        property.setAvailable(true);
        property.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        propertyRepository.save(property);
    }
}