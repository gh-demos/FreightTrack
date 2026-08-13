import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";
import pg from "pg";

const { Pool } = pg;

const pool = new Pool({
  host: process.env.DB_HOST || "localhost",
  port: Number(process.env.DB_PORT || "5432"),
  database: process.env.DB_NAME || "freighttrack",
  user: process.env.DB_USERNAME || "postgres",
  password: process.env.DB_PASSWORD || "postgres"
});

const server = new McpServer({
  name: "freighttrack-shipment-lookup",
  version: "1.0.0"
});

server.tool(
  "get_shipment_by_tracking_and_email",
  "Get shipment status using tracking number and customer email. Returns only non-personal shipment data.",
  {
    trackingNumber: z.string().min(3).max(50),
    email: z.string().email().max(100)
  },
  async ({ trackingNumber, email }) => {
    const query = `
      SELECT
        s.id,
        s.tracking_number,
        s.origin,
        s.destination,
        s.status,
        s.pickup_date,
        s.expected_delivery_date,
        s.actual_delivery_date,
        s.weight,
        s.weight_unit,
        s.currency,
        s.value,
        s.created_at,
        s.updated_at
      FROM shipments s
      JOIN customers c ON c.id = s.customer_id
      WHERE s.tracking_number = $1
        AND LOWER(c.email) = LOWER($2)
      LIMIT 1
    `;

    const eventQuery = `
      SELECT
        event_type,
        location,
        description,
        event_time
      FROM tracking_events
      WHERE shipment_id = $1
      ORDER BY event_time DESC
      LIMIT 10
    `;

    let client;
    try {
      client = await pool.connect();

      const shipmentResult = await client.query(query, [trackingNumber.trim(), email.trim()]);
      if (shipmentResult.rows.length === 0) {
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(
                {
                  found: false,
                  message: "No shipment found for the provided tracking number and email."
                },
                null,
                2
              )
            }
          ]
        };
      }

      const shipment = shipmentResult.rows[0];
      const eventsResult = await client.query(eventQuery, [shipment.id]);

      const response = {
        found: true,
        shipment: {
          trackingNumber: shipment.tracking_number,
          status: shipment.status,
          origin: shipment.origin,
          destination: shipment.destination,
          pickupDate: shipment.pickup_date,
          expectedDeliveryDate: shipment.expected_delivery_date,
          actualDeliveryDate: shipment.actual_delivery_date,
          weight: shipment.weight,
          weightUnit: shipment.weight_unit,
          value: shipment.value,
          currency: shipment.currency,
          createdAt: shipment.created_at,
          updatedAt: shipment.updated_at
        },
        recentTrackingEvents: eventsResult.rows.map((e) => ({
          eventType: e.event_type,
          location: e.location,
          description: e.description,
          eventTime: e.event_time
        })),
        privacy: {
          customerPersonalDetailsExcluded: true,
          fieldsExcluded: [
            "customer.name",
            "customer.phone",
            "customer.address",
            "customer.city",
            "customer.state",
            "customer.postalCode"
          ]
        }
      };

      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(response, null, 2)
          }
        ]
      };
    } catch (error) {
      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              {
                found: false,
                error: "Failed to query shipment data.",
                detail: error instanceof Error ? error.message : "Unknown error"
              },
              null,
              2
            )
          }
        ]
      };
    } finally {
      if (client) {
        client.release();
      }
    }
  }
);

async function start() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
}

start().catch((err) => {
  console.error("Failed to start FreightTrack MCP server", err);
  process.exit(1);
});
