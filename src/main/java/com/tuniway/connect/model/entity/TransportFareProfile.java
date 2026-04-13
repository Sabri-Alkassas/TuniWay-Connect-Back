package com.tuniway.connect.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "transport_fare_profiles")
public class TransportFareProfile {
    @Id
    @Column(name = "transport_code", nullable = false)
    private String transportCode;

    @Column(name = "pricing_model", nullable = false)
    private String pricingModel;

    @Column(name = "max_sections", nullable = false)
    private Integer maxSections;

    @Column(name = "section_boundary_stop_order")
    private Integer sectionBoundaryStopOrder;

    @Column
    private String notes;

    public String getTransportCode() {
        return transportCode;
    }

    public void setTransportCode(String transportCode) {
        this.transportCode = transportCode;
    }

    public String getPricingModel() {
        return pricingModel;
    }

    public void setPricingModel(String pricingModel) {
        this.pricingModel = pricingModel;
    }

    public Integer getMaxSections() {
        return maxSections;
    }

    public void setMaxSections(Integer maxSections) {
        this.maxSections = maxSections;
    }

    public Integer getSectionBoundaryStopOrder() {
        return sectionBoundaryStopOrder;
    }

    public void setSectionBoundaryStopOrder(Integer sectionBoundaryStopOrder) {
        this.sectionBoundaryStopOrder = sectionBoundaryStopOrder;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
