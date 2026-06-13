from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from typing import Optional
import httpx
import asyncio
import random
import logging
from datetime import datetime
from enum import Enum

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="XenoPilot Simulator", version="1.0.0")

class CommunicationChannel(str, Enum):
    EMAIL = "EMAIL"
    SMS = "SMS"
    WHATSAPP = "WHATSAPP"
    RCS = "RCS"

class CommunicationEventType(str, Enum):
    CREATED = "CREATED"
    DISPATCHED = "DISPATCHED"
    DELIVERED = "DELIVERED"
    OPENED = "OPENED"
    CLICKED = "CLICKED"
    REPLIED = "REPLIED"
    FAILED = "FAILED"

class SendRequest(BaseModel):
    customerId: str
    campaignId: str
    communicationId: str
    channel: CommunicationChannel
    message: str

class ReceiptRequest(BaseModel):
    communicationId: str
    eventType: CommunicationEventType

async def send_receipt(backend_url: str, request: ReceiptRequest):
    """Send receipt callback to backend"""
    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(
                f"{backend_url}/api/receipts",
                json={
                    "communicationId": request.communicationId,
                    "eventType": request.eventType
                },
                timeout=5.0
            )
            logger.info(f"Receipt sent: {request.communicationId} -> {request.eventType}")
    except Exception as e:
        logger.error(f"Failed to send receipt: {e}")

def get_delivery_probabilities(channel: CommunicationChannel) -> dict:
    """Get realistic probabilities for each channel"""
    probabilities = {
        CommunicationChannel.EMAIL: {
            "delivered": 0.92,
            "read": 0.35,
            "clicked": 0.15,
            "converted": 0.05
        },
        CommunicationChannel.SMS: {
            "delivered": 0.98,
            "read": 0.95,
            "clicked": 0.20,
            "converted": 0.08
        },
        CommunicationChannel.WHATSAPP: {
            "delivered": 0.99,
            "read": 0.90,
            "clicked": 0.35,
            "converted": 0.12
        },
        CommunicationChannel.RCS: {
            "delivered": 0.96,
            "read": 0.70,
            "clicked": 0.25,
            "converted": 0.10
        }
    }
    return probabilities.get(channel, probabilities[CommunicationChannel.EMAIL])

def get_timing(channel: CommunicationChannel, stage: str) -> float:
    """Get realistic timing for each event in seconds"""
    timings = {
        CommunicationChannel.EMAIL: {
            "sent": 0.5,
            "delivered": random.uniform(2, 8),
            "read": random.uniform(30, 300),
            "clicked": random.uniform(60, 900),
            "converted": random.uniform(300, 3600)
        },
        CommunicationChannel.SMS: {
            "sent": 0.2,
            "delivered": random.uniform(1, 3),
            "read": random.uniform(5, 30),
            "clicked": random.uniform(15, 120),
            "converted": random.uniform(60, 600)
        },
        CommunicationChannel.WHATSAPP: {
            "sent": 0.3,
            "delivered": random.uniform(1, 5),
            "read": random.uniform(10, 60),
            "clicked": random.uniform(30, 300),
            "converted": random.uniform(120, 1200)
        },
        CommunicationChannel.RCS: {
            "sent": 0.3,
            "delivered": random.uniform(2, 6),
            "read": random.uniform(20, 120),
            "clicked": random.uniform(45, 450),
            "converted": random.uniform(300, 1800)
        }
    }
    channel_timings = timings.get(channel, timings[CommunicationChannel.EMAIL])
    return channel_timings.get(stage, 1)

async def simulate_delivery(backend_url: str, request: SendRequest):
    """Simulate message delivery with realistic probabilities"""
    comm_id = request.communicationId
    channel = request.channel
    probs = get_delivery_probabilities(channel)
    
    try:
        await asyncio.sleep(get_timing(channel, "sent"))
        await send_receipt(backend_url, ReceiptRequest(
            communicationId=comm_id,
            eventType=CommunicationEventType.DISPATCHED
        ))
        
        if random.random() < 0.15:
            await send_receipt(backend_url, ReceiptRequest(
                communicationId=comm_id,
                eventType=CommunicationEventType.FAILED
            ))
            return
        
        await asyncio.sleep(get_timing(channel, "delivered"))
        await send_receipt(backend_url, ReceiptRequest(
            communicationId=comm_id,
            eventType=CommunicationEventType.DELIVERED
        ))
        
        if random.random() < probs["read"]:
            await asyncio.sleep(get_timing(channel, "read"))
            await send_receipt(backend_url, ReceiptRequest(
                communicationId=comm_id,
                eventType=CommunicationEventType.OPENED
            ))
            
            if random.random() < probs["clicked"]:
                await asyncio.sleep(get_timing(channel, "clicked"))
                await send_receipt(backend_url, ReceiptRequest(
                    communicationId=comm_id,
                    eventType=CommunicationEventType.CLICKED
                ))
                
                if random.random() < probs["converted"]:
                    await asyncio.sleep(get_timing(channel, "converted"))
                    await send_receipt(backend_url, ReceiptRequest(
                        communicationId=comm_id,
                        eventType=CommunicationEventType.REPLIED
                    ))
    
    except Exception as e:
        logger.error(f"Error simulating delivery for {comm_id}: {e}")

@app.post("/send")
async def send_message(request: SendRequest, backend_url: str = "http://localhost:8080"):
    """
    Simulate sending a message
    
    This endpoint queues a message for async simulation.
    The simulator will make callbacks to POST /api/receipts on the backend.
    """
    try:
        asyncio.create_task(simulate_delivery(backend_url, request))
        return {
            "status": "queued",
            "communicationId": request.communicationId,
            "channel": request.channel,
            "timestamp": datetime.utcnow().isoformat()
        }
    except Exception as e:
        logger.error(f"Error queuing message: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "ok", "service": "xenopilot-simulator"}

@app.get("/")
async def root():
    """API documentation"""
    return {
        "service": "XenoPilot Simulator",
        "version": "1.0.0",
        "endpoints": {
            "POST /send": "Simulate sending a message",
            "GET /health": "Health check"
        }
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8081)
