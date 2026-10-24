# AVCOE Campus Lost & Found Platform

A full-stack web application for Amrutvahini College of Engineering, Sangamner. Students and staff can report lost/found items, communicate privately with verification, and arrange campus handovers.

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Frontend | HTML5, CSS3, Vanilla JavaScript |
| Backend | Java 17, Spring Boot 3.2.5, Spring Security, Spring Data JPA, WebSocket/STOMP |
| Database | H2 (dev) / PostgreSQL (production) |
| Authentication | JWT (JSON Web Tokens) |
| Real-time | WebSocket via SockJS + STomp |
| File Storage | Local filesystem (configurable for cloud) |

## Project Structure

```
avcoe-lost-found/
├── frontend/               # Static frontend files
│   ├── index.html          # Landing page
│   ├── login.html          # Login
│   ├── register.html       # Registration
│   ├── dashboard.html      # Dashboard
│   ├── lost-items.html     # Lost items listing
│   ├── found-items.html    # Found items listing
│   ├── item.html           # Item detail page
│   ├── create-lost.html    # Report lost item
│   ├── create-found.html   # Report found item
│   ├── messages.html       # Real-time chat
│   ├── profile.html        # User profile
│   ├── notifications.html  # Notifications
│   ├── admin.html          # Admin panel
│   ├── css/                # Stylesheets
│   │   ├── style.css       # Base styles & variables
│   │   ├── components.css  # Reusable components
│   │   ├── responsive.css  # Responsive layout & sidebar
│   │   └── animations.css  # Animations
│   └── js/                 # JavaScript modules
│       ├── api.js          # API client
│       ├── auth.js         # Authentication manager
│       └── utils.js        # Shared utilities
├── backend/                # Spring Boot application
│   └── src/main/java/com/avcoe/lostfound/
│       ├── controller/     # REST controllers
│       ├── service/        # Business logic
│       ├── repository/     # JPA repositories
│       ├── entity/         # Database entities
│       ├── dto/            # Data transfer objects
│       ├── security/       # JWT & authentication
│       ├── config/         # Configuration classes
│       ├── websocket/      # WebSocket handlers
│       └── exception/      # Error handling
├── build.sh                # Build script
└── start.sh                # Start script
```

## Quick Start

### Prerequisites
- Java 17+ installed
- Maven (or use the included Maven wrapper)

### Build & Run

```bash
# Build the project
./build.sh

# Start the application
./start.sh
```

Then open http://localhost:8080 in your browser.

### Manual Build

```bash
cd backend
mvn package -DskipTests
cp -r ../frontend/* src/main/resources/static/
mvn package -DskipTests
java -jar target/lostfound-1.0.0.jar
```

## Local Demo Accounts

The development setup seeds demo accounts and sample data. The Render deployment disables demo account seeding.
| Email | Password | Role |
|-------|----------|------|
| finder@example.com | password123 | STUDENT (Rahul Patil) |
| claimant1@example.com | password123 | STUDENT (Priya Sharma) |
| claimant2@example.com | password123 | STUDENT (Amit Kumar) |
| admin@example.com | admin123 | ADMIN (Dr. Suresh Deshmukh) |
| faculty@example.com | password123 | FACULTY (Prof. Sunita Jadhav) |

## API Documentation

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### Items
- `GET /api/items` - List items (with search, filter, pagination)
- `GET /api/items/{id}` - Get item details
- `POST /api/items` - Create new item
- `DELETE /api/items/{id}` - Delete/close item
- `GET /api/items/my` - Get my items
- `GET /api/items/{id}/matches` - Get possible matches

### Images
- `POST /api/images/item/{itemId}` - Upload images (multipart)
- `GET /api/images/item/{itemId}` - Get item images
- `DELETE /api/images/{imageId}` - Delete image

### Claims
- `POST /api/claims` - Create claim
- `GET /api/claims/item/{itemId}` - Get item claims
- `GET /api/claims/my` - Get my claims
- `PUT /api/claims/{claimId}/status` - Update claim status

### Conversations (Chat)
- `GET /api/conversations` - Get my conversations
- `GET /api/conversations/{id}/messages` - Get messages
- `POST /api/conversations/{id}/messages` - Send message
- `POST /api/conversations/{id}/verification-question` - Send verification question

### Notifications
- `GET /api/notifications` - Get notifications
- `GET /api/notifications/unread-count` - Get unread count
- `PUT /api/notifications/mark-all-read` - Mark all as read

### Handovers
- `POST /api/handovers` - Schedule handover
- `GET /api/handovers/my` - Get my handovers
- `PUT /api/handovers/{id}/confirm` - Confirm handover

### Reports
- `POST /api/reports` - Report listing

### Profile
- `GET /api/profile` - Get profile
- `PUT /api/profile` - Update profile

### Locations
- `GET /api/locations` - Get campus locations

### Admin (requires ADMIN role)
- `GET /api/admin/dashboard` - Get stats
- `GET /api/admin/users` - Get all users
- `PUT /api/admin/users/{id}/toggle-status` - Enable/disable user
- `GET /api/admin/reports` - Get reports

### WebSocket
- Connect to `/ws` via SockJS
- Send messages to `/app/chat/{conversationId}`
- Subscribe to `/topic/conversation/{conversationId}`
- Subscribe to `/user/queue/messages` for notifications

## Key Features

### 1. Private Verification System
Found-item reporters set a verification question for claimants to answer. Lost-item reporters can set a question for someone who found their item, with a general question used by default. Each response is saved as the first message in a private conversation; the reporter can approve or dismiss the request, and normal chat stays locked until approval.

### 2. Isolated Claim Conversations
Each claim or lost-item response creates a separate private conversation. Other claimants cannot see the response or messages. Only the reporter and the specific respondent can see their conversation.

### 3. Real-time Chat
WebSocket-based real-time messaging. Messages appear instantly without page refresh.

### 4. Smart Matching
When viewing a lost item, the system suggests possible matches from found items based on name similarity.

### 5. Campus Locations
Configurable list of campus locations (Library, Canteen, Labs, etc.)

### 6. Image Upload
Support for JPG, JPEG, PNG, WebP images up to 10MB each, with multiple image support per item.

### 7. Role-based Access Control
Four roles: STUDENT, FACULTY, STAFF, ADMIN with appropriate permissions.

### 8. Responsive Design
Mobile-first responsive design with sidebar navigation on desktop and bottom navigation on mobile.

## Database Schema

The application uses 14 database tables:
- users, profiles, items, item_images, campus_locations
- claims, conversations, conversation_participants, messages
- notifications, handovers, reports, audit_logs, categories

## Security Features

- Password hashing with BCrypt
- JWT token authentication
- Role-based authorization
- Backend validation
- Conversation access control (users can only access their own conversations)
- File upload validation (type + size)
- Input sanitization
- CORS configuration

## Deploy to Render

The repository includes a free Render Blueprint in `render.yaml`. It creates only a free web service, uses the default in-memory H2 database, disables demo accounts and the H2 console, and generates a JWT signing secret. No paid database or persistent disk is provisioned.

1. In the [Render Dashboard](https://dashboard.render.com), select **New → Blueprint** and connect this GitHub repository.
2. Confirm the Blueprint shows a **Free** web service and no database or disk, then apply it. Do not upgrade the service or add paid resources.
3. Set `APP_INITIAL_ADMIN_EMAIL` and `APP_INITIAL_ADMIN_PASSWORD` when prompted. Use a password with at least 12 characters. The account is created as an admin on startup if it does not already exist.
4. Wait for deployment to finish, open the generated `onrender.com` URL, and register regular accounts.

**Free-tier limitations:** Render spins down an idle free web service and its filesystem is ephemeral. This app's free deployment uses an in-memory database and stores uploaded images locally, so user accounts, listings, messages, and images can be lost when the service sleeps, restarts, or redeploys. The site may take about a minute to wake after inactivity. Render free services have usage limits; check the current Render free plan terms. This option is suitable for a demo, not reliable long-term storage or production use. Local development continues to use H2 and seeded demo data.

## Deploy to other platforms

For production deployment:

1. Switch from H2 to PostgreSQL:
```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/avcoe_lostfound
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=validate
```

2. Build the JAR:
```bash
mvn package -DskipTests
```

3. Deploy to any Java-capable server (Heroku, AWS, DigitalOcean, etc.)

## Limitations

- H2 in-memory database loses data on restart (use PostgreSQL for persistence)
- Local image storage is filesystem-based; Render uses a persistent disk. Configure cloud object storage if uploads need to be shared across multiple app instances.
- WebSocket reconnection may require page refresh in edge cases
- No email verification on registration
- No password reset functionality

## License

Built for educational purposes at Amrutvahini College of Engineering, Sangamner.
