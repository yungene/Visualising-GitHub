# Design Document for Visualising GitHub project

Author: Jevgenijus Cistiakovas

Last edit date: 2019-11-26

## Task

Task: _Interrogate the GitHub API to build visualisation of data available that elucidates some aspect of the software engineering process, such as a social graph of developers and projects, or a visualisation of individual of team performance._


## System Architecture Overview
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
 - Store the state of all the processed repositories in a DB. Consider each repository atomically, i.e. either process it fully and successfully, and commit to the DB, or abort and mark it as not yet processed.
 - Use JavaScript and D3.js library to visualize the data. Host a simple webpage with visualization. Ideally, webpage can run on partial data available in the database. Perhaps also allow webpage to run on sample data from a csv file, such that to allow for demonstration withour prior collection.

## Solution
I decided to focus on analysing repositories. Within a repository, time frames between releases are considered. Different metrics related to each release and pre-release development period are calculated. The main focus is to consider the size of an active development team versus time between the start and end of release period( commit frequency with respect to time, commit size with respect to size). Presenting these metrics and comparing them between different repositories should show if there is common pattern. Of interest is whether the team size drops after a release  ~~( whether commit frequency is high straight after a release ( perhaps to fix the bugs) and is high before the release ( to fix all the bugs before the release), and at what stage are the biggest changes committed.)~~

Data to present (DRAFT) :
 - 2d graph - Active team size vs time, with each release being explicitly shows on a time axis. 
 -  ~~2d graph - Commit frequency vs time.~~
 -  ~~2d graph - Commit size vs time.~~
Note: Each release is being explicitly shows on a time axis.

The metrics required:
- Release information - https://developer.github.com/v3/repos/releases/ . In particular, commit marking the release.
-  ~~Commit size - https://developer.github.com/v3/repos/statistics/#get-contributors-list-with-additions-deletions-and-commit-counts. https://developer.github.com/v3/repos/contents/#get-contents~~
-  ~~Commit frequency. https://developer.github.com/v3/repos/statistics/#get-the-number-of-commits-per-hour-in-each-day~~

### Active team size
Active team size over some period T is defined as the number of users that have contributed (e.g. committed) at least N times over that period T. The only type of contribution considered initially will be commits. Comments and other contributions can be added with adjusting coefficients. As discussed, time frames between releases are considered. 
Define active team size as follows:
- An active team size at a day D is the number of people that have contibuted more than N times over the period T starting at D-T and ending at day D. To allow for comparison between repositories, choose T and N to be same for every repository. E.g. choose T = a week, N = 3 commits.
Algorithmically, keeping the last window of T days can be achieved by using a queue to allow for O(1) insert and removal. Given the number of contributors and number of commits is realtively low, ~O(10^3), calculation of team size can be done dynamically for each commit (as well as for each day). It is more likely to hit API rate limit than become affected by runtime complexity. A sort of queue and dictionary table can be used to keep track of all the updates + all the values. A balanced tree can be used to keep a sorted queue of contributors to allow for efficient calculation of a team size.
- An information for each date considered in stored in a DB, represeting a single point on a time axis. This allows for webpage to easily extract the necessary values from a database without any complex processing.

The data collected by the data collector is stored in the MySQL database. In particular for each day for a specific configuration processed the value of the active team size is stored. Similarly, the release names and dates of releases are stored in the MySQL isntance. This data is accessed and used by the Node.js backend which queries the database to retrive the points and sends the to the the frontend, which uses D3.js to create an interactive graph. Initially, all the processing was done at the backend and Node.js was serving static pages with the graph already preprocessed. While this makes the webpage very light for the end user, I decided to move the D3 code to frontend in order to allow for interactivity as it is an essential part of experience with data visualisation.
