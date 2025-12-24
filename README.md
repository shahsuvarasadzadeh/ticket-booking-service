# High-Load Ticket Booking Service

A high-performance REST API designed to handle high-concurrency ticket sales with responsive user experience and data consistency.
Built with Java 21, Spring Boot 3, PostgreSQL, Redis, and Apache Kafka.

## Overview

This project simulates a ticket sales platform for a popular concert where:
1.  **High Traffic:** Users frequently check ticket availability.
2.  **Concurrency:** Multiple users attempt to buy the last tickets simultaneously.
3.  **Responsiveness:** The booking process is instant, while notifications (simulated as slow) are handled asynchronously.

## Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.5.9
* **Database:** PostgreSQL (Relational Data & ACID transactions)
* **Caching:** Redis (High-speed read operations)
* **Messaging:** Apache Kafka (Asynchronous event processing)
* **Containerization:** Docker & Docker Compose

---

## Architecture Decisions (Why I chose these tools?)

### 1. Handling High Read Traffic (Redis)
* **Problem:** The requirement states that the "View Availability" endpoint will be called extremely frequently. Querying the PostgreSQL database for every request would create a bottleneck and increase latency.
* **Solution:** I implemented **Redis** as a caching layer.
* **Strategy:** The service first checks Redis. If the data is missing, it queries the DB and populates the cache. The cache is automatically invalidated/updated whenever a ticket is sold to ensure users see near real-time data.

### 2. Solving Race Conditions (PostgreSQL Atomic Updates)
* **Problem:** With thousands of concurrent requests, a standard "Read-Modify-Write" approach in Java (e.g., `if (tickets > 0) save(tickets - 1)`) causes **double-booking** issues.
* **Solution:** I used **Database-Level Atomic Updates**.
* **Implementation:**
    ```sql
    UPDATE event SET available_tickets = available_tickets - 1 
    WHERE id = :id AND available_tickets > 0
    ```
    This utilizes PostgreSQL's row-level locking. The database ensures that concurrent updates are serialized, and the condition `> 0` prevents selling more tickets than available.

### 3. Asynchronous Notifications (Apache Kafka)
* **Problem:** Sending an email confirmation is simulated to take 2 seconds. Blocking the user's HTTP request for this duration violates the responsiveness requirement.
* **Solution:** I implemented an **Event-Driven Architecture** using **Apache Kafka**.
* **Flow:**
    1. User books a ticket -> Transaction commits -> Event sent to Kafka topic `notification-topic`.
    2. HTTP Response returns immediately to the user.
    3. `NotificationService` (Consumer) reads the message and processes the "slow" email logic in the background.

### 4. Data Consistency (Transaction Synchronization)
* **Problem:** What if the Kafka message is sent, but the database transaction rolls back? The user would receive an email for a ticket they didn't actually buy.
* **Solution:** I used `TransactionSynchronizationManager`.
* **Logic:** The Kafka event is only published **after** the database transaction is successfully committed (`afterCommit`). This guarantees that notifications are only sent for valid, persisted bookings.

---

## Setup & Running

### Prerequisites
* Docker & Docker Compose
* Java 21 (Optional, if running via IDE)

### 1. Clone the Repository
```bash
git clone <repository-url>
cd ticket-booking-service
