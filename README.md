# Visualising-GitHub
CS3012 - "Social Graph"
## Part 1 - GitHub Access

Commit which identifies the solution to this part: bf944d30d144e70019e2a4b3d102234f17ec43b6

A little program under _/Github-acess_ demonstrates the use of Eclipse EGit Github library for Java to retrieve and display data about a Github's user repositories and the number of "watchers" for these public repositories. In this case, data about myself is retrieved and displayed. More details can be found in README in the directory of the program.

The program is built using Apache Maven with a POM included in the directory. The program was built and ran using Eclipse Photon with Maven M2E plugin.

## Part 2 - Visualising GitHub

Important links:

* [Design Doc](/DesignDoc.md)
* DEMO - [Sample static html page with prefetched data for one of the repositories](https://yungene.github.io/) OR see in /static-demo-page/index.html
* Commit which identifies the current solution: [d7498db1876e7f99115340ed7474bc03d414db8c](https://github.com/yungene/Visualising-GitHub/commit/d7498db1876e7f99115340ed7474bc03d414db8c)
* Solution prior to followers network: [091d951a7fea5497ea5a2701b1a54dc338ea7f97](https://github.com/yungene/Visualising-GitHub/commit/091d951a7fea5497ea5a2701b1a54dc338ea7f97)
* [Users followers graph](https://yungene.github.io/users) OR see static in /static-demo-page/users.html


### Task

Task: _Interrogate the GitHub API to build visualisation of data available that elucidates some aspect of the software engineering process, such as a social graph of developers and projects, or a visualisation of individual of team performance._

### Project Information 
_Note: Please see Design Doc for full description of the project and its development._

The project visualises information about repositories on GitHub. On the main page, the only metric presented is a line (area) chart of active development team size vs time with releases (tags) also shown on a time axis. (See below for additional network graph). This visualisation indents to investigate whether there is correlation between active team size and time with respect to releases. Initial conjecture was that active team size will increase prior to release and will peak at the time of release and will decreases straight after the release. The project consists of three logical parts: data collector, database and website. Website extracts all the processed repositories from the database and presents it to user by means of a drop down menu. Data for the chosen repository will be read from the database and presented to the user as an *interactive* graph. By hovering over the graph, the user can see the exact coordinates of each point and also see the release that this point is working towards. The user can zoom in by brushing on the graph. That is click and drag, and release to zoom in, inversely proportional to the area covered by the movement. Double click to reset the scaling.

#### Usage
Use select box to select the desired reposiroty to be displayed. List is generated based on the data in the database. List names follow this format "repo_owner,repo_name,days_backfill,threshold". See the headings below and title of the graph for the parameters of the current graph. 
This is a line (area) chart. It has three main components. Y-axis displays the active team size as measured using the metric described in the design doc. X-axis displays the time/date. Thus a change of team size with the respect to time is displayed. Red vertical lines represent individual releases (tags) in that repository. Tags are bound to the time values.
This graph is *interactive*. Hover over a point to see the exact value as well as the name of the release this commit is conributing to. Name of the release is displayed above the release line. Additionally, brush on the graph to zoom in. Double click to reset the scaling. That is click and drag, and release to zoom in, inversely proportional to the area covered by the movement.

[Demo page.](https://yungene.github.io/) (With sample prefetched data. Original webpage includes a backend and a database. Should be online and up to date.)

Pattern that similar to that described by the conjecture:
![Pattern that similar tot he conjecture.](/images/conjecture-pattern.png "Pattern that similar tot he conjecture.")

Demonstration of the webpage, including select box, zoom in and cursor:
![Demonstration of the webpage, including select box, zoom in and cursor.](/images/demo_select_box_cursor_zoom_in.gif "Demonstration of the webpage, including select box, zoom in and cursor.")

### Followers graph
As an addition to the main metric described above, I also created a second page /users which presents a followers/followings graph. That is it builds a directed graph with nodes for users and edges for "is followed by" and "is following" relations. It is a Force Directed Graph with Labels. TI created a separated crawler for this. The crwaler does a simple BFS starting from my account.

[Demo page.](https://yungene.github.io/users) (With sample prefetched data. Original webpage includes a backend and a database. Should be online and up to date.)

Screenshot
![Followers network.](/images/followers-graph.png "Followers network.")

Demonstration of the webpage for graph:
![Demonstration of the webpage for graph.](/images/followers-network.gif "Demonstration of the webpage for graph.")


### Technologies used

- [Java 8](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [The GitHub Java API (org.eclipse.egit.github.core) library](https://github.com/eclipse/egit-github/tree/master/org.eclipse.egit.github.core)
- [Node.js](https://nodejs.org/en/)
- [Express.js](https://expressjs.com/)
- [D3.js](https://d3js.org/)
- [MySQL](https://www.mysql.com/)

### Project Setup
The project consists of 3 main parts:

- MySQL local DB
- Data collector in Java
- Local Node.js webserver

At present in order to run the project all three components are required. 
TODO: allow to run a website without a database using some sample data.

#### MySQL database

- Create the DB and tables using the script under [/MySQL/init.sql](/MySQL/init.sql). Use [/MySQL/insert_sample_repos.sql](/MySQL/insert_sample_repos.sql) to insert a list of sample repos to the DB. THis list will be used by the data collector.
- Create the database configs using your credentials for your local MySQL instance. Under [_/data-collector/_](/data-collector) create a _config_ folder. Under _/data\_collector/config/_ create a _dbconfig.properties_ file. Insert and modify the template: 
```
 host=localhost
 user=root
 password=****
 db=dbName
 port=3306
```
- Under [_/visual/_](/visual) create a _config_ folder. In this folder create a _config.js_ file. Indert and modify the template:
```
var config = {
development: {
    //url to be used in link generation
    url: 'http://my.site.com',
    //mysql connection settings
    database: {
        host: 'localhost',
        user: 'root',
        password: '****',
        db:     'dbName',
        port: "3306"
    },
    //server details
    server: {
        host: '127.0.0.1',
        port: '3422'
    }
}};
module.exports = config;
```

#### Data Collector

- Ensure that Java 8 is installed.
- Ensure that Maven is installed. Maven 3.5.3 was used. More specifically, it was tested to work with M2E v1.9.0.
- Build and install the Maven dependecies listed in [_data-collector/pom.xml_](/data-collector/pom.xml). This includes org.eclipse.egit.github.core library and MySQL Java connector.
- Ensure that MySQL Connector/J for Java is installed. It was tested to work with Connector/J 8.0.18. It should be installed by Maven. Otherwise install manually and link with the project.
- To ensure that the collector is less likely to hit a rate limit, provide an OAuth 2 GitHub token. Under [_/data-collector/_](/data-collector) create a _config_ folder. Under _/data\_collector/config/_ create a _config.txt_ file. Insert and modify the template: 
```
*token*

```
- Run the Main.java file. See getRepos() function to see how the repositories to be porcessed are supplied. For graph, run GraphMain.

#### Website
##### Install:

```
npm install
```

##### Start webserver:

```
npm start
```

##### Access local webpage

```
http://localhost:3000
http://localhost:3000/users
```

