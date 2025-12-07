#!/bin/bash

# OLSHEETS Data Insertion Script
# This script inserts sample data into the OLSHEETS application via REST API
# Make sure the application is running on http://localhost:8080 before executing

BASE_URL="http://localhost:8080"

echo "========================================="
echo "OLSHEETS Data Insertion Script"
echo "========================================="
echo ""

# Check if server is running
echo "Checking if server is running..."
if ! curl -s -o /dev/null -w "%{http_code}" "$BASE_URL" | grep -q "200\|302\|404"; then
    echo "❌ Error: Server is not running at $BASE_URL"
    echo "Please start the application first with: mvn spring-boot:run"
    exit 1
fi
echo "✓ Server is running"
echo ""

# Function to register an instrument
register_instrument() {
    local name="$1"
    local price="$2"
    local type="$3"
    local family="$4"
    local age="$5"
    local owner_id="$6"
    local description="$7"

    echo "Registering instrument: $name"
    
    response=$(curl -s -X POST "$BASE_URL/api/instruments/register" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"$name\",
            \"price\": $price,
            \"type\": \"$type\",
            \"family\": \"$family\",
            \"age\": $age,
            \"ownerId\": $owner_id,
            \"description\": \"$description\"
        }")
    
    if echo "$response" | grep -q "id"; then
        echo "  ✓ Successfully registered"
    else
        echo "  ❌ Failed: $response"
    fi
}

# Function to register a music sheet
register_sheet() {
    local name="$1"
    local composer="$2"
    local category="$3"
    local price="$4"
    local owner_id="$5"
    local description="$6"
    local instrumentation="$7"
    local duration="$8"

    echo "Registering music sheet: $name"
    
    # Note: Adjust the endpoint based on your actual API
    # This is a placeholder as the sheet registration endpoint may differ
    echo "  ⚠ Music sheet registration endpoint not implemented in script"
    echo "  Please use the DataInitializer component or implement the endpoint"
}

echo "========================================="
echo "Inserting Instruments"
echo "========================================="
echo ""

# Insert sample instruments
register_instrument "Yamaha P-125 Digital Piano" 45.0 "DIGITAL" "KEYBOARD" 2 1 "Professional 88-key digital piano with weighted keys"
register_instrument "Fender Stratocaster" 60.0 "ELECTRIC" "GUITAR" 5 1 "Classic electric guitar with vintage tone"
register_instrument "Gibson Les Paul" 75.0 "ELECTRIC" "GUITAR" 3 2 "Premium electric guitar with mahogany body"
register_instrument "Taylor 214ce Acoustic" 50.0 "ACOUSTIC" "GUITAR" 1 2 "Beautiful acoustic guitar with cutaway"
register_instrument "Roland TD-17 Drum Kit" 80.0 "DRUMS" "PERCUSSION" 2 3 "Electronic drum set with mesh heads"
register_instrument "Yamaha YAS-280 Alto Sax" 55.0 "WIND" "WOODWIND" 4 3 "Student-level alto saxophone"
register_instrument "Fender Precision Bass" 55.0 "BASS" "GUITAR" 6 4 "Classic 4-string electric bass"
register_instrument "Korg Minilogue XD" 65.0 "SYNTHESIZER" "KEYBOARD" 1 4 "Polyphonic analog synthesizer"
register_instrument "Martin D-28 Acoustic" 85.0 "ACOUSTIC" "GUITAR" 10 5 "Premium dreadnought acoustic guitar"
register_instrument "Yamaha P-45 Digital Piano" 35.0 "DIGITAL" "KEYBOARD" 3 5 "Compact 88-key digital piano"

echo ""
echo "========================================="
echo "Data Insertion Complete!"
echo "========================================="
echo ""
echo "Note: For music sheets, please use the DataInitializer component"
echo "or restart the application to trigger automatic data initialization."
echo ""
echo "To verify the data, visit:"
echo "  - Application: $BASE_URL"
echo "  - H2 Console: $BASE_URL/h2-console"
echo ""
