# Motivational Mornings Project Log

This log summarizes the team’s weekly meetings, major decisions, and task assignments for the development of **Motivational Mornings**. It is intended to document progress for Iteration 2 and Iteration 3, including planning discussions, feature assignment, testing, documentation work, and release preparation.

## Monday, January 12
- Held the team’s initial project meeting.
- Brainstormed possible project ideas and discussed the type of user problem the team wanted to solve.
- Agreed that each team member would come to the next meeting with additional ideas and possible feature suggestions.

## Monday, January 19
- Reviewed the project ideas brought forward by the team.
- Selected Jordi’s proposal because it already had a client connection and a clearer target audience.
- Discussed the general direction of the application and began identifying possible core features.
- Assigned all team members to think further about features that would strengthen the initial concept.

## Monday, January 26
- Clarified the project’s scope and discussed what would realistically fit within the course timeline.
- Reviewed open questions about the client’s needs and the intended user experience.
- **Task assignment:**
  - **Jordi:** meet with the client and collect additional requirement details.

## Monday, February 2
- Reviewed the information gathered from the client meeting.
- Decided to use **Android Studio** for development.
- Reviewed the first draft of the Jira board and discussed how stories should be divided by iteration.
- Confirmed several important project features after the client discussion, including **intentions**, clarification of the **RSS feed**, and the addition of the **aggregator** feature.
- **Task assignment:**
  - **Jordi:** translate the client discussion into the required Iteration 0 planning documents.
  - **Everyone:** set up the Android Studio project locally and confirm the environment was working.

## Monday, February 9
- Finalized the initial Jira structure and discussed the remaining setup tasks.
- Reviewed the expected work for the first development cycle and how the daily content area would be divided.
- **Task assignment:**
  - **Jordi:** implement **Image of the Day** and **Intentions**.
  - **Arsh:** implement **Weather**.
  - **Hassan:** prepare drafts for the major components and support the overall structure of the application.

## Monday, February 23
- Began planning for **Sprint 2 / ITR2**.
- Reviewed the Iteration 2 user stories from the planning document and mapped them to implementation tasks.
- Agreed that the main Sprint 2 focus would be the **RSS feed features** and **database-backed daily content**.
- **ITR2 task assignment:**
  - **Jordi:** implement subscribing to RSS feeds and backend support for daily content.
  - **Arsh:** implement viewing RSS feed content.
  - **Hassan:** implement unsubscribing from RSS feeds and viewing the list of subscribed feeds.

## Monday, March 2
- Reviewed Sprint 2 progress.
- Estimated that the team was roughly one-third of the way through the Sprint 2 features.
- Discussed technical issues encountered so far and the remaining work required to complete RSS and database-related functionality.
- Agreed to continue implementation during the week and use the next meeting to identify any missing requirements before the extension period.

## Monday, March 9
- Discussed what was still missing from Iteration 2 and what could be completed during the extension period.
- Reviewed testing and documentation requirements in addition to feature work.
- Confirmed that the extension would be used to stabilize the ITR2 features and address incomplete items.
- **ITR2 extension task assignment:**
  - **Jordi:** update documentation, review unit tests, create integration tests, and make minor fixes to completed features.
  - **Arsh:** restore the weather feature after it had been accidentally removed during development.
  - **Hassan:** add database support for **Image of the Day** and make RSS items clickable that redirect the user to the actual page.

## Monday, March 16
- Began planning for **Sprint 3 / ITR3**.
- Reviewed what had been completed in Sprint 2 and identified the remaining high-priority features for the final iteration.
- Matched the new tasks to the Iteration 3 planning updates and Jira board.
- **ITR3 task assignment:**
  - **Jordi:** implement **cross-feature analytics**, the **home screen widget**, **input website into aggregator**, and **reflections for intentions**.
  - **Arsh:** implement **user-defined information**, **keyword search for the aggregator**, and **notifications**.
  - **Hassan:** implement **like/dislike for daily content**, **display content when first opening the aggregator tab**, and **stock tracking in the aggregator**.

## Monday, March 23
- Reviewed progress across the Sprint 3 features.
- Discussed the remaining unit tests, integration tests, and the division of final testing work.
- Reviewed documentation tasks required for the release submission.
- Discussed the final preparation steps for the app release and Delivery 2 presentation.
- Also discussed Lab 5 scheduling so that project work could be coordinated around other course deliverables.

## Saturday, March 28
- Conducted a final review of the Sprint 3 Jira board before release.
- Confirmed that the implemented Sprint 3 work included the following stories or tasks:
  - refining the display content when first opening the tab,
  - option to receive notifications for selected content,
  - stock tracking,
  - user-defined information,
  - cross-feature analytics,
  - app widget to view the quote of the day and intentions,
  - like and dislike for daily content - a database that would store feedback and use the data to determine frequency of quotes and images,
  - input website into the aggregator,
  - reflections for intentions,
  - integration tests,
  - clickable RSS items which link to the actual site,
  - backend support for weather using api,
  - backend support for image of the day using a Room Database,
  - keyword search for the aggregator.
- Confirmed that only minor finishing work remained on the story related to displaying content upon first opening the tab.
- Agreed to use the final day before submission for cleanup, validation, and demo preparation.

## Sunday, March 29
- Performed a final documentation and release check before Delivery 2 submission.
- Reviewed the log file, planning materials, and GitHub contents to make sure the repository reflected the final state of the project.
- Confirmed that Jira assignments, testing work, and implemented features were consistent with the planned Iteration 2 and Iteration 3 stories.
- Met with customer and demonstrated the features that were proposed and verified the requirements, as well as took client feedback.
- Prepared for the live demo by reviewing feature ownership and making sure each team member could present their assigned work.
- Reviewed the GitHub repositories to check for code smells and perform refactorig where needed.

## Summary of Main Task Ownership

### Iteration 2
- **Jordi:** RSS subscription, daily content backend support, documentation adjustments, unit/integration test review.
- **Arsh:** viewing RSS feed content, weather restoration.
- **Hassan:** RSS unsubscribe and subscribed-feed list, image-of-the-day database support, clickable RSS items.

### Iteration 3
- **Jordi:** cross-feature analytics, widget, aggregator website input, reflections for intentions.
- **Arsh:** user-defined information, keyword search in the aggregator, notifications.
- **Hassan:** like/dislike for daily content, automatic display of aggregator content on tab open, stock tracking.

## Note
- Detailed task tracking and completion status were also maintained on Jira throughout Iteration 2 and Iteration 3.
- Jira snapshots were used alongside this log to verify task assignment and feature completion.
