# Sample usage:
# mysql -u root -p < init.sql

# An SQL script to initialise the database

# drop database visualGithub;
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

CREATE TABLE IF NOT EXISTS repos_store (
	repo_name 			VARCHAR(255) NOT NULL,
	repo_owner 			VARCHAR(255) NOT NULL,
	time_delta 			INT NOT NULL,
	threshold			INT NOT NULL,
	estimated_size		INT NOT NULL DEFAULT 0,
	PRIMARY KEY (repo_name,repo_owner,time_delta,threshold)
);

# Create a table to store the repositories traversed
CREATE TABLE IF NOT EXISTS repos_visited (
	repo_name 			VARCHAR(255) NOT NULL,
	repo_owner 			VARCHAR(255) NOT NULL,
	finished 			BOOLEAN NOT NULL DEFAULT FALSE,
	start_time 			DATETIME,
	finish_time 		DATETIME,
	time_delta			INT NOT NULL,
	threshold			INT NOT NULL,
	PRIMARY KEY (repo_name,repo_owner,time_delta,threshold)
);

CREATE TABLE IF NOT EXISTS active_team_size_vs_time (
	repo_name 			VARCHAR(255) NOT NULL,
	repo_owner 			VARCHAR(255) NOT NULL,
	date 				DATE 		 NOT NULL,
	team_size 			INT UNSIGNED NOT NULL,
	time_delta			INT 		 NOT NULL,
	threshold			INT NOT NULL,
	PRIMARY KEY (repo_name, repo_owner, date, time_delta, threshold)
);

CREATE TABLE IF NOT EXISTS release_table (
	repo_name 			VARCHAR(255) NOT NULL,
	repo_owner 			VARCHAR(255) NOT NULL,
	date 				DATE 		 NOT NULL,
	release_name		VARCHAR(255) NOT NULL,
	PRIMARY KEY (repo_name, repo_owner, date, release_name)
);

# Tables for the graph portion
CREATE TABLE IF NOT EXISTS nodes (
	node_id				INT UNSIGNED NOT NULL,
	user_name			VARCHAR(255) NOT NULL,
	followers			INT UNSIGNED NOT NULL,
	following			INT UNSIGNED NOT NULL,
	PRIMARY KEY (node_id)
);

CREATE TABLE IF NOT EXISTS edges (
	out_node_id			INT UNSIGNED NOT NULL,
	in_node_id			INT UNSIGNED NOT NULL,
	weight				INT NOT NULL DEFAULT 1,
	PRIMARY KEY (out_node_id,in_node_id)
);
