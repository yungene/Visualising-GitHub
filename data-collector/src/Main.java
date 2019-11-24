import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import org.eclipse.egit.github.core.client.GitHubClient;

public class Main {
	// static PreparedStatement isRepoProcessed;
	// static PreparedStatement processRepo;
	// static PreparedStatement insertRepo;
	static String processRepoSQL = "UPDATE repos_visited " + "SET finished= ? ," + "start_time = ? ,"
			+ "finish_time = ?" + "WHERE repo_name= ? AND repo_owner= ?;";
	static String isRepoProcessedSQL = "SELECT finished FROM repos_visited " + "WHERE repo_name= ? AND repo_owner= ?;";
	static String insertRepoSQL = "INSERT INTO repos_visited" + " VALUES(?,?,FALSE,NULL,NULL);";

	public static void main(String[] args) {
		Calendar calendar = Calendar.getInstance();
		GitHubClient client = new GitHubClient().setOAuth2Token(readToken());
		// Connection conn = null;

		try (Connection conn = MySQLJDBCUtil.getConnection();
				PreparedStatement isRepoProcessed = conn.prepareStatement(isRepoProcessedSQL);
				PreparedStatement processRepo = conn.prepareStatement(processRepoSQL);
				PreparedStatement insertRepo = conn.prepareStatement(insertRepoSQL);
				AutoCloseable finish = conn::rollback) {

			conn.setAutoCommit(false);
			// do work
			String[][] repos = getRepos(conn);
			for (String[] repo : repos) {
				System.out.printf("Starting to process repo:%s by user:%s\n", repo[0], repo[1]);
				if (!processedRepo(conn, repo[0], repo[1], isRepoProcessed, insertRepo)) {
					DataCollector collector = new DataCollector(conn, client, repo[0], repo[1]);
					java.util.Date start = calendar.getTime();
					if (collector.process()) {
						markAsProcessed(conn, repo[0], repo[1], start, calendar.getTime(), processRepo);
					} else {
						System.err.printf("Failed to process repo:%s by user:%s\n", repo[0], repo[1]);
					}
				}
			}
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// TODO: implement proper function. It is dummy for now
	private static String[][] getRepos(Connection conn) {
		String[][] result = new String[1][2];
		result[0][1] = "yungene";
		result[0][0] = "CS3012-SWENG";
		return result;
	}

	private static boolean processedRepo(Connection conn, String repo, String repoOwner,
			PreparedStatement isRepoProcessed, PreparedStatement insertRepo) throws SQLException {
		isRepoProcessed.setString(1, repo);
		isRepoProcessed.setString(2, repoOwner);
		ResultSet rs = isRepoProcessed.executeQuery();
		System.out.printf("Search of repos_visited repo:%s by user:%s\n", repo, repoOwner);
		// loop through the result set
		int count = 0;
		while (rs.next()) {
			count++;
			if (rs.getBoolean("finished")) {
				return true;
			}
		}
		// Check if repo is in the DB, if not insert a default row
		if (count == 0) {
			insertRepo.setString(1, repo);
			insertRepo.setString(2, repoOwner);
			insertRepo.executeUpdate();
			System.out.printf("Insert into repos_visited repo:%s by user:%s\n", repo, repoOwner);
			conn.commit();
		}
		return false;
	}

	private static void markAsProcessed(Connection conn, String repo, String repoOwner, java.util.Date start,
			java.util.Date finish, PreparedStatement processRepo) throws SQLException {
		processRepo.setBoolean(1, true);
		processRepo.setDate(2, new java.sql.Date(start.getTime()));
		processRepo.setDate(3, new java.sql.Date(finish.getTime()));
		processRepo.setString(4, repo);
		processRepo.setString(5, repoOwner);
		int rs = processRepo.executeUpdate();
		System.out.printf("Update of repos_visited repo:%s by user:%s\n", repo, repoOwner);
	}

	static private String readToken() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("config/config.txt")));
			String token = br.readLine();
			return token;
		} catch (IOException e) {
			// System.err.printf("Not able to open and/or read OAuth token from
			// ../config/config.txt. Using empty token instead.");
			e.printStackTrace();
			return "";
		}
	}

}
