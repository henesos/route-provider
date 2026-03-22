package com.aviation.routeprovider.infrastructure.config;

import com.aviation.routeprovider.domain.model.valueobject.TransportationType;
import com.aviation.routeprovider.domain.model.valueobject.UserRole;
import com.aviation.routeprovider.infrastructure.persistence.entity.LocationJpaEntity;
import com.aviation.routeprovider.infrastructure.persistence.entity.TransportationJpaEntity;
import com.aviation.routeprovider.infrastructure.persistence.entity.UserJpaEntity;
import com.aviation.routeprovider.infrastructure.persistence.repository.LocationJpaRepository;
import com.aviation.routeprovider.infrastructure.persistence.repository.TransportationJpaRepository;
import com.aviation.routeprovider.infrastructure.persistence.repository.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserJpaRepository userRepository;
    private final LocationJpaRepository locationRepository;
    private final TransportationJpaRepository transportationRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-password:}")
    private String adminPassword;

    @Value("${app.seed.agency-password:}")
    private String agencyPassword;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    public DataInitializer(UserJpaRepository userRepository,
                           LocationJpaRepository locationRepository,
                           TransportationJpaRepository transportationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.transportationRepository = transportationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Data seeding is disabled");
            return;
        }
        try {
            initUsers();
            initLocations();
            initTransportations();
        } catch (Exception e) {
            log.error("Data initialization failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void initUsers() {
        if (userRepository.count() == 0) {
            log.info("Initializing default users...");

            // Use environment variables for passwords, or generate random ones
            String adminPwd = getOrCreatePassword(adminPassword, "ADMIN");
            String agencyPwd = getOrCreatePassword(agencyPassword, "AGENCY");

            UserJpaEntity admin = new UserJpaEntity();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode(adminPwd));
            admin.setRole(UserRole.ADMIN);
            admin.setCreatedAt(LocalDateTime.now());

            UserJpaEntity agency = new UserJpaEntity();
            agency.setUsername("agency");
            agency.setPasswordHash(passwordEncoder.encode(agencyPwd));
            agency.setRole(UserRole.AGENCY);
            agency.setCreatedAt(LocalDateTime.now());

            userRepository.saveAll(Arrays.asList(admin, agency));
            log.info("Default users created: admin, agency");
            log.warn("SECURITY NOTICE: Default users created. Please change passwords immediately!");
            log.info("Admin password was set via: {}", 
                adminPassword != null && !adminPassword.isBlank() ? "APP_SEED_ADMIN_PASSWORD env var" : "auto-generated (check logs)");
        }
    }

    private String getOrCreatePassword(String envPassword, String role) {
        if (envPassword != null && !envPassword.isBlank()) {
            return envPassword;
        }
        // Generate a random password for security
        String randomPassword = java.util.UUID.randomUUID().toString().substring(0, 12);
        log.warn("No password set for {} user via environment. Generated random password: {}", role, randomPassword);
        return randomPassword;
    }

    private void initLocations() {
        if (locationRepository.count() == 0) {
            log.info("Initializing sample locations...");

            List<LocationJpaEntity> locations = Arrays.asList(
                createLocation("Istanbul Airport", "Turkey", "Istanbul", "IST"),
                createLocation("Sabiha Gokcen Airport", "Turkey", "Istanbul", "SAW"),
                createLocation("London Heathrow Airport", "United Kingdom", "London", "LHR"),
                createLocation("Taksim Square", "Turkey", "Istanbul", "CCTAK"),
                createLocation("Wembley Stadium", "United Kingdom", "London", "CCWEM")
            );

            locationRepository.saveAll(locations);
            log.info("Sample locations created: IST, SAW, LHR, CCTAK, CCWEM");
        }
    }

    private LocationJpaEntity createLocation(String name, String country, String city, String code) {
        LocationJpaEntity location = new LocationJpaEntity();
        location.setName(name);
        location.setCountry(country);
        location.setCity(city);
        location.setLocationCode(code);
        location.setCreatedAt(LocalDateTime.now());
        location.setUpdatedAt(LocalDateTime.now());
        return location;
    }

    private void initTransportations() {
        if (transportationRepository.count() == 0) {
            log.info("Initializing sample transportations...");

            // Get locations for transportation creation
            LocationJpaEntity ist = locationRepository.findByLocationCode("IST").orElse(null);
            LocationJpaEntity saw = locationRepository.findByLocationCode("SAW").orElse(null);
            LocationJpaEntity lhr = locationRepository.findByLocationCode("LHR").orElse(null);
            LocationJpaEntity cctak = locationRepository.findByLocationCode("CCTAK").orElse(null);
            LocationJpaEntity ccwem = locationRepository.findByLocationCode("CCWEM").orElse(null);

            if (ist == null || saw == null || lhr == null || cctak == null || ccwem == null) {
                log.warn("Cannot initialize transportations: missing locations");
                return;
            }

            List<TransportationJpaEntity> transportations = Arrays.asList(
                // Ground transportation to/from airports in Istanbul
                createTransportation(cctak, ist, TransportationType.UBER, Set.of(1, 2, 3, 4, 5, 6, 7)), // Taksim -> IST (daily)
                createTransportation(cctak, saw, TransportationType.BUS, Set.of(1, 2, 3, 4, 5)), // Taksim -> SAW (weekdays)
                createTransportation(ist, cctak, TransportationType.UBER, Set.of(1, 2, 3, 4, 5, 6, 7)), // IST -> Taksim (daily)
                createTransportation(saw, cctak, TransportationType.BUS, Set.of(1, 2, 3, 4, 5)), // SAW -> Taksim (weekdays)

                // Flights between airports
                createTransportation(ist, lhr, TransportationType.FLIGHT, Set.of(1, 2, 3, 4, 5, 6, 7)), // IST -> LHR (daily)
                createTransportation(lhr, ist, TransportationType.FLIGHT, Set.of(1, 2, 3, 4, 5, 6, 7)), // LHR -> IST (daily)
                createTransportation(saw, lhr, TransportationType.FLIGHT, Set.of(2, 4, 6)), // SAW -> LHR (Tue, Thu, Sat)

                // Ground transportation in London
                createTransportation(lhr, ccwem, TransportationType.SUBWAY, Set.of(1, 2, 3, 4, 5, 6, 7)), // LHR -> Wembley (daily)
                createTransportation(ccwem, lhr, TransportationType.SUBWAY, Set.of(1, 2, 3, 4, 5, 6, 7)), // Wembley -> LHR (daily)
                createTransportation(lhr, ccwem, TransportationType.UBER, Set.of(1, 2, 3, 4, 5, 6, 7)), // LHR -> Wembley Uber (daily)
                createTransportation(ccwem, lhr, TransportationType.UBER, Set.of(1, 2, 3, 4, 5, 6, 7))  // Wembley -> LHR Uber (daily)
            );

            transportationRepository.saveAll(transportations);
            log.info("Sample transportations created: 12 routes including flights and ground transportation");
        }
    }

    private TransportationJpaEntity createTransportation(LocationJpaEntity origin, LocationJpaEntity destination,
                                                         TransportationType type, Set<Integer> operatingDays) {
        TransportationJpaEntity transportation = new TransportationJpaEntity();
        transportation.setOriginLocation(origin);
        transportation.setDestinationLocation(destination);
        transportation.setTransportationType(type);
        transportation.setOperatingDays(operatingDays.stream().mapToInt(Integer::intValue).toArray());
        transportation.setCreatedAt(LocalDateTime.now());
        transportation.setUpdatedAt(LocalDateTime.now());
        return transportation;
    }
}
