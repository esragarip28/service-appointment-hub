    package com.auth.app.entity;

    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import org.hibernate.annotations.GenericGenerator;

    import java.time.LocalDateTime;
    import java.util.UUID;

    @Entity
    @Table(name = "customer")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Customer {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", updatable = false, nullable = false)
        private Long id;

        @Column(name = "customer_name", nullable = false)
        private String customerName;

        @Column(name = "email", unique = true)
        private String email;

        @Column(name = "phone_number")
        private String phoneNumber;

        @Column(name = "address")
        private String address;

        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

        @PrePersist
        protected void onCreate() {
            createdAt = LocalDateTime.now();
            updatedAt = LocalDateTime.now();
        }

        @PreUpdate
        protected void onUpdate() {
            updatedAt = LocalDateTime.now();
        }
    }
