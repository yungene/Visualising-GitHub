import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;

import org.eclipse.egit.github.core.client.GitHubClient;
/*
 * @author Jevgenijus Cistiakovas cistiakj@tcd.ie
 * This is a mainline file. It reads in the GitHub repositories and configurations to be processed, and then
 * uses DataCollector to process each repository.
 */
public class Main {
	static String processRepoSQL = "UPDATE repos_visited " + "SET finished= ? ," + "start_time = ? ,"
			+ "finish_time = ?" + "WHERE repo_name= ? AND repo_owner= ? AND time_delta= ? AND threshold = ?;";
	static String isRepoProcessedSQL = "SELECT finished FROM repos_visited "
			+ "WHERE repo_name= ? AND repo_owner= ? AND time_delta= ? AND threshold = ?;";
	static String insertRepoSQL = "INSERT INTO repos_visited" + " VALUES(?,?,FALSE,NULL,NULL,?,?);";

	/*
	 * A simple class representing a configuration to be processed.
	 */
	static class Repo {
		String name;
		String owner;
		int backfill;
		int threshold;

		public Repo(String name, String owner, int backfill, int threshold) {
			super();
			this.name = name;
			this.owner = owner;
			this.backfill = backfill;
			this.threshold = threshold;
		}
	}

	// TODO(cistiakj): process the whole repo simultaneously for different parameters to limit the number of calls to GItHub API
	public static void main(String[] args) {
		Calendar calendar = Calendar.getInstance();
		GitHubClient client = new GitHubClient().setOAuth2Token(readToken());
		try (Connection conn = MySQLJDBCUtil.getConnection();
				PreparedStatement isRepoProcessed = conn.prepareStatement(isRepoProcessedSQL);
				PreparedStatement processRepo = conn.prepareStatement(processRepoSQL);
				PreparedStatement insertRepo = conn.prepareStatement(insertRepoSQL);
				AutoCloseable finish = conn::rollback) {

			conn.setAutoCommit(false);
			// do work
			ArrayList<Repo> repos = getRepos(conn, 10000);
			for (Repo repo : repos) {
				System.out.printf("Starting to process repo:%s by user:%s\n", repo.name, repo.owner);
				if (!processedRepo(conn, repo.name, repo.owner, isRepoProcessed, insertRepo, repo.backfill,
						repo.threshold)) {
					DataCollector collector = new DataCollector(conn, client, repo.name, repo.owner, repo.backfill,
							repo.threshold);
					java.util.Date start = calendar.getTime();
					if (collector.process()) {
						markAsProcessed(conn, repo.name, repo.owner, start, calendar.getTime(), processRepo,
								repo.backfill, repo.threshold);
					} else {
						System.err.printf("Failed to process repo:%s by user:%s\n", repo.name, repo.owner);
					}
				}
			}
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Retrieves the list of repositories to be processed which are less than the maxSize parameter.
	 * Repositories are read from the MySQL table. See schema for further information.
	 */
	private static ArrayList<Repo> getRepos(Connection conn, int maxSize) throws SQLException {
		ArrayList<Repo> res = new ArrayList<>();
		try (Statement getRepo = conn.createStatement();) {
			String sql = String.format(
					"SELECT repo_name,repo_owner,time_delta,threshold,estimated_size FROM repos_store WHERE estimated_size < %d;",
					maxSize);
			ResultSet rs = getRepo.executeQuery(sql);
			while (rs.next()) {
				int estimatedSize = rs.getInt("estimated_size");
				if (estimatedSize < maxSize) {
					Repo repo = new Repo(rs.getString("repo_name"), rs.getString("repo_owner"), rs.getInt("time_delta"),
							rs.getInt("threshold"));
					res.add(repo);
				}
			}
		}
		return res;
	}

	/*
	 * Returns whether a particular repository configuration has been marked as processed.
	 * If no entry found, then a inserts a new entry to the table.
	 */
	private static boolean processedRepo(Connection conn, String repo, String repoOwner,
			PreparedStatement isRepoProcessed, PreparedStatement insertRepo, int timeInterval, int threshold)
			throws SQLException {
		isRepoProcessed.setString(1, repo);
		isRepoProcessed.setString(2, repoOwner);
		isRepoProcessed.setInt(3, timeInterval);
		isRepoProcessed.setInt(4, threshold);
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
			insertRepo.setInt(3, timeInterval);
			insertRepo.setInt(4, threshold);
			insertRepo.executeUpdate();
			System.out.printf("Insert into repos_visited repo:%s by user:%s\n", repo, repoOwner);
			conn.commit();
		}
		return false;
	}

	/*
	 * Update the entry in the table to mark the repository configuration as processed.
	 * Use together with processedRepo() to ensure that the repository configuration is processed twice,
	 * and that an entry in the table exists.
	 */
	private static void markAsProcessed(Connection conn, String repo, String repoOwner, java.util.Date start,
			java.util.Date finish, PreparedStatement processRepo, int timeInterval, int threshold) throws SQLException {
		processRepo.setBoolean(1, true);
		processRepo.setDate(2, new java.sql.Date(start.getTime()));
		processRepo.setDate(3, new java.sql.Date(finish.getTime()));
		processRepo.setString(4, repo);
		processRepo.setString(5, repoOwner);
		processRepo.setInt(6, timeInterval);
		processRepo.setInt(7, threshold);
		int rs = processRepo.executeUpdate();
		System.out.printf("Update of repos_visited repo:%s by user:%s\n", repo, repoOwner);
	}

	/*
	 * Read OAuth 2 GitHub token from the predetermined location.
	 * If fails, then return empty string.
	 */
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
