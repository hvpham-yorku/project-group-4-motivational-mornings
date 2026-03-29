# EECS 2311 Project Group 4: Motivational Mornings

## ITR3 Highlights

### Aggregator
 - Implemented the aggregator feature
 - Allow the user to input a link to a source
 - The aggregator uses-webscraping to extract news articles from the given source
  - The aggregator assumes that the source given contains headlines to articles in a format similar to "https://www.cnn.com/world"

### Analytics Dashboard
 - Implemented analytics as a user-facing dashboard in the app navigation
 - Added live metrics derived from saved intentions:
   - Total intentions count
   - Top keywords (with a "view all" dialog)
   - Keyword drill-down (shows intentions containing the selected keyword)
   - Weather distribution
   - Intentions per day
 - Added habit/pattern detection:
   - Detects recurring intention keywords by day/time/weather context
   - Surfaces "Habit Insights" cards for repeated patterns
 - Added smarter intention suggestions in Daily Content:
   - Suggestions are ranked from detected patterns and current context (day/time/weather)
   - Falls back to top recurring keywords when context-specific matches are not available
 - Added analytics tracking plumbing:
   - Intention submission events are tracked through `AnalyticsRepository`
   - Current implementation uses a fake analytics repository for logging/stub behavior

## ITR2 Highlights

### Database
 - Implemented a database to serve as persistent storage for the project
 - Database was implemented with "Room" persitence library
 - Database now works with the following: daily quotes, images of the day, RSS feeds
 - There is no additional setup needed for the database
  - Room uses an embedded SQLite database stored in the app's data directory
 - You can switch between the database and the stub using the "DatabaseConfig.kt" file
  - Change the value of "DatabaseConfig.USE_REAL_DATABASE" to "false" if you want to use the stub
  - If you want to use the real database set it to "true"

### RSS Feed
 - Implemented the RSS feed feature
 - A user can subscribe to an RSS feed (the link is then saved in the database)
 - User can unsubscribe from the RSS feed
 - User can see RSS content
 - User can select RSS items to have the content open up

## Major Source Code Files

### Presentation
 - Aggregator.kt
 	 - The frontend for the aggregator
 - AnalyticsDashboard.kt
	 - Frontend for analytics
	 - Displays summary stats, top/all keywords, weather distribution, intentions-per-day, and habit insight cards
	 - Includes dialogs for all keywords and keyword-specific intention drill-down
 - DailyContent.kt
 	 - The frontend for the daily content
 	 - Has a spot to display the quote of the day, image of the day, and the intentions feature
 	 - Intentions feature contains a textbox to input the intention, and a submit button
 - MainActivity.kt
 	 - The main frontend file
 	 - Operates effectively as a homepage
	 - Hosts app navigation between Daily Content, Dashboard, Aggregator, and RSS Feed
 - MotivationalWidget.kt
	 - Android home-screen widget implementation (Glance)
	 - Shows quote of the day and today's intentions
	 - Provides refresh action and deep-link into the app
 - RssFeed.kt
 	 - The frontend for the RSS feed
	 - Has a textbox for the user to input a link to an RSS feed they want to subscribe to
	 - Displays the RSS feeds the user is subscribed to in a horizontal list that the user can scroll horizontally through
 - WeatherScreen.kt
	 - The card to display the weather

### Business Logic
 - Analytics.kt
	 - Core analytics domain logic
	 - Extracts keywords, groups weather values, detects recurring patterns, and generates contextual intention suggestions
 - AnalyticsViewModel.kt
	 - Backend for analytics dashboard state
	 - Computes and exposes aggregate analytics from saved intentions
 - AggregatorViewModel.kt
 	 - The backend for the aggregator
 - DailyContentViewModel.kt
 	 - The backend for the daily content
 	 - Hooks up to the ContentRepository.kt
 	 - Retrieves the quote and image from ContentRepository.kt
 	 - Sends a request to save the intentions to DailyContentRepository.kt
	 - Sends analytics tracking events on intention save
	 - Builds contextual intention suggestions using analytics patterns
 - MainViewModel.kt
 	 - The backend for the "Main" activity
 	 - Handles the switching between the various other features (i.e. to Daily Content)
 - RssFeedViewModel.kt
 	 - Handles the backend for the RSS feed
 	 - Has hard-coded feed items, in the future will pull feed items from the database using the ContentRepository.kt
 - WeatherViewModel.kt
	 - Backend for the Weather sub-feature

### Persistence
 - AnalyticsRepository.kt
	 - Analytics event model and analytics repository interface
	 - Includes fake implementation currently used by analytics/domain/viewmodels
 - AggregatorArticle.kt
	 - Data model for scraped aggregator headlines (title + URL)
 - AggregatorWebScraper.kt
	 - Web scraping interface and default implementation for aggregator headline scraping (via Jsoup)
 - NewsHeadlineExtractor.kt
	 - Headline extraction and filtering heuristics used by aggregator scraping
 - ContentRepository.kt
 	 - Interface for daily content; implementations: RoomContentRepository (real DB) and HardcodedContentRepository (stub). Same default content in both.
 - DailyContentRepository.kt
 	 - Room database, DAO, and entities (Intention, QuoteOfTheDay, RssFeedUrl).
 - DatabaseConfig.kt
 	 - Single-line switch USE_REAL_DATABASE to choose real DB vs stub.
 - RssItem.kt
	 - Data class for RSS
 - RssRepository.kt
	 - Database connector and url handler for RSS feature
 - UserPrefs.kt
	 - DataStore-backed preferences helper for lightweight persisted user settings
 - weather/WeatherRepository.kt
	 - Weather repository interface + Open-Meteo implementation
	 - Performs city geocoding + weather lookup and maps weather codes to app conditions
 - weather/GeocodingApi.kt
	 - Retrofit API definition for city-to-coordinate lookup
 - weather/WeatherApi.kt
	 - Retrofit API definition for current weather lookup
 - weather/OpenMeteoResponse.kt
	 - DTOs for Open-Meteo and geocoding API responses
 - weather/WeatherInfo.kt
	 - Domain/data model for normalized weather info used in app features

## Deployment Guide
 - This project is an android project and as such that makes running it a bit different. 
 - Specifically you need to install android studio and then import the project there
	 - The "Src" folder must be imported as the project, otherwise android studio might have a hard time detecting it
	 - Once there you need to hit the green arrow in the top left, or hit "Shift + F10"  - If there are issues with running the project try syncing it first, as there might be an issue with the dependencies
	 - If there are further issues try "cold booting" the android emulator. This can be done by selecting "Device Manager" from the toolbar on the right hand side, clicking the triple-dot menu next to the device you want to run, and then selecting "Cold Boot"