# FreightTrack MCP Server

This MCP server exposes one privacy-safe tool:

- `get_shipment_by_tracking_and_email`

It requires:
- `trackingNumber`
- `email`

The tool validates both values and only returns non-personal shipment information.

## Privacy Behavior

No personal customer details are returned. The response excludes:
- customer name
- customer phone
- customer address
- customer city
- customer state
- customer postal code

## Setup

1. Install dependencies:

```bash
cd .vscode/mcp-server
npm install
```

2. Ensure PostgreSQL is running and your FreightTrack tables exist.

3. Confirm `.vscode/mcp.json` env variables are correct for your database.

4. Restart VS Code so MCP configuration is reloaded.

## Tool Response

If a shipment matches tracking number + email, the tool returns:
- tracking number
- shipment status
- origin and destination
- pickup and delivery dates
- weight and declared value
- timestamps
- recent tracking events (up to 10)

If no match exists, it returns `found: false`.
