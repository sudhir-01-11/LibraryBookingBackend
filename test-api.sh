#!/bin/bash

echo "1. Registering user John Doe..."
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@example.com","phone":"9876543210","password":"mysecretpassword"}' | jq . || echo "Failed to register"

echo -e "\n\n2. Logging in..."
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"mysecretpassword"}')

echo $RESPONSE | jq .

TOKEN=$(echo $RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "Could not extract token!"
    exit 1
fi

echo -e "\nExtracted Token: $TOKEN\n"

echo "3. Checking Seat Availability for Tomorrow..."
curl -s -X GET "http://localhost:8080/api/bookings/availability?zoneId=1&startTime=2026-10-11T10:00:00Z&endTime=2026-10-11T12:00:00Z" \
  -H "Authorization: Bearer $TOKEN" | jq .

echo -e "\n\n4. Holding a seat (Booking Seat 2)..."
curl -s -X POST http://localhost:8080/api/bookings/hold \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"zoneId":1,"seatId":2,"startTime":"2026-10-11T10:00:00Z","endTime":"2026-10-11T12:00:00Z"}' | jq .
echo -e "\n"
