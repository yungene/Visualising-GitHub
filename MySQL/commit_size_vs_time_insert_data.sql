# Sample data for commit size vs time database
# Usage within mysql console:
# 	source commit_size_vs_time_insert_data.sql

INSERT INTO repos_visited
(repo_name, repo_owner, finished, start_time,finish_time)
VALUES
	('example','sample_user', TRUE, '2019-11-11 09:47:34', '2019-11-11 09:49:34');

INSERT INTO commit_size_vs_time 
(repo_name, repo_owner, time_val, commit_size)
VALUES
	('example','sample_user', 100, 4000),
	('example','sample_user', 102, 1000),
	('example','sample_user', 103, 4313),
	('example','sample_user', 105, 1234),
	('example','sample_user', 99, 10),
	('example','sample_user', 95, 10000),
	('example','sample_user', 110, 9999),
	('example','sample_user', 115, 12000)
ON DUPLICATE KEY UPDATE commit_size=VALUES(commit_size);