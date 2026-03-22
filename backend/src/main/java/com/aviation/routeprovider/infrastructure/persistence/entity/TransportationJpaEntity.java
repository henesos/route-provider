package com.aviation.routeprovider.infrastructure.persistence.entity;

import com.aviation.routeprovider.domain.model.valueobject.TransportationType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transportations", indexes = {
    @Index(name = "idx_trans_origin", columnList = "origin_location_id"),
    @Index(name = "idx_trans_destination", columnList = "destination_location_id"),
    @Index(name = "idx_trans_type", columnList = "transportation_type"),
    @Index(name = "idx_trans_composite", 
           columnList = "origin_location_id, transportation_type")
})
@NamedEntityGraph(
    name = "Transportation.withLocations",
    attributeNodes = {
        @NamedAttributeNode("originLocation"),
        @NamedAttributeNode("destinationLocation")
    }
)
public class TransportationJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_location_id", nullable = false)
    private LocationJpaEntity originLocation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_location_id", nullable = false)
    private LocationJpaEntity destinationLocation;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transportation_type", nullable = false, length = 20)
    private TransportationType transportationType;
    
    @Column(name = "operating_days", nullable = false)
    private int[] operatingDays;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public TransportationJpaEntity() {}
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters
    public Long getId() { return id; }
    public LocationJpaEntity getOriginLocation() { return originLocation; }
    public LocationJpaEntity getDestinationLocation() { return destinationLocation; }
    public TransportationType getTransportationType() { return transportationType; }
    public int[] getOperatingDays() { return operatingDays; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setOriginLocation(LocationJpaEntity originLocation) { this.originLocation = originLocation; }
    public void setDestinationLocation(LocationJpaEntity destinationLocation) { this.destinationLocation = destinationLocation; }
    public void setTransportationType(TransportationType transportationType) { this.transportationType = transportationType; }
    public void setOperatingDays(int[] operatingDays) { this.operatingDays = operatingDays; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
