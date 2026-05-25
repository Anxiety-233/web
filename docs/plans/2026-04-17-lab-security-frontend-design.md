# Laboratory Device Security Frontend Design

**Date:** 2026-04-17

**Goal**

Build a Vue single-page frontend for the laboratory device web security system that can be demonstrated independently before the backend exists, while preserving realistic API boundaries for later integration with JWT, RBAC, replay protection, and audit logging.

**Project Context**

The repository currently contains a minimal Vue + Vite starter with a single `App.vue`, an empty router, and one basic Vitest test. There is no existing business UI, state layer, or API integration code.

## Scope

The frontend will cover the project-facing requirements that are meaningful without a backend:

- Login page
- Device dashboard
- Device detail page for temperature sensors and switch controllers
- Audit log viewer with CSV export
- Role-aware UI for ordinary users and administrators
- Token lifecycle visualization for access token expiry and refresh flow
- Replay-attack and unauthorized-access demo states
- Mock API layer with the same interface shape a backend will later expose

The frontend will not implement real cryptography, real JWT parsing, real cookie management, or real HTTPS/TLS handling. Those concerns will be represented in the UI copy, request metadata, and service boundaries so the backend team can connect later without redesigning the app.

## Recommended Approach

Use a single Vue SPA with Vue Router and a thin in-project mock service layer.

Why this approach:

- It demonstrates the complete user journey in one coherent application.
- It keeps the UI realistic by modeling authentication, roles, and error states instead of hardcoding static screens.
- It minimizes future backend integration work by isolating all request behavior in services.

Rejected alternatives:

- A pure static prototype would look polished but would not convincingly demonstrate auth, RBAC, or security flows.
- A multi-page loose prototype would be faster but weaker as a system demo and harder to evolve into a production-like frontend.

## Information Architecture

### Routes

- `/login`
- `/dashboard`
- `/devices/:id`
- `/logs`
- `/forbidden`

### Access Rules

- Unauthenticated users can only access `/login`.
- Authenticated ordinary users can access `/dashboard` and `/devices/:id`.
- Only authenticated administrators can access `/logs`.
- Unauthorized route visits redirect to `/login` or `/forbidden` depending on the failure mode.

## Visual Direction

The UI should look like a laboratory operations console rather than a generic admin panel.

- Theme: industrial control room
- Palette: graphite, off-white, safety orange, cyan signal accents
- Background: layered panels with subtle grid and noise texture
- Typography: instrument-like heading font paired with a readable body font
- Components: status chips, metric tiles, signal lamps, command panels

This direction supports the course topic and makes the project visually memorable during presentation.

## Screen Design

### Login

The login view combines:

- Username and password form
- Security overview panel that explains:
  - Access Token valid for 15 minutes
  - Refresh Token valid for 7 days
  - Refresh token stored via HttpOnly cookie
  - Requests carry timestamp and nonce metadata
- Demo user shortcuts for ordinary user and admin
- Clear failure states for invalid credentials and expired sessions

### Dashboard

The dashboard is the main system overview. It shows:

- Current user identity and role
- Access token remaining time
- Backend connection badge showing mock mode
- Device cards for all devices
- Recent alerts or risk events
- Role-sensitive command affordances

Ordinary users see view-only actions. Administrators see control actions.

### Device Detail

The device page contains:

- Device status summary
- Metadata panel
- Recent telemetry or state changes
- Control panel
- Recent audit entries for the selected device

The control panel remains visible for ordinary users but disabled with a clear RBAC explanation. This makes the permission system demonstrable instead of invisible.

### Audit Logs

The logs view is admin-only and combines:

- Search and filters
- Table for audit records
- Highlighted risk labels for unauthorized access, replay blocking, and privilege misuse
- CSV export action

Displayed fields:

- User
- Time
- Action
- IP
- User-Agent
- Result
- Nonce
- Timestamp validation status

## State Design

Use composable state modules instead of adding a full external state library. The initial app is small enough for this and it avoids unnecessary dependency growth.

### Auth State

Tracks:

- Current user
- Role
- Access token metadata
- Token expiry timestamp
- Refresh status
- Auth loading and error states

### Device State

Tracks:

- Device list
- Active device detail
- Loading state
- Command submission state
- Last updated time

### Audit State

Tracks:

- Log records
- Filters
- Pagination
- Export state

### UI State

Tracks:

- Global notifications
- Demo mode
- Backend connection badge
- Modal visibility if needed

## API Boundary Design

All network behavior should be isolated behind service modules.

### Services

- `authService`
  - `login`
  - `refreshSession`
  - `logout`
- `deviceService`
  - `listDevices`
  - `getDeviceById`
  - `runDeviceAction`
- `auditService`
  - `listLogs`
  - `exportLogsCsv`
- `securityService`
  - `createRequestMetadata`
  - `validateDemoReplayMode`

### Request Shape

The service layer should model real backend expectations:

- `Authorization: Bearer <access_token>`
- `X-Timestamp`
- `X-Nonce`

The mock implementation will not verify cryptographic signatures, but it will carry these values through the simulated request lifecycle.

## Demo Modes

To support classroom presentation without backend tooling, include a local demo mode switch with these states:

- Normal
- Token Expired
- Permission Denied
- Replay Blocked

Changing the mode updates responses and interface feedback consistently across pages.

## Error Handling

Security-related failures should remain distinct:

- `401`: session expired or login required
- `403`: role denied
- replay blocked: explicit security warning state
- backend unavailable: mock/disconnected system status

The UI should never collapse these into a single generic error toast.

## Testing Strategy

The frontend will use Vitest and Vue Test Utils for behavior-focused tests.

Priority coverage:

- Login flow renders and transitions correctly
- Route guards enforce auth and admin-only access
- Role-based UI disables or enables control actions
- Demo mode changes security outcomes
- CSV export returns the expected mock file content

Because no backend exists yet, test emphasis should stay on route behavior, state transitions, and view logic rather than network libraries.

## Handoff Considerations

The backend team should later be able to replace only the mock service implementations while preserving:

- Route structure
- Page layout
- Role-aware UI logic
- Request metadata generation
- Error and audit presentation

That boundary is the main design constraint for this frontend.
