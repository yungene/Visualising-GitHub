use visualGithub;
#INSERT IGNORE INTO release_table VALUES('CS301ddd2-SWENG','yungene','2019-11-07','v4.0');
select * FROM active_team_size_vs_time a ORDER BY a.date;

select * FROM release_table;

select * FROM repos_visited;

#delete from active_team_size_vs_time t WHERE t.repo_name = t.repo_name;
#drop table active_team_size_vs_time;
update repos_visited
set finished = False
where repo_owner = "yungene" AND repo_name = "CS3012-SWENG";