# Sample usage:
# mysql -u root -p < init.sql

# An SQL script to initialise the database
CREATE DATABASE IF NOT EXISTS visualGithub;
USE visualGithub;

# Create a table to store the graph points
# TODO: consider using DATETIME or WEEK + YEAR for time axis
CREATE TABLE IF NOT EXISTS commit_size_vs_time (
	repo_name 			VARCHAR(255) NOT NULL,
	repo_owner 			VARCHAR(255) NOT NULL,
	time_val			INT UNSIGNED NOT NULL,
	commit_size	 		INT UNSIGNED NOT NULL,
	PRIMARY KEY (repo_name,repo_owner,time_val)
);

# Create a table to store the repositories traversed
CREATE TABLE IF NOT EXISTS repos_visited (
	repo_name 			VARCHAR(255) NOT NULL,
	repo_owner 			VARCHAR(255) NOT NULL,
	finished 			BOOLEAN NOT NULL DEFAULT FALSE,
	start_time 			DATETIME,
	finish_time 		DATETIME,
	PRIMARY KEY (repo_name,repo_owner)
);

