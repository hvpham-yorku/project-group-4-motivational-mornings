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

### Tests

