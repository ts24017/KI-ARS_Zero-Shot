KI-ARS: Zero-Shot Feedback System

This repository contains a full-stack application for automated analysis of student feedback using Natural Language Processing (NLP).  
It consists of a RESTful API built with Spring Boot (backend/), a Vue.js v3 single-page frontend (frontend/), and an AI microservice (ki/) based on XLM-RoBERTa for zero-shot classification.

The system automatically detects sentiment, topic, and urgency of textual feedback and displays aggregated results in interactive dashboards.

---

Quick Start

1. Clone this repository.

2. Follow the  instructions in the `/ki` folder to install and run the AI service.

3. Follow the  instructions in the `/backend` folder to install and start the Spring Boot backend.

4. Follow the instructions in the `/frontend` folder to install and start the Vue.js frontend.

5. Start the AI service by navigating into its folder and executing:
   bash
   python ki_modell.py
Start the backend by navigating into its folder and executing:
./mvnw spring-boot:run
Now start the frontend by navigating into its folder and executing:
npm install
npm run dev

---

Requirements

- Java 21 (JDK)
- Python 3.8 or newer
- Node.js 18 or newer
- Maven (included via `mvnw`)
  
---

Usage
- Assuming all prerequisites are fulfilled, you can follow these instructions to get to know the application:

Once all components are running:

- The frontend is available at http://localhost:5173  
- The backend API is available at http://localhost:8080
- The AI service runs at http://localhost:8000

