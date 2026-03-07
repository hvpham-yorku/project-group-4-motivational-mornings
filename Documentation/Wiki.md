# EECS 2311 Project Group 4: Motivational Mornings

## Major Source Code Files

### Frontend
 - Aggregator.kt
 	 - The frontend for the aggregator
 - DailyContent.kt
 	 - The frontend for the daily content.
 	 - Has a spot to display the quote of the day, image of the day, and the intentions feature
 	 - Intentions feature contains a textbox to input the intention, and a submit button
 - MainActivity.kt
 	 - The main frontend file
 	 - Operates effectively as a homepage
 - RssFeed.kt
 	 - The frontend for the RSS feed
 	 - Currently has some dummy RSS items to demonstrate how the feed would work

### Backend
 - AggregatorViewModel.kt
 	 - The backend for the aggregator
 - DailyContentViewModel.kt
 	 - The backend for the daily content
 	 - Hooks up to the ContentRepository.kt (which is currently just a dummy database that has hard-coded values)
 	 - Retrieves the quote and image from ContentRepository.kt
 	 - Sends a request to save the intentions to ContentRepository.kt and saves the intention to the program memory (in the future it won't save to memory, it will just save to a database through ContentRepository.kt)
 - MainViewModel.kt
 	 - The backend for the "Main" activity
 	 - Handles the switching between the various other features (i.e. to Daily Content)
 - RssFeedViewModel.kt
 	 - Handles the backend for the RSS feed
 	 - Has hard-coded feed items, in the future will pull feed items from the database using the ContentRepository.kt

### Database
 - ContentRepository.kt
 	 - Handles the connection to the database
 	 - In the future other classes will request access to the database through ContentRepository.kt
 	 - Currently it has some methods that just call hard-coded data for other classes

## Deployment Guide
 - This project is an android project and as such that makes running it a bit different. 
 - Specifically you need to install android studio and then import the project there
  - The "Src" folder must be imported as the project, otherwise android studio might have a hard time detecting it
  - Once there you need to hit the green arrow in the top left, or hit "Shift + F10"
  - If there are issues with running the project try syncing it first, as there might be an issue with the dependencies
  - If there are further issues try "cold booting" the android emulator. This can be done by selecting "Device Manager" from the toolbar on the right hand side, clicking the triple-dot menu next to the device you want to run, and then selecting "Cold Boot"