#!/usr/bin/env python3
"""
At Risk Shipment Advisor MCP Server

This MCP server identifies shipments likely to miss expected delivery before they
are officially delayed, explains why each shipment is at risk using recent tracking
events and route context, and recommends next best actions.
"""

import anyio
import logging
from datetime import datetime
from typing import Any
import httpx

from mcp.server import Server
from mcp.server.stdio import stdio_server
from mcp.types import CallToolResult, Tool, TextContent

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Constants
BACKEND_URL = "http://localhost:8080"
AT_RISK_THRESHOLD_HOURS = 6  # Shipments at risk if within 6 hours of delivery deadline
DELIVERED_STATUSES = {"DELIVERED", "CANCELLED", "RETURNED"}
EXCLUDED_STATUSES = {"PENDING", "PENDING_PICKUP"}

# Create server instance
server = Server("at-risk-shipment-advisor")


class ShipmentRiskAnalyzer:
    """Analyzes shipment risk based on tracking events and route context."""

    def __init__(self, backend_url: str = BACKEND_URL):
        self.backend_url = backend_url
        self.client = httpx.AsyncClient(timeout=30.0)

    async def close(self):
        """Close the HTTP client."""
        await self.client.aclose()

    async def fetch_json(self, endpoint: str) -> list | dict:
        """Fetch JSON data from backend API."""
        try:
            url = f"{self.backend_url}{endpoint}"
            response = await self.client.get(url)
            response.raise_for_status()
            return response.json()
        except Exception as e:
            logger.error(f"Error fetching {endpoint}: {str(e)}")
            return [] if endpoint.endswith("s") else {}

    async def get_at_risk_shipments(self) -> list[dict]:
        """
        Find shipments likely to miss expected delivery.
        Returns a list of at-risk shipments with risk analysis.
        """
        try:
            # Fetch all shipments
            shipments = await self.fetch_json("/api/shipments?size=1000")
            if not shipments or not isinstance(shipments, dict):
                return []

            content_list = shipments.get("content", [])
            at_risk = []

            for shipment in content_list:
                # Skip already delivered or excluded shipments
                status = shipment.get("status", "")
                if status in DELIVERED_STATUSES or status in EXCLUDED_STATUSES:
                    continue

                # If already marked as DELAYED, still include but flag differently
                risk_analysis = await self._analyze_shipment_risk(shipment)
                if risk_analysis and risk_analysis.get("is_at_risk"):
                    at_risk.append(risk_analysis)

            # Sort by risk score (descending)
            at_risk.sort(key=lambda x: x.get("risk_score", 0), reverse=True)
            return at_risk

        except Exception as e:
            logger.error(f"Error getting at-risk shipments: {str(e)}")
            return []

    async def _analyze_shipment_risk(self, shipment: dict) -> dict | None:
        """Analyze risk for a single shipment."""
        try:
            tracking_number = shipment.get("trackingNumber")
            if not tracking_number:
                return None

            # Get tracking events for this shipment
            tracking_events = await self.fetch_json(
                f"/api/tracking-events?shipmentId={shipment.get('id')}&size=100"
            )
            events = tracking_events.get("content", []) if isinstance(tracking_events, dict) else tracking_events

            # Analyze risk factors
            risk_factors = []
            risk_score = 0
            status = shipment.get("status", "")

            # Parse dates
            expected_delivery = shipment.get("expectedDeliveryDate")
            actual_delivery = shipment.get("actualDeliveryDate")
            pickup_date = shipment.get("pickupDate")

            if not expected_delivery or not pickup_date:
                return None

            try:
                expected_date = datetime.strptime(expected_delivery, "%Y-%m-%d").date()
                current_time = datetime.now()
                hours_until_deadline = (
                    datetime.combine(expected_date, datetime.max.time()) - current_time
                ).total_seconds() / 3600

                # Check if already past deadline
                if hours_until_deadline < 0:
                    if status == "DELAYED":
                        return None  # Already officially delayed
                    risk_factors.append(
                        f"OVERDUE: Shipment is {abs(hours_until_deadline):.1f} hours past expected delivery date"
                    )
                    risk_score += 100
                elif hours_until_deadline <= AT_RISK_THRESHOLD_HOURS:
                    risk_factors.append(
                        f"Critical: Only {hours_until_deadline:.1f} hours until expected delivery"
                    )
                    risk_score += 50

                # Analyze tracking event progression
                if events:
                    event_analysis = self._analyze_tracking_events(events, hours_until_deadline)
                    risk_factors.extend(event_analysis.get("factors", []))
                    risk_score += event_analysis.get("score", 0)

                # Check if no recent activity
                if events and hours_until_deadline > 0:
                    last_event = events[0]  # Assuming sorted by time
                    last_event_time = last_event.get("eventTime")
                    if last_event_time:
                        try:
                            last_event_dt = datetime.fromisoformat(
                                last_event_time.replace("Z", "+00:00")
                            )
                            hours_since_update = (current_time - last_event_dt).total_seconds() / 3600
                            if hours_since_update > 12:
                                risk_factors.append(
                                    f"No activity for {hours_since_update:.1f} hours"
                                )
                                risk_score += 20
                        except:
                            pass

                # Check driver assignment
                driver_id = shipment.get("assignedDriver", {}).get("id") if isinstance(
                    shipment.get("assignedDriver"), dict
                ) else shipment.get("assignedDriver")
                if not driver_id or driver_id == 0:
                    risk_factors.append("No driver assigned")
                    risk_score += 15

                # If low hours until deadline and not yet out for delivery
                last_event_type = events[0].get("eventType", "") if events else ""
                if (
                    0 < hours_until_deadline <= AT_RISK_THRESHOLD_HOURS
                    and last_event_type != "OUT_FOR_DELIVERY"
                ):
                    risk_factors.append(
                        f"Shipment not yet out for delivery but deadline is approaching"
                    )
                    risk_score += 35

            except ValueError as e:
                logger.error(f"Error parsing dates for shipment {tracking_number}: {e}")
                return None

            # Determine if at risk
            is_at_risk = risk_score >= 15 or len(risk_factors) > 0

            if is_at_risk:
                return {
                    "tracking_number": tracking_number,
                    "shipment_id": shipment.get("id"),
                    "current_status": status,
                    "origin": shipment.get("origin"),
                    "destination": shipment.get("destination"),
                    "expected_delivery_date": expected_delivery,
                    "pickup_date": pickup_date,
                    "risk_score": min(risk_score, 100),
                    "risk_factors": risk_factors,
                    "recent_events": self._format_recent_events(events[:3]),
                    "recommendation": self._generate_recommendation(
                        status, risk_score, risk_factors
                    ),
                }

            return None

        except Exception as e:
            logger.error(f"Error analyzing shipment: {str(e)}")
            return None

    def _analyze_tracking_events(self, events: list[dict], hours_until_deadline: float) -> dict:
        """Analyze tracking events for risk factors."""
        factors = []
        score = 0

        if not events:
            factors.append("No tracking events recorded")
            score += 25
            return {"factors": factors, "score": score}

        # Check for exceptions
        exception_events = [e for e in events if "EXCEPTION" in e.get("eventType", "")]
        if exception_events:
            for exc_event in exception_events[:2]:
                desc = exc_event.get("description", "Unknown issue")
                factors.append(f"Exception: {desc}")
                score += 20

        # Check event progression
        event_types = [e.get("eventType", "") for e in events]

        # If delivery deadline is soon but not at final stage
        if hours_until_deadline <= 24:
            if "OUT_FOR_DELIVERY" not in event_types and "DELIVERED" not in event_types:
                factors.append("Expected delivery today but not yet out for delivery")
                score += 25

        # Multiple delivery attempts suggest issues
        delivery_attempts = sum(1 for e in event_types if "DELIVERY_ATTEMPTED" in e)
        if delivery_attempts > 1:
            factors.append(f"Multiple delivery attempts ({delivery_attempts})")
            score += 15

        return {"factors": factors, "score": score}

    def _format_recent_events(self, events: list[dict]) -> list[dict]:
        """Format recent events for display."""
        formatted = []
        for event in events:
            formatted.append(
                {
                    "type": event.get("eventType", ""),
                    "location": event.get("location", ""),
                    "time": event.get("eventTime", ""),
                    "description": event.get("description", ""),
                }
            )
        return formatted

    def _generate_recommendation(self, status: str, risk_score: int, risk_factors: list) -> str:
        """Generate actionable recommendation."""
        if risk_score >= 80:
            if "No driver assigned" in str(risk_factors):
                return "URGENT: Immediately assign driver and contact customer about potential delay"
            if "OVERDUE" in str(risk_factors):
                return "URGENT: Contact customer immediately - shipment is overdue. Investigate delay cause and provide updated ETA"
            return "URGENT: Proactively reach out to driver for status update and provide customer with revised delivery time"

        if risk_score >= 50:
            return "HIGH: Contact driver for current location and ETA. Be prepared to contact customer with update"

        if risk_score >= 25:
            return "MEDIUM: Monitor closely. Check with driver about any issues or delays"

        return "Monitor for changes. Consider reaching out to driver if additional delays detected"


# Global analyzer instance
analyzer = None


@server.list_tools()
async def list_tools() -> list[Tool]:
    """List available tools."""
    return [
        Tool(
            name="find_at_risk_shipments",
            description="Identify all shipments likely to miss their expected delivery date. Returns shipments with risk scores, explanations based on tracking events and route context, and recommended actions.",
            inputSchema={
                "type": "object",
                "properties": {
                    "min_risk_score": {
                        "type": "integer",
                        "description": "Minimum risk score to include (0-100). Default: 15",
                        "default": 15,
                    },
                    "sort_by": {
                        "type": "string",
                        "enum": ["risk_score", "hours_until_deadline", "expected_delivery_date"],
                        "description": "Sort results by this field. Default: risk_score",
                        "default": "risk_score",
                    },
                },
            },
        ),
        Tool(
            name="get_shipment_risk_analysis",
            description="Get detailed risk analysis for a specific shipment, including tracking event history, delay factors, and recommended actions.",
            inputSchema={
                "type": "object",
                "properties": {
                    "tracking_number": {
                        "type": "string",
                        "description": "The shipment tracking number",
                    }
                },
                "required": ["tracking_number"],
            },
        ),
        Tool(
            name="get_shipment_by_id",
            description="Get tracking events and current status for a specific shipment by ID.",
            inputSchema={
                "type": "object",
                "properties": {
                    "shipment_id": {
                        "type": "integer",
                        "description": "The shipment ID",
                    }
                },
                "required": ["shipment_id"],
            },
        ),
    ]


@server.call_tool()
async def call_tool(name: str, arguments: dict) -> CallToolResult:
    """Handle tool calls."""
    global analyzer
    if analyzer is None:
        analyzer = ShipmentRiskAnalyzer()

    try:
        if name == "find_at_risk_shipments":
            min_risk = arguments.get("min_risk_score", 15)
            sort_by = arguments.get("sort_by", "risk_score")

            at_risk_list = await analyzer.get_at_risk_shipments()

            # Filter by minimum risk score
            filtered = [s for s in at_risk_list if s.get("risk_score", 0) >= min_risk]

            # Sort
            if sort_by == "hours_until_deadline":
                # This would require additional calculation
                pass
            elif sort_by == "expected_delivery_date":
                filtered.sort(key=lambda x: x.get("expected_delivery_date", ""))

            result_text = format_at_risk_results(filtered)
            return CallToolResult(content=[TextContent(type="text", text=result_text)])

        elif name == "get_shipment_risk_analysis":
            tracking_number = arguments.get("tracking_number")
            shipments = await analyzer.fetch_json("/api/shipments?size=1000")
            content_list = shipments.get("content", []) if isinstance(shipments, dict) else []

            shipment = next(
                (s for s in content_list if s.get("trackingNumber") == tracking_number),
                None,
            )

            if not shipment:
                return CallToolResult(
                    content=[TextContent(type="text", text=f"Shipment {tracking_number} not found")]
                )

            analysis = await analyzer._analyze_shipment_risk(shipment)
            if not analysis:
                return CallToolResult(
                    content=[
                        TextContent(
                            type="text",
                            text=f"Shipment {tracking_number} is on track or already delivered",
                        )
                    ]
                )

            result_text = format_shipment_analysis(analysis)
            return CallToolResult(content=[TextContent(type="text", text=result_text)])

        elif name == "get_shipment_by_id":
            shipment_id = arguments.get("shipment_id")
            shipment = await analyzer.fetch_json(f"/api/shipments/{shipment_id}")
            events = await analyzer.fetch_json(
                f"/api/tracking-events?shipmentId={shipment_id}&size=100"
            )

            if not shipment:
                return CallToolResult(
                    content=[TextContent(type="text", text=f"Shipment ID {shipment_id} not found")]
                )

            result_text = format_shipment_detail(shipment, events)
            return CallToolResult(content=[TextContent(type="text", text=result_text)])

    except Exception as e:
        logger.error(f"Error calling tool {name}: {str(e)}")
        return CallToolResult(
            content=[TextContent(type="text", text=f"Error: {str(e)}")],
            isError=True,
        )


def format_at_risk_results(shipments: list[dict]) -> str:
    """Format at-risk shipments for display."""
    if not shipments:
        return "✓ No at-risk shipments found. All shipments are on track!"

    output = f"🚨 FOUND {len(shipments)} AT-RISK SHIPMENTS\n"
    output += "=" * 80 + "\n\n"

    for i, shipment in enumerate(shipments, 1):
        output += f"{i}. TRACKING: {shipment.get('tracking_number', 'N/A')}\n"
        output += f"   Status: {shipment.get('current_status', 'N/A')}\n"
        output += f"   Route: {shipment.get('origin', 'N/A')} → {shipment.get('destination', 'N/A')}\n"
        output += f"   Expected Delivery: {shipment.get('expected_delivery_date', 'N/A')}\n"
        output += f"   Risk Score: {shipment.get('risk_score', 0)}/100\n"
        output += f"\n   ⚠️  Risk Factors:\n"
        for factor in shipment.get("risk_factors", []):
            output += f"      • {factor}\n"
        output += f"\n   📦 Recent Events:\n"
        for event in shipment.get("recent_events", [])[:2]:
            output += f"      • [{event.get('type', 'N/A')}] {event.get('location', 'N/A')} at {event.get('time', 'N/A')}\n"
        output += f"\n   ✓ RECOMMENDATION:\n"
        output += f"      {shipment.get('recommendation', 'Monitor closely')}\n"
        output += "\n" + "-" * 80 + "\n\n"

    return output


def format_shipment_analysis(analysis: dict) -> str:
    """Format detailed shipment analysis."""
    output = f"📦 SHIPMENT RISK ANALYSIS\n"
    output += "=" * 80 + "\n\n"
    output += f"Tracking Number: {analysis.get('tracking_number', 'N/A')}\n"
    output += f"Current Status: {analysis.get('current_status', 'N/A')}\n"
    output += f"Route: {analysis.get('origin', 'N/A')} → {analysis.get('destination', 'N/A')}\n"
    output += f"Pickup Date: {analysis.get('pickup_date', 'N/A')}\n"
    output += f"Expected Delivery: {analysis.get('expected_delivery_date', 'N/A')}\n\n"

    output += f"🎯 RISK ASSESSMENT\n"
    output += "-" * 80 + "\n"
    output += f"Risk Score: {analysis.get('risk_score', 0)}/100\n\n"

    output += f"⚠️  Risk Factors:\n"
    for factor in analysis.get("risk_factors", []):
        output += f"   • {factor}\n"

    output += f"\n📊 Recent Tracking Events:\n"
    for event in analysis.get("recent_events", []):
        output += f"   • [{event.get('type', 'N/A')}] {event.get('location', 'N/A')}\n"
        output += f"     Time: {event.get('time', 'N/A')}\n"
        if event.get("description"):
            output += f"     Description: {event.get('description')}\n"

    output += f"\n✓ RECOMMENDED ACTION:\n"
    output += f"   {analysis.get('recommendation', 'Monitor closely')}\n"

    return output


def format_shipment_detail(shipment: dict, events_response: Any) -> str:
    """Format shipment details."""
    output = f"📦 SHIPMENT DETAILS\n"
    output += "=" * 80 + "\n\n"
    output += f"ID: {shipment.get('id', 'N/A')}\n"
    output += f"Tracking: {shipment.get('trackingNumber', 'N/A')}\n"
    output += f"Status: {shipment.get('status', 'N/A')}\n"
    output += f"Route: {shipment.get('origin', 'N/A')} → {shipment.get('destination', 'N/A')}\n"
    output += f"Weight: {shipment.get('weight', 'N/A')} {shipment.get('weightUnit', 'KG')}\n"
    output += f"Value: {shipment.get('currency', 'USD')} {shipment.get('value', 'N/A')}\n"
    output += f"Pickup: {shipment.get('pickupDate', 'N/A')}\n"
    output += f"Expected Delivery: {shipment.get('expectedDeliveryDate', 'N/A')}\n"
    if shipment.get("actualDeliveryDate"):
        output += f"Actual Delivery: {shipment.get('actualDeliveryDate', 'N/A')}\n"
    output += f"Description: {shipment.get('description', 'N/A')}\n\n"

    output += f"📊 TRACKING EVENTS\n"
    output += "-" * 80 + "\n"

    events = events_response.get("content", []) if isinstance(events_response, dict) else events_response
    if events:
        for event in events[:10]:
            output += f"[{event.get('eventType', 'N/A')}] {event.get('location', 'N/A')}\n"
            output += f"  Time: {event.get('eventTime', 'N/A')}\n"
            if event.get("description"):
                output += f"  Note: {event.get('description')}\n"
            output += "\n"
    else:
        output += "No tracking events found.\n"

    return output


async def main():
    """Main entry point."""
    global analyzer

    try:
        logger.info("Starting At Risk Shipment Advisor MCP Server...")
        analyzer = ShipmentRiskAnalyzer()

        # Start the server over stdio for MCP clients.
        async with stdio_server() as (read_stream, write_stream):
            logger.info("Server running over stdio.")
            await server.run(
                read_stream,
                write_stream,
                server.create_initialization_options(),
            )

    except KeyboardInterrupt:
        logger.info("Server stopped by user")
    except Exception as e:
        logger.error(f"Server error: {str(e)}")
        raise
    finally:
        if analyzer:
            await analyzer.close()


if __name__ == "__main__":
    anyio.run(main)
