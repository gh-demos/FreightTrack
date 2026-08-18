# At Risk Shipment Advisor MCP

An intelligent Model Context Protocol (MCP) server for the FreightTrack application that **proactively identifies shipments at risk of missing their expected delivery date** before they are officially marked as delayed.

## Features

✨ **Key Capabilities**:
- **Proactive Detection**: Identifies at-risk shipments before they're officially delayed
- **Risk Scoring**: Assigns risk scores (0-100) based on multiple factors
- **Smart Analysis**: Evaluates tracking events, driver status, and route context
- **Explainability**: Provides clear reasons why each shipment is at risk
- **Actionable Recommendations**: Generates specific next-best actions for operations teams

## How It Works

The advisor analyzes shipments based on:

1. **Time-to-Deadline Analysis**
   - Calculates hours remaining until expected delivery
   - Flags shipments within 6 hours of deadline as critical
   - Detects overdue shipments

2. **Tracking Event Analysis**
   - Examines delivery progress (PICKED_UP → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED)
   - Identifies exceptions (damaged, lost, detained, etc.)
   - Detects multiple delivery attempts (indicating issues)
   - Monitors for inactivity gaps

3. **Operational Context**
   - Checks driver assignment status
   - Verifies out-for-delivery status relative to time-to-deadline
   - Considers pickup-to-delivery timeframe

4. **Risk Scoring Algorithm**
   ```
   Risk Score = Sum of:
   - Overdue: +100 points
   - Critical (<6h to deadline): +50 points
   - No tracking activity (>12h): +20 points
   - Not out for delivery (<24h to deadline): +25 points
   - Multiple delivery attempts: +15 points per attempt
   - No driver assigned: +15 points
   - Exception events: +20 points each
   - Other warnings: +10-25 points
   ```

## Tools Provided

### 1. `find_at_risk_shipments`
Scan all shipments and identify those at risk of missing delivery.

**Parameters:**
- `min_risk_score` (optional): Filter by minimum risk (default: 15, range: 0-100)
- `sort_by` (optional): Sort results by `risk_score`, `hours_until_deadline`, or `expected_delivery_date`

**Returns:**
- List of at-risk shipments with:
  - Tracking number and status
  - Origin/destination route
  - Expected delivery date
  - Risk score (0-100)
  - Risk factors explanation
  - Recent tracking events
  - Recommended action

**Example Response:**
```
🚨 FOUND 3 AT-RISK SHIPMENTS
================================================================================

1. TRACKING: TRK-2024-001234
   Status: IN_TRANSIT
   Route: New York → Los Angeles
   Expected Delivery: 2024-01-15
   Risk Score: 78/100

   ⚠️  Risk Factors:
      • Critical: Only 4.2 hours until expected delivery
      • Shipment not yet out for delivery but deadline is approaching
      • No activity for 8.5 hours

   📦 Recent Events:
      • [IN_TRANSIT] Denver, CO at 2024-01-14 14:30:00
      • [PICKED_UP] New York, NY at 2024-01-14 06:00:00

   ✓ RECOMMENDATION:
      URGENT: Proactively reach out to driver for status update and provide customer with revised delivery time
```

### 2. `get_shipment_risk_analysis`
Get detailed risk analysis for a specific shipment.

**Parameters:**
- `tracking_number` (required): The shipment tracking number

**Returns:**
- Detailed breakdown of:
  - Shipment route and dates
  - Risk score and factors
  - Complete recent tracking event history
  - Specific recommended action

### 3. `get_shipment_by_id`
Retrieve full tracking history for a shipment by ID.

**Parameters:**
- `shipment_id` (required): The shipment database ID

**Returns:**
- Complete shipment details
- Full tracking event log (most recent first)

## Installation

### Prerequisites
- Python 3.8+
- FreightTrack backend API running on `http://localhost:8080`

### Setup Steps

1. **Clone/extract the MCP server**:
   ```bash
   cd mcp_at_risk_advisor
   ```

2. **Create a virtual environment**:
   ```bash
   python -m venv venv
   
   # On Windows:
   venv\Scripts\activate
   # On macOS/Linux:
   source venv/bin/activate
   ```

3. **Install dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

4. **Verify backend connectivity**:
   ```bash
   curl http://localhost:8080/api/shipments
   ```

5. **Run the MCP server**:
   ```bash
   python server.py
   ```

   Expected output:
   ```
   INFO:__main__:Starting At Risk Shipment Advisor MCP Server...
   INFO:__main__:Server running. Press Ctrl+C to stop.
   ```

## Integration with Claude

### In VS Code Copilot Chat

1. Open VS Code and go to Copilot settings
2. Add this MCP server to your Copilot configuration:
   ```json
   {
     "mcpServers": {
       "at-risk-shipment-advisor": {
         "command": "python",
         "args": ["/path/to/mcp_at_risk_advisor/server.py"]
       }
     }
   }
   ```

3. Restart Copilot Chat

### Usage Examples in Chat

**Example 1: Quick Risk Scan**
```
@copilot Can you scan for at-risk shipments? Use the at-risk-shipment-advisor MCP.
```

**Example 2: High-Risk Only**
```
@copilot Show me only the highest-risk shipments (score > 70) using the at-risk-shipment-advisor
```

**Example 3: Specific Shipment Analysis**
```
@copilot Use the at-risk-shipment-advisor to analyze shipment TRK-2024-001234. What's the risk?
```

**Example 4: Operational Priority**
```
@copilot Which at-risk shipments should we contact drivers about TODAY? Use at-risk-shipment-advisor.
```

## Understanding Risk Factors

### Critical Risk Indicators (High Priority)

| Factor | Meaning | Action |
|--------|---------|--------|
| **OVERDUE** | Hours past expected delivery | Immediate customer contact required |
| **Critical: X hours until deadline** | <6 hours remaining | Contact driver for status immediately |
| **Not yet out for delivery** | <24h to deadline, not in last mile | Escalate to driver/operations |
| **No driver assigned** | Shipment not assigned to carrier | Assign driver immediately |
| **No activity for X hours** | No tracking updates recently | Check driver status/communication |

### Medium Risk Indicators

| Factor | Meaning | Action |
|--------|---------|--------|
| **Multiple delivery attempts** | Multiple failed attempts | Contact customer, resolve issues |
| **Exception** | Damage, delay, detention, etc. | Assess impact, notify customer |

## Customization

### Adjusting Risk Thresholds

Edit the constants in `server.py`:

```python
# Time until deadline when shipment is critical (hours)
AT_RISK_THRESHOLD_HOURS = 6

# Statuses that exclude from analysis (no point checking delivered shipments)
DELIVERED_STATUSES = {"DELIVERED", "CANCELLED", "RETURNED"}
EXCLUDED_STATUSES = {"PENDING", "PENDING_PICKUP"}

# Backend API URL
BACKEND_URL = "http://localhost:8080"
```

### Modifying Risk Score Weights

In the `_analyze_tracking_events()` and `_analyze_shipment_risk()` methods, adjust these score values:

```python
# Example: Make multiple attempts higher priority
delivery_attempts = sum(1 for e in event_types if "DELIVERY_ATTEMPTED" in e)
if delivery_attempts > 1:
    risk_factors.append(f"Multiple delivery attempts ({delivery_attempts})")
    score += 25  # ← Increase this value for higher priority
```

### Adding New Risk Factors

Add analysis in `_analyze_shipment_risk()`:

```python
# Example: Check weather-affected regions
weather_affected_regions = ["Seattle", "Denver", "Chicago"]
if any(region in str(events) for region in weather_affected_regions):
    risk_factors.append(f"Shipment in weather-affected region")
    risk_score += 10
```

## API Reference

### Endpoints Used

The MCP connects to the FreightTrack backend API:

| Endpoint | Purpose |
|----------|---------|
| `GET /api/shipments?size=1000` | Get all shipments with pagination |
| `GET /api/shipments/{id}` | Get shipment details |
| `GET /api/tracking-events?shipmentId={id}&size=100` | Get tracking events for shipment |

### Event Types

Common tracking event types:

```
PENDING - Shipment registered, waiting for pickup
PICKED_UP - Collected from origin
IN_TRANSIT - On the way
IN_WAREHOUSE - Temporary storage
OUT_FOR_DELIVERY - On delivery route
DELIVERED - Successfully delivered
DELIVERY_ATTEMPTED - Delivery attempt made
DELIVERY_EXCEPTION - Issue with delivery (no one home, address issue, etc.)
EXCEPTION - General issue (damaged, lost, detained, etc.)
CANCELLED - Shipment cancelled
RETURNED - Returned to sender
```

## Troubleshooting

### Issue: "Connection refused" when starting server

**Cause**: Backend API not running

**Solution**:
```bash
# In a separate terminal, start the FreightTrack backend
cd backend
mvn spring-boot:run
```

### Issue: No shipments returned

**Cause**: No shipments in database or all are already delivered

**Solution**:
```bash
# Check database has shipments
curl http://localhost:8080/api/shipments?size=5

# Run simulation to generate test data
curl -X POST http://localhost:8080/api/shipments/simulate
```

### Issue: Import errors with MCP

**Cause**: MCP library not properly installed

**Solution**:
```bash
pip install --upgrade pip
pip install -r requirements.txt --force-reinstall
```

### Issue: Server hangs or slow response

**Cause**: Large number of shipments or slow network

**Solution**:
```python
# In server.py, reduce page size or add timeout
timeout=10.0  # Increase from 30.0 if needed
# Fetch with smaller pagination
"/api/shipments?size=500"  # Reduce from 1000
```

## Performance Considerations

- **Shipment Volume**: Server efficiently handles 500-1000 shipments
- **Response Time**: ~2-5 seconds for typical operations
- **Memory Usage**: ~50-100MB for standard deployments

For larger installations (>5000 shipments), consider:
1. Adding backend filtering by status/date range
2. Implementing caching of shipment data
3. Using event streaming instead of polling

## Contributing

Improvements welcome! Consider:
- Adding weather/traffic impact analysis
- Integrating with driver APIs for real-time location
- Machine learning for delay prediction
- Historical trend analysis
- Multi-warehouse support

## License

Part of the FreightTrack demo application.

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review backend logs: `docker logs freighttrack-backend`
3. Test API connectivity: `curl http://localhost:8080/api/shipments`
4. Verify Python environment: `python --version && pip list | grep mcp`
