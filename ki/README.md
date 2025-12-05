# KI-ARS AI Service

This directory contains the AI microservice used by the KI-ARS system for automatic text classification.  
It is implemented in Python using FastAPI and the pre-trained **XLM-RoBERTa** model for zero-shot learning.

The service analyzes each feedback text and classifies it by:
- Sentiment (positive, neutral, negative)
- Topic (e.g., Organisation, Content, Materials)
- Urgency (low, medium, high) --> only for questions, indicating how urgent the request is
- Type (question or statement)

---

## Quick Start

1. Navigate into the `/ki` folder:

       cd /ki


2. Install dependencies:

       pip install fastapi uvicorn torch transformers pydantic

   or, if a requirements.txt file is provided:

        pip install -r requirements.txt

3. Start the FastAPI service:

       python ki_modell.py

The API will run on http://localhost:8000

### Requirements

* Python 3.8 or newer
* torch
* transformers
* fastapi
* uvicorn
* pydantic

### Usage

Once the service is running, you can test it via HTTP POST requests.
Example request:

     curl -X POST "http://localhost:8000/classify" \
     -H "Content-Type: application/json" \
     -d '{"text":"Der Aufbau der Vorlesung heute war echt top."}'


**Example response**

    {  
      "mode": "single",   
      "sentiment": "positiv",   
      "topic": "Organisation und Ablauf",   
      "question": false   
    }

The backend automatically sends all new feedback entries to this service for classification.

### Notes

On the first run, the model joeddav/xlm-roberta-large-xnli will be downloaded automatically.

The service runs on CPU by default; GPU is optional.

Swagger UI is available at http://localhost:8000/docs
