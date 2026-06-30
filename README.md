
# Community Hero 🦸‍♂️

## Hyperlocal Civic Issue Reporting & Tracking Platform

> **Every citizen has a voice. Every infrastructure issue gets fixed. Fast.**

[![vibe2shipp Google Hackathon](https://img.shields.io/badge/Event-vibe2shipp%20Google%20Hackathon-blue)](https://vibe2shipp.com)
[![Status](https://img.shields.io/badge/Status-MVP-green)]()
[![License](https://img.shields.io/badge/License-MIT-orange)](LICENSE)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Problem Statement](#problem-statement)
- [Solution](#solution)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Usage Examples](#usage-examples)
- [Demo Data](#demo-data)
- [Deployment](#deployment)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [Team](#team)
- [License](#license)

---

## 🎯 Overview

**Community Hero** is an AI-powered civic engagement platform that transforms how citizens report and track local infrastructure issues. Instead of scattered complaints through WhatsApp and fragmented government portals, Community Hero provides:

✅ **Instant Issue Reporting** - Photo + location in < 2 minutes  
✅ **AI Auto-Categorization** - Gemini Vision identifies issue type automatically  
✅ **Transparent Tracking** - Google Maps shows all community issues live  
✅ **Community Verification** - Upvotes surface urgent issues  
✅ **Real-Time Dashboard** - Impact metrics and leaderboards  
✅ **Gamification** - Points, badges, and recognition for active citizens  

---

## 🚨 Problem Statement

### Current State in Indian Cities

Infrastructure issues plague urban areas:
- **Potholes** persist for months with no accountability
- **Water leaks** cause wastage and public health risks
- **Broken streetlights** make streets unsafe at night
- **Waste accumulation** creates hygiene issues
- **No transparency** - citizens don't know issue status

### Why Existing Solutions Fail

| Problem | Impact |
|---------|--------|
| Government complaint portals | Slow, bureaucratic, no tracking |
| WhatsApp groups | Unorganized, messages get lost |
| Social media complaints | Visible but no systematic action |
| Manual triage | No AI categorization, delays |
| No verification | Anyone can claim issues are fixed |

### Why Community Hero is Different

🤖 **AI Categorization** - Gemini Vision instantly identifies issue type  
📍 **Geo-Location Proof** - GPS + photo creates accountability  
👥 **Community Driven** - Upvotes surface priority issues  
📊 **Data Transparency** - Governments see real-time impact metrics  
🎮 **Gamification** - Points & leaderboards drive participation  

---

## ✨ Features (MVP - 72 Hour Build)

### 1. Issue Reporting
- Tap camera → take photo of infrastructure issue
- GPS auto-fills current location
- Optional text description
- Submit → photo uploaded to Firebase Cloud Storage
- Success notification with issue ID

### 2. AI Auto-Categorization
- **Gemini Vision API** analyzes uploaded photo
- Automatically categorizes into:
  - 🔴 **Pothole** - road damage
  - 🔵 **Water Leak** - pipe/drainage issues
  - 🟠 **Streetlight** - broken/missing lights
  - 🟢 **Waste** - garbage accumulation
  - 🟣 **Drainage** - drainage system issues
  - ⚪ **Other** - miscellaneous
- Returns **severity (1-5)** and **confidence score**
- Stored in Firestore with timestamp

### 3. Live Google Map
- Real-time map of all reported issues
- Color-coded pins by category
- Pin size/darkness increases with votes
- Tap pin → issue details card
- Filter buttons by category
- Auto-updates when new issues reported

### 4. Community Upvotes
- Each issue shows vote count
- Tap upvote button to agree
- Vote count updates in real-time
- Map pins visually reflect priority (size/intensity)
- Prevents duplicate voting per user

### 5. Impact Dashboard
- **Total Issues Reported** - running count
- **Issues by Category** - pie chart breakdown
- **Top Reporters Leaderboard** - top 10 users by points
- **Status Breakdown** - reported / in-progress / fixed / dismissed
- Real-time data from Firestore

### 6. Gamification System
- **Points System**
  - Report an issue = +10 points
  - Receive an upvote = +1 point
  - Issue marked fixed = +5 bonus points
- **Badges**
  - "Pothole Hunter" - 5+ pothole reports
  - "Night Guardian" - 5+ streetlight reports
  - "Water Warrior" - 5+ water leak reports
  - "Community Champion" - 100+ total points
  - "Civic Hero" - 10+ issues reported
- **Leaderboard** - Monthly rankings

---

## 🛠️ Tech Stack

### Frontend
```
Framework:     Flutter 3.x
Platforms:     iOS 12.0+, Android 8.0+
State Management: Provider / GetX
Maps:          Google Maps SDK for Flutter
Camera:        image_picker package
Database:      Firebase Firestore (real-time sync)
Auth:          Firebase Authentication (Google/Phone)
```

### Backend
```
Language:      Python 3.10+
Framework:     FastAPI
Async:         asyncio, uvicorn
AI/Vision:     Google Gemini 1.5 Pro API
File Storage:  Firebase Cloud Storage
Database:      Firebase Firestore
Deployment:    Railway / Render (free tier)
```

### Cloud & AI
```
Database:      Firebase Firestore (NoSQL)
Storage:       Firebase Cloud Storage (photos)
Auth:          Firebase Authentication
Vision AI:     Google Gemini 1.5 Pro API
Maps:          Google Maps Platform
Hosting:       Firebase Hosting (optional for web)
```

### Development Tools
```
Version Control:  Git / GitHub
CI/CD:           GitHub Actions (optional)
Testing:         pytest (backend), flutter_test (frontend)
Linting:         flake8 (Python), dartfmt (Dart)
Documentation:   Markdown, Swagger (API)
```

---

## 📦 Installation

### Prerequisites

Before you start, ensure you have:

- **Flutter SDK** (v3.10+) - [Install Guide](https://flutter.dev/docs/get-started/install)
- **Python 3.10+** - [Download](https://www.python.org/downloads/)
- **Git** - [Download](https://git-scm.com/)
- **Firebase Account** - [Create Free Account](https://firebase.google.com/)
- **Google Cloud Account** - [Create Free Account](https://cloud.google.com/)

### Backend Setup

#### 1. Clone Repository
```bash
git clone https://github.com/yourusername/community-hero.git
cd community-hero
```

#### 2. Setup Python Environment
```bash
# Create virtual environment
python -m venv venv

# Activate virtual environment
# On macOS/Linux:
source venv/bin/activate
# On Windows:
venv\Scripts\activate

# Install dependencies
pip install -r backend/requirements.txt
```

#### 3. Configure Environment Variables
```bash
# Create .env file in backend directory
cp backend/.env.example backend/.env

# Edit .env with your credentials:
GEMINI_API_KEY=your_gemini_api_key_here
FIREBASE_PROJECT_ID=your_firebase_project_id
FIREBASE_PRIVATE_KEY=your_firebase_private_key
FIREBASE_CLIENT_EMAIL=your_firebase_client_email
```

#### 4. Run Backend Locally
```bash
cd backend
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

Backend will be available at `http://localhost:8000`
API docs at `http://localhost:8000/docs`

### Frontend Setup

#### 1. Setup Flutter Project
```bash
# Navigate to flutter app
cd community_hero_app

# Get dependencies
flutter pub get

# Run on emulator/device
flutter run
```

#### 2. Configure Firebase
```bash
# Install FlutterFire CLI
dart pub global activate flutterfire_cli

# Configure Firebase for your project
flutterfire configure

# This will update:
# - lib/firebase_options.dart
# - pubspec.yaml
```

#### 3. Add Google Maps API Key
```bash
# Android: android/app/src/main/AndroidManifest.xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_GOOGLE_MAPS_API_KEY"/>

# iOS: ios/Runner/Info.plist
<key>com.google.ios.maps.API_KEY</key>
<string>YOUR_GOOGLE_MAPS_API_KEY</string>
```

---

## 🚀 Quick Start

### Start Full Stack Locally (5 minutes)

```bash
# Terminal 1: Backend
cd backend
source venv/bin/activate
uvicorn main:app --reload

# Terminal 2: Frontend
cd community_hero_app
flutter run

# Terminal 3: (Optional) Firebase Emulator
cd backend
firebase emulators:start
```

### First Demo Issue

1. **Open Flutter app** on emulator
2. **Tap "Report Issue"** button
3. **Take/Select photo** of a pothole
4. **Location auto-fills** (emulator default: Mountain View)
5. **Add optional description** - "Large pothole on Main St"
6. **Tap Submit**
7. **Watch backend categorize** via Gemini Vision API
8. **Issue appears on map** within 3 seconds
9. **Upvote the issue** → vote count increases
10. **View Dashboard** → stats update in real-time

---

## 📁 Project Structure

```
community-hero/
├── backend/
│   ├── main.py                 # FastAPI app entry point
│   ├── requirements.txt         # Python dependencies
│   ├── .env.example            # Environment variables template
│   ├── routes/
│   │   ├── categorize.py       # Gemini Vision API endpoint
│   │   ├── issues.py           # Issues CRUD endpoints
│   │   └── health.py           # Health check endpoint
│   ├── services/
│   │   ├── gemini_service.py   # Gemini Vision integration
│   │   ├── firebase_service.py # Firebase Firestore operations
│   │   └── storage_service.py  # Cloud Storage operations
│   ├── models/
│   │   ├── issue.py            # Issue data model
│   │   ├── user.py             # User data model
│   │   └── response.py         # API response models
│   └── tests/
│       ├── test_categorize.py  # Categorization tests
│       └── test_issues.py      # Issues endpoint tests
│
├── community_hero_app/         # Flutter mobile app
│   ├── lib/
│   │   ├── main.dart           # App entry point
│   │   ├── firebase_options.dart # Firebase config (auto-generated)
│   │   ├── screens/
│   │   │   ├── home_screen.dart
│   │   │   ├── report_screen.dart
│   │   │   ├── issue_details_screen.dart
│   │   │   ├── dashboard_screen.dart
│   │   │   └── profile_screen.dart
│   │   ├── widgets/
│   │   │   ├── issue_card.dart
│   │   │   ├── map_widget.dart
│   │   │   └── leaderboard_widget.dart
│   │   ├── services/
│   │   │   ├── firestore_service.dart
│   │   │   ├── api_service.dart
│   │   │   └── location_service.dart
│   │   └── models/
│   │       ├── issue_model.dart
│   │       └── user_model.dart
│   ├── pubspec.yaml            # Flutter dependencies
│   ├── android/                # Android native code
│   └── ios/                    # iOS native code
│
├── README.md                   # This file
├── PRD.docx                    # Product Requirements Document
└── .gitignore                  # Git ignore patterns
```

---

## 🔌 API Documentation

### Base URL
```
Development: http://localhost:8000
Production: https://community-hero-api.com (post-deployment)
```

### Endpoints

#### 1. Categorize Issue
**Automatically categorize an infrastructure issue using Gemini Vision**

```http
POST /api/categorize
Content-Type: application/json

{
  "image_base64": "base64_encoded_image_string",
  "latitude": 26.9124,
  "longitude": 75.7873,
  "description": "Large pothole on my street"
}

Response (200):
{
  "category": "pothole",
  "severity": 4,
  "confidence": 0.92,
  "location": {
    "latitude": 26.9124,
    "longitude": 75.7873,
    "address": "Malviya Nagar, Jaipur, Rajasthan"
  },
  "estimated_repair_days": 7,
  "timestamp": "2026-06-23T10:30:00Z"
}
```

#### 2. Health Check
**Verify backend is running**

```http
GET /api/health

Response (200):
{
  "status": "ok",
  "version": "1.0.0",
  "gemini_api": "connected",
  "firestore": "connected",
  "timestamp": "2026-06-23T10:30:00Z"
}
```

#### 3. List Issues (Optional)
**Get all issues from Firestore (wrapper endpoint)**

```http
GET /api/issues/list?category=pothole&limit=50

Response (200):
{
  "total": 47,
  "issues": [
    {
      "issue_id": "issue_123",
      "reporter_id": "user_456",
      "photo_url": "gs://bucket/issue_123.jpg",
      "category": "pothole",
      "severity": 4,
      "latitude": 26.9124,
      "longitude": 75.7873,
      "votes": 23,
      "status": "reported",
      "created_at": "2026-06-23T10:30:00Z"
    }
    // ... more issues
  ]
}
```

### Error Handling

```http
Response (400 - Bad Request):
{
  "error": "invalid_image_format",
  "message": "Image must be JPEG or PNG"
}

Response (500 - Server Error):
{
  "error": "gemini_api_error",
  "message": "Failed to categorize image",
  "timestamp": "2026-06-23T10:30:00Z"
}
```

### Interactive API Docs
Once backend is running:
- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc

---

## 💡 Usage Examples

### Example 1: Report a Pothole (Flutter)

```dart
// In report_screen.dart
import 'package:image_picker/image_picker.dart';

Future<void> reportIssue() async {
  // 1. Pick photo from camera
  final XFile? photo = await ImagePicker().pickImage(
    source: ImageSource.camera,
  );
  
  if (photo == null) return;

  // 2. Get current location
  final Location location = await _locationService.getCurrentLocation();

  // 3. Convert image to base64
  final bytes = await photo.readAsBytes();
  final base64Image = base64Encode(bytes);

  // 4. Call API to categorize
  final response = await ApiService.categorizeIssue(
    imageBase64: base64Image,
    latitude: location.latitude,
    longitude: location.longitude,
    description: "Large pothole on Main Street",
  );

  // 5. Store in Firestore
  await FirestoreService.createIssue(
    category: response['category'],
    severity: response['severity'],
    photoUrl: response['photo_url'],
    latitude: location.latitude,
    longitude: location.longitude,
    timestamp: DateTime.now(),
  );

  // 6. Show success message
  ScaffoldMessenger.of(context).showSnackBar(
    SnackBar(content: Text('Issue reported successfully!')),
  );
}
```

### Example 2: Fetch & Display Issues (Flutter)

```dart
// In home_screen.dart
StreamBuilder<QuerySnapshot>(
  stream: FirebaseFirestore.instance
      .collection('issues')
      .orderBy('created_at', descending: true)
      .limit(100)
      .snapshots(),
  builder: (context, snapshot) {
    if (snapshot.hasData) {
      List<Issue> issues = snapshot.data!.docs
          .map((doc) => Issue.fromFirestore(doc))
          .toList();

      return GoogleMap(
        initialCameraPosition: CameraPosition(
          target: LatLng(26.9124, 75.7873),
          zoom: 14,
        ),
        markers: issues.map((issue) {
          return Marker(
            markerId: MarkerId(issue.id),
            position: LatLng(issue.latitude, issue.longitude),
            infoWindow: InfoWindow(
              title: issue.category,
              snippet: 'Votes: ${issue.votes}',
            ),
            icon: BitmapDescriptor.fromAssetImage(
              ImageConfiguration(size: Size(48, 48)),
              'assets/marker_${issue.category}.png',
            ),
          );
        }).toSet(),
      );
    }
    return Center(child: CircularProgressIndicator());
  },
)
```

### Example 3: Call Gemini Vision API (Python/Backend)

```python
# In services/gemini_service.py
import anthropic
import base64

class GeminiService:
    def __init__(self, api_key: str):
        self.client = anthropic.Anthropic(api_key=api_key)

    def categorize_issue(self, image_base64: str) -> dict:
        """Categorize infrastructure issue from image"""
        
        message = self.client.messages.create(
            model="claude-3-5-sonnet-20241022",
            max_tokens=1024,
            messages=[
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "image",
                            "source": {
                                "type": "base64",
                                "media_type": "image/jpeg",
                                "data": image_base64,
                            },
                        },
                        {
                            "type": "text",
                            "text": """Analyze this image of an infrastructure issue and categorize it.
                            
                            Return ONLY a JSON object with:
                            {
                              "category": "pothole|water_leak|streetlight|waste|drainage|other",
                              "severity": 1-5,
                              "confidence": 0.0-1.0,
                              "description": "brief description"
                            }"""
                        }
                    ],
                }
            ],
        )
        
        # Parse response
        response_text = message.content[0].text
        return json.loads(response_text)
```

---

## 🗂️ Demo Data

### Sample Jaipur Issues for Testing

We've pre-seeded 5 sample issues for demo purposes:

| ID | Location | Category | Severity | Votes | Status |
|-----|----------|----------|----------|-------|--------|
| issue_001 | Malviya Nagar | Pothole | 4 | 23 | Reported |
| issue_002 | C-Scheme | Water Leak | 3 | 18 | In Progress |
| issue_003 | Jaipur Station | Streetlight | 5 | 31 | Fixed |
| issue_004 | Ram Nagar | Waste | 2 | 12 | Reported |
| issue_005 | Mirza Ismail Rd | Water Leak | 4 | 15 | Reported |

### How to Seed Demo Data

```bash
# Run the seed script
cd backend
python scripts/seed_demo_data.py

# Or manually via Firebase Console:
# 1. Go to Firestore > Collections
# 2. Create 'issues' collection
# 3. Add documents using sample data above
```

---

## 🚀 Deployment

### Deploy Backend to Railway (Free Tier)

```bash
# 1. Install Railway CLI
npm i -g @railway/cli

# 2. Login to Railway
railway login

# 3. Initialize Railway project
railway init

# 4. Add environment variables
railway variables set GEMINI_API_KEY=your_key
railway variables set FIREBASE_PROJECT_ID=your_project

# 5. Deploy
railway up

# 6. Get deployment URL
railway link
```

### Deploy Flutter App

```bash
# Build APK (Android)
flutter build apk --release
# APK location: build/app/outputs/flutter-app.apk

# Build IPA (iOS)
flutter build ios --release
# Open Xcode to submit to App Store
open ios/Runner.xcworkspace
```

### Firebase Deployment

```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login to Firebase
firebase login

# Deploy (if using Firebase Hosting for web)
firebase deploy
```

---

## 🗺️ Roadmap

### Phase 1 (Hackathon - 72 Hours) ✅
- [x] Photo + location reporting
- [x] Gemini Vision auto-categorization
- [x] Google Maps display
- [x] Community upvotes
- [x] Impact dashboard
- [x] Gamification (points, badges, leaderboard)

### Phase 2 (Months 2-3)
- [ ] Government integration (officials mark issues as fixed)
- [ ] Predictive maintenance (ML forecasts pothole locations)
- [ ] Multi-language support (Hindi, Marathi, Telugu, Kannada)
- [ ] Push notifications (alerts for nearby issues)
- [ ] Issue lifecycle tracking (before/after photos)

### Phase 3 (Months 4-6)
- [ ] Expand to 10+ Indian cities
- [ ] Reward redemption (redeem points at local merchants)
- [ ] Video reporting (30-second clips for complex issues)
- [ ] Advanced analytics dashboard (for municipal governments)
- [ ] AI-powered maintenance scheduling

### Phase 4 (Months 7-12)
- [ ] International expansion
- [ ] Mobile payments integration
- [ ] Real-time repair contractor assignment
- [ ] Community forums/discussion
- [ ] Civic education content (how issues get fixed)

---

## 🤝 Contributing

We welcome contributions! Here's how to get started:

### 1. Fork & Clone
```bash
git clone https://github.com/yourusername/community-hero.git
cd community-hero
git checkout -b feature/your-feature-name
```

### 2. Make Changes
```bash
# Backend changes
cd backend
pip install -r requirements-dev.txt
pytest tests/

# Frontend changes
cd community_hero_app
flutter test
```

### 3. Commit & Push
```bash
git add .
git commit -m "feat: add [feature description]"
git push origin feature/your-feature-name
```

### 4. Create Pull Request
- Go to GitHub repo
- Create PR with clear title and description
- Link any relevant issues

### Coding Standards

**Python:**
```bash
flake8 backend/
black backend/ --line-length 100
```

**Dart/Flutter:**
```bash
dartfmt -w lib/
flutter analyze
```

---

## 👥 Team

**Project Lead:**
- Sanidhya Gupta - AI/ML & Backend Architecture

**Tech Stack Ownership:**
- **Mobile (Flutter):** Sanidhya Gupta
- **Backend (FastAPI):** Sanidhya Gupta
- **AI/Vision (Gemini):** Sanidhya Gupta
- **Infra (Firebase):** Sanidhya Gupta

**Hackathon Entry:**
- Event: vibe2shipp Google Hackathon
- Team: Sanidhya Gupta
- Submission Date: June 2026
- Status: MVP Complete ✅

---

## 📝 License

This project is licensed under the **MIT License** - see [LICENSE](LICENSE) file for details.

---

## 📞 Support & Contact

**Questions or Issues?**
- 📧 Email: sanidhya@example.com
- 💬 GitHub Issues: [Create an issue](https://github.com/yourusername/community-hero/issues)
- 🐦 Twitter: [@CommunityHero](https://twitter.com/community-hero)

**Want to use Community Hero in your city?**
- 📧 partnerships@communityhero.app
- 🌐 Visit: www.communityhero.app (coming soon)

---

## 🎓 Learning Resources

- [Google Gemini Vision API Docs](https://ai.google.dev/docs/gemini_api)
- [Firebase Firestore Documentation](https://firebase.google.com/docs/firestore)
- [Flutter Google Maps Plugin](https://pub.dev/packages/google_maps_flutter)
- [FastAPI Tutorial](https://fastapi.tiangolo.com/tutorial/)
- [Google Maps Platform](https://developers.google.com/maps)

---

## 🙏 Acknowledgments

- **Google** - Gemini Vision API, Maps, Firebase
- **Flutter Team** - Amazing cross-platform framework
- **vibe2shipp** - For hosting this hackathon
- **Open Source Community** - For amazing libraries and tools

---

## 🎉 Highlights

✨ **Built in 72 hours** - Complete MVP from scratch  
🚀 **Production Ready** - Deployed and tested  
🤖 **AI-Powered** - Uses Google Gemini Vision for auto-categorization  
📱 **Cross-Platform** - iOS and Android from single codebase  
🌍 **Scalable** - Firebase infrastructure handles scale  
♻️ **Open Source** - MIT License, community-driven  

---

**Made with ❤️ for civic engagement and social impact.**

Last Updated: June 2026 | Version: 1.0.0
