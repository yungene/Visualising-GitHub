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

### Active team size
Active team size over some period T is defined as the number of users that have contributed (e.g. committed) at least N times over that period T. The only type of contribution considered initially will be commits. Comments and other contributions can be added with adjusting coefficients. As discussed, time frames between releases are considered. 
Define active team size as follows:
- An active team size at a day D is the number of people that have contibuted more than N times over the period T starting at D-T and ending at day D. To allow for comparison between repositories, choose T and N to be same for every repository. Choose T = a week, N = 3 commits.
Algorithmically, keeping the last window of T days can be achieved by using a queue to allow for O(1) insert and removal. Given the number of contributors and number of commits is realtively low, ~O(10^3), calculation of team size can be done dynamically for each commit (as well as for each day). A sort of queue and dictionary table can be used to keep track of all the updates + all the values. A balanced tree can be used to keep a sorted queue of contributors to allow for efficient calculation of a team size.
