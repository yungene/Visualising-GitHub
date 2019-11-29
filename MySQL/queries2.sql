use visualGithub;
#INSERT IGNORE INTO release_table VALUES('CS301ddd2-SWENG','yungene','2019-11-07','v4.0');
select * FROM active_team_size_vs_time a ORDER BY a.date;

select * from repos_store;

select * FROM release_table;

select * FROM repos_visited;

SELECT date, team_size FROM active_team_size_vs_time WHERE repo_name='CS3012-SWENG' AND repo_owner='yungene' ORDER BY date ASC;

SELECT date, team_size,time_delta,threshold  FROM active_team_size_vs_time WHERE repo_name='coc.nvim' AND repo_owner='neoclide' AND time_delta=30 AND threshold=3 ORDER BY date ASC;


#delete from active_team_size_vs_time t WHERE t.repo_name = t.repo_name;
drop table active_team_size_vs_time;
drop table repos_visited;
CREATE TABLE IF NOT EXISTS active_team_size_vs_time (
	repo_name 			VARCHAR(255) NOT NULL,
	repo_owner 			VARCHAR(255) NOT NULL,
	date 				DATE 		 NOT NULL,
	team_size 			INT UNSIGNED NOT NULL,
	time_delta			INT 		 NOT NULL,
	PRIMARY KEY (repo_name, repo_owner, date)
);
drop table release_table;
# Create a table to store the repositories traversed
CREATE TABLE IF NOT EXISTS release_table (
	repo_name 			VARCHAR(255) NOT NULL,
	repo_owner 			VARCHAR(255) NOT NULL,
	date 				DATE 		 NOT NULL,
	release_name		VARCHAR(255) NOT NULL,
	PRIMARY KEY (repo_name, repo_owner, date, release_name)
);
update repos_visited
set finished = False
where repo_owner = "google" AND repo_name = "filament";


# select repos visited
SELECT CONCAT(repo_owner,",",repo_name) FROM repos_visited WHERE finished = TRUE;