#!/bin/bash
# Day 3-4 smoke test — run both services before executing this

set -e
ORDER_BASE="http://localhost:8081/api/orders"
INV_BASE="http://localhost:8082/api/inventory"
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

pass() { echo -e "${GREEN}PASS${NC} $1"; }
fail() { echo -e "${RED}FAIL${NC} $1"; }

echo "===== Inventory Service ====="

echo ""
echo "1. List all inventory (should have 3 seeded products)..."
RESULT=$(curl -s $INV_BASE)
echo $RESULT | grep -q "LAPTOP-001" && pass "Seed data present" || fail "Seed data missing"

echo ""
echo "2. Check stock for LAPTOP-001 (qty=10, should be available)..."
RESULT=$(curl -s "$INV_BASE/check/LAPTOP-001?quantity=10")
echo $RESULT | grep -q '"available":true' && pass "Stock check available" || fail "Stock check failed"

echo ""
echo "3. Check stock for TABLET-001 (qty=1, should be unavailable — 0 in stock)..."
RESULT=$(curl -s "$INV_BASE/check/TABLET-001?quantity=1")
echo $RESULT | grep -q '"available":false' && pass "Out-of-stock check correct" || fail "Out-of-stock check failed"

echo ""
echo "4. Reserve 5 units of LAPTOP-001..."
RESULT=$(curl -s -X POST "$INV_BASE/LAPTOP-001/reserve?quantity=5")
echo $RESULT |  grep -q '"reservedQuantity":[1-9]' && pass "Reserve successful" || fail "Reserve failed: $RESULT"

echo ""
echo "5. Try to reserve 999 (should fail with 409)..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$INV_BASE/LAPTOP-001/reserve?quantity=999")
[ "$HTTP_CODE" = "409" ] && pass "Conflict returned correctly" || fail "Expected 409, got $HTTP_CODE"

echo ""
echo "===== Order Service ====="

echo ""
echo "6. Place a new order..."
RESPONSE=$(curl -s -X POST $ORDER_BASE \
  -H "Content-Type: application/json" \
  -d '{"productCode":"LAPTOP-001","quantity":2,"unitPrice":1299.99}')
echo $RESPONSE | grep -q '"status":"PENDING"' && pass "Order placed" || fail "Order failed: $RESPONSE"
ORDER_ID=$(echo $RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
echo "   Order ID: $ORDER_ID"

echo ""
echo "7. Get all orders..."
RESULT=$(curl -s $ORDER_BASE)
echo $RESULT | grep -q "LAPTOP-001" && pass "List orders works" || fail "List orders failed"

echo ""
echo "8. Get order by ID..."
RESULT=$(curl -s "$ORDER_BASE/$ORDER_ID")
echo $RESULT | grep -q '"id":'$ORDER_ID && pass "Get by ID works" || fail "Get by ID failed"

echo ""
echo "9. Update order status to CONFIRMED..."
RESULT=$(curl -s -X PATCH "$ORDER_BASE/$ORDER_ID/status?status=CONFIRMED")
echo $RESULT | grep -q '"status":"CONFIRMED"' && pass "Status update works" || fail "Status update failed"

echo ""
echo "10. Place invalid order (blank productCode, expect 400)..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST $ORDER_BASE \
  -H "Content-Type: application/json" \
  -d '{"productCode":"","quantity":1,"unitPrice":100}')
[ "$HTTP_CODE" = "400" ] && pass "Validation works (400 returned)" || fail "Expected 400, got $HTTP_CODE"

echo ""
echo "11. Get non-existent order (expect 404)..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$ORDER_BASE/9999")
[ "$HTTP_CODE" = "404" ] && pass "Not found works (404 returned)" || fail "Expected 404, got $HTTP_CODE"

echo ""
echo "===== Actuator health checks ====="
curl -s http://localhost:8081/actuator/health | grep -q '"status":"UP"' && pass "order-service healthy" || fail "order-service unhealthy"
curl -s http://localhost:8082/actuator/health | grep -q '"status":"UP"' && pass "inventory-service healthy" || fail "inventory-service unhealthy"

echo ""
echo "Done."