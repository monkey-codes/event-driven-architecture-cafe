#!/bin/bash
WAITER_SERVICE=http://localhost:8080
WAITER_1_ID=fb5cd662-2250-4a3a-bd52-456926e0f179
WAITER_1_NAME="monkey codes"

ORDER_ID_1=$(uuidgen)

#Hire Waiter
#echo "Hiring a waiter ($WAITER_1_NAME)"
#curl -X POST "$WAITER_SERVICE/api/waiters" \
#-H "Content-Type: application/json" \
#--data "{\"id\":\"$WAITER_1_ID\",\"name\":\"$WAITER_1_NAME\"}"
#echo ""
#Take order
echo "$WAITER_1_NAME taking order $ORDER_ID_1"
curl -X POST "$WAITER_SERVICE/api/waiters/$WAITER_1_ID/orders" \
-H "Content-Type: application/json" \
--data "{\"id\":\"$ORDER_ID_1\",\"items\": [{\"name\": \"burger\", \"quantity\": 1}]}"
echo ""
