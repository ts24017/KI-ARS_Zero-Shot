# KI-ARS AI Service

This directory contains the AI microservice used by the KI-ARS system for automatic text classification. 

It is implemented in Python and uses the pre-trained **XLM-RoBERTa** model for zero-shot learning.

The service analyzes each feedback text and classifies it by:
- **Sentiment** (positive, neutral, negative)
- **Topic** (e.g., Organisation, Content, Materials)
- **Urgency** (low, medium, high) --> only for questions, indicating how urgent the request is
- **Type** (question or statement)

---

## Quick Start
The commands below must be executed in a terminal (macOS/Linux) or PowerShell (Windows).

1. Navigate to the project root

       cd /KI-ARS_Zero-Shot

2. Install dependencies:

       pip install fastapi uvicorn torch transformers pydantic

   or, if a requirements.txt file is provided:

        pip install -r requirements.txt

3. Start the FastAPI service with:

       python3 -m uvicorn ki.ki_modell:app --host 0.0.0.0 --port 8000 --reload

   On Windows, the Python command may be python or py instead of python3.
   If python3 does not work, use:

       python -m uvicorn ki.ki_modell:app --host 0.0.0.0 --port 8000 --reload
    OR

       py -m uvicorn ki.ki_modell:app --host 0.0.0.0 --port 8000 --reload

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
