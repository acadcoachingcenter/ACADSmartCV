# ACAD SmartCV — Android Application

**ACAD Online Coaching Center** | acadapp.in  
*Build your academic identity. Apply smarter.*

---

## Overview

SmartCV is a native Android application that replaces the traditional PDF/Word CV with a **structured, intelligent profile system** for academics, researchers, and professionals. Candidates build rich structured profiles — projects, publications, grants, awards, education, achievements — and apply directly to universities, funding bodies, and research organisations.

---

## Architecture

```
SmartCV Android
├── MVVM Architecture (ViewModel + LiveData + StateFlow)
├── Room Database (SQLite ORM)
├── Navigation Component (Single Activity, Multi-Fragment)
├── Material Design 3 Components
└── PDF Export (iText 7)
```

### Package Structure

```
com.acad.smartcv/
├── data/
│   ├── model/          ← Room entities (Profile, Project, Publication, Award, Grant, Education, Achievement, Organisation, Application)
│   └── repository/     ← DAOs + SmartCVDatabase + SmartCVRepository
├── viewmodel/          ← SmartCVViewModel (single shared ViewModel)
└── ui/
    ├── MainActivity.kt ← Single Activity host
    ├── SplashActivity  ← ACAD branded splash
    └── screens/        ← Fragments (Home, Profile, Apply, Applications, + add forms)
```

---

## Features

### Profile Builder
- Upload existing CV (PDF/DOCX) for auto-parsing
- Photo upload with crop
- Tag-based skills input
- ORCID / LinkedIn / Website links

### Structured Sections
| Section | Fields |
|---|---|
| Projects | Title, role, duration, description, tags, funding, outcomes, GitHub/URL |
| Publications | Title, authors, journal, year, type (journal/conf/book/patent/preprint/thesis), DOI, impact factor, citations |
| Grants & Funding | Title, agency, amount, period, role (PI/Co-PI), status |
| Awards & Honours | Name, awarding body, year, category |
| Education | Degree, institution, year, field, grade |
| Other Achievements | Title, category (invited talk/patent/media/leadership/open-source/etc.), description |

### Organisation Discovery
- Pre-seeded with 12 Indian & international institutions
- Search / filter by name, field, type
- Bookmark favourite organisations
- Detail page with description, website link

### Smart Application
- Select organisation → choose position → write cover letter
- Toggle which profile sections to include (per application)
- Live preview before submission
- Status tracking: Draft → Submitted → Under Review → Shortlisted → Accepted/Rejected

### Export
- PDF generation via iText 7 (ACAD branded, structured layout)
- Shareable via Android share sheet

---

## Setup

### Requirements
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34 (minSdk 24)
- Kotlin 1.9.x

### Quick Start

```bash
# 1. Clone / open project in Android Studio
# 2. Sync Gradle (File → Sync Project with Gradle Files)
# 3. Add your ACAD logo:
#    - Place ACAD_LOGO_NEW.png in app/src/main/res/drawable/
#    - Reference as @drawable/acad_logo in layouts
# 4. Run on device or emulator (API 24+)
```

### Add ACAD Logo
Place your `ACAD_LOGO_NEW.png` in:
```
app/src/main/res/drawable/acad_logo.png
```
It is already referenced in the splash screen and app header as `@drawable/acad_logo`.

### Fonts
Add DM Sans font files to `app/src/main/res/font/`:
- `dm_sans.ttf` (Regular 400)
- `dm_sans_medium.ttf` (Medium 500)

Free download: https://fonts.google.com/specimen/DM+Sans

---

## Database Schema

```
profiles ──< projects
         ──< publications
         ──< awards
         ──< grants
         ──< education
         ──< achievements
         ──< applications >── organisations
```

All child tables cascade-delete when a profile is deleted.

---

## Extending the App

### Add a new section type
1. Add entity class in `Models.kt`
2. Add DAO in `Daos.kt`
3. Register DAO in `SmartCVDatabase`
4. Add repository methods in `SmartCVRepository`
5. Add LiveData field in `SmartCVViewModel`
6. Create `AddXxxFragment` + layout
7. Add navigation action in `nav_graph.xml`
8. Add PDF section in `PdfExporter.kt`

### Connect to backend (Supabase / Firebase)
- Replace `SmartCVRepository` calls with API calls
- Add Retrofit / Ktor client dependency
- Map Room entities to JSON DTOs

---

## Branding

- **App name**: ACAD SmartCV
- **Primary colour**: `#185FA5` (ACAD Blue)
- **Tagline**: *Build your academic identity. Apply smarter.*
- **Footer**: *Powered by ACAD Online Coaching Center · acadapp.in*

---

## License

© ACAD Online Coaching Center, Chennai. All rights reserved.  
Contact: acadcoachingcenter@gmail.com | acadapp.in
