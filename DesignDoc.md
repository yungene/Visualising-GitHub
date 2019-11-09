# Design Document for Visualising GitHub project

Author: Jevgenijus Cistiakovas
Last edit date: 2019-11-09

## Task

Task: _Interrogate the GitHub API to build visualisation of data available that elucidates some aspect of the software engineering process, such as a social graph of developers and projects, or a visualisation of individual of team performance._


## System Architecture
TODO: Provide a diagram

 - Input Data: 
	 - GitHub API v3
 - Intermediate representation:
	 - Semi or fully processed data from GitHub stored in a persistent database. 
	 - The state of crawler process stored in a  persistent manner.
 - Output:
	 - Present the results in a visual manner such as graphs, tables, charts.

#### Proposed architecture:
 - Collect data from GitHub using Eclipse Egit GitHub Java API library. Do all the necessary processing in Java and output the resulting information into a database (e.g. local MySQL instance)
 - Use JavaScript and D3.js library to visualize the data. Host a simple webpage with visualization. Ideally, webpage can run on partial data available in the database.

## Solution
I decided to focus on analysing repositories. Within a repository, time frames between releases are considered. Different metrics related to each release and pre-release development period are calculated. The main focus is to consider the size of an active development team versus time between the start and end of release period, commit frequency with respect to time, commit size with respect to size. Presenting these metrics and comparing them between different repositories should show if there is common pattern. Of interest is whether the team size drops after a release, whether commit frequency is high straight after a release ( perhaps to fix the bugs) and is high before the release ( to fix all the bugs before the release), and at what stage are the biggest changes committed.

Data to present (DRAFT) :
 - 2d graph - Active team size vs time, with each release being explicitly shows on a time axis. 
 - 2d graph - Commit frequency vs time.
 - 2d graph - Commit size vs time.
Note: Each release is being explicitly shows on a time axis.

The metrics required:
- Release information - https://developer.github.com/v3/repos/releases/ . In particular, commit marking the release.
- Commit size - https://developer.github.com/v3/repos/statistics/#get-contributors-list-with-additions-deletions-and-commit-counts. https://developer.github.com/v3/repos/contents/#get-contents
- Commit frequency. https://developer.github.com/v3/repos/statistics/#get-the-number-of-commits-per-hour-in-each-day
