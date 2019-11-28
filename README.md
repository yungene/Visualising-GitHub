# Visualising-GitHub
CS3012 - "Social Graph"

## Part 2 - Visualising GitHub

Important links:

* [Design Doc](/DesignDoc.md)
* [Sample static html page with prefetched data for one of the repositories](/static-demo-page/index.html)
*


### Task

Task: _Interrogate the GitHub API to build visualisation of data available that elucidates some aspect of the software engineering process, such as a social graph of developers and projects, or a visualisation of individual of team performance._

### Project Information 
_Note: Please see Design Doc for full description of the project and its development._

The project visualises information about repositories on GitHub. At present, the only metric presented is a line (area) chart of active development team size vs time with releases (tags) also shown on a time axis. This visualisation indents to investigate whether there is correlation between active team size and time with respect to releases. Initial conjecture was that active team size will increase prior to release and will peak at the time of release and will decreases straight after the release. The project consists of three logical parts: data collector, database and website. Website extracts all the processed repositories from the database and presents it to user by means of a drop down menu. Data for the chosen repository will be read from the databse and presented to the user as an interactive graph. By hovering over the graph, the user can see the exact coordinates of each point and also see the release that this point is working towards.

TODO: Provide examples, screenshots, GIFs

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

- Create the DB and tables using the script under [/MySQL/init.sql](/MySQL/init.sql).
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
- Run the Main.java file. See getRepos() function to see how the repositories to be porcessed are supplied.

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
```