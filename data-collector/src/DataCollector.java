import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryTag;
import org.eclipse.egit.github.core.TypedResource;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;

// Consider the last 10 releases

/*
 * @author Jevgenijus Cistiakovas cistiakj@tcd.ie
 * This class represents a data collector that gathers information about a particular
 * repository configuration. In particular, it gather information about release tags and its
 * dates as well as active team size vs time. This class implements an exponential backoff to
 * try and eliminate errors due to faulty calls to GitHub API.
 */
public class DataCollector {
	// Input parameters
	Connection conn;
	GitHubClient client;
	String repo;
	String repoOwner;
	// Internal attributes
	Queue<Contributor> contributorsQueue;
	Map<String, Integer> contributorsTable;
	int teamSize;
	// Configurations
	int exponentialBackoffTime = 1;
	int exponentialBackoffMultiplier = 2;
	int exponentialBackoffLimit = (int) Math.pow(exponentialBackoffMultiplier, 12);
	int timeInterval; // days
	int commitThreshold; // commits
	int daysToProcess = 400; // days to process

	// Simple class representing info about a contributor.
	static class Contributor {
		java.util.Date date;
		String login;
		public Contributor(Date date, String login) {
			super();
			this.date = date;
			this.login = login;
		}
	}

	public DataCollector(Connection conn, GitHubClient client, String repo, String repoOwner, int timeInterval, int commitThreshold) throws SQLException {
		super();
		this.conn = conn;
		this.client = client;
		this.repo = repo;
		this.repoOwner = repoOwner;
		this.timeInterval = timeInterval;
		this.commitThreshold = commitThreshold;
		System.out.printf("Created a new DataCollector for repo:%s by user:%s\n", repo, repoOwner);
	}

	/*
	 * Process the repo provided and put all the results into the DB. Use a big
	 * single transaction to ensure atomicity.
	 */
	public boolean process() throws InterruptedException {
		// Start transaction
		RepositoryService repoService = new RepositoryService(client);
		CommitService commitService = new CommitService(client);
		try (PreparedStatement setTag = conn.prepareStatement(String.format(
				"INSERT IGNORE INTO release_table (repo_name,repo_owner,date,release_name) VALUES ('%s','%s',?,?);",
				repo, repoOwner));
				PreparedStatement setTeamSizeVSTime = conn.prepareStatement(
						String.format("INSERT IGNORE INTO active_team_size_vs_time VALUES ('%s','%s',?,?,%d,%d);", repo,
								repoOwner, timeInterval, commitThreshold));
				AutoCloseable finish = conn::rollback;) {

			exponentialBackoffTime = 1;
			while (exponentialBackoffTime < exponentialBackoffLimit) {
				try {
					processTags(repoService, commitService, setTag);
					conn.commit();
					break;
				} catch (SQLException | IOException ex) {
					System.out.printf("Executing tag backoff for repo:%s by user:%s\n", repo, repoOwner);
					Thread.sleep(1000 * exponentialBackoffTime);
					exponentialBackoffTime *= exponentialBackoffMultiplier;
					ex.printStackTrace();
					try {
						if (conn != null)
							conn.rollback();
					} catch (SQLException e) {
						System.err.println(e.getMessage());
					}
				}
			}
			if (exponentialBackoffTime >= exponentialBackoffLimit) {
				return false;
			}
			exponentialBackoffTime = 1;
			while (exponentialBackoffTime < exponentialBackoffLimit) {
				try {
					processCommits(repoService, commitService, setTeamSizeVSTime);
					conn.commit();
					break;
				} catch (SQLException | IOException ex) {
					System.out.printf("Executing commit backoff for repo:%s by user:%s\n", repo, repoOwner);
					Thread.sleep(1000 * exponentialBackoffTime);
					exponentialBackoffTime *= exponentialBackoffMultiplier;
					ex.printStackTrace();
					try {
						if (conn != null)
							conn.rollback();
					} catch (SQLException e) {
						System.err.println(e.getMessage());
					}
				}
			}
			if (exponentialBackoffTime >= exponentialBackoffLimit) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// End transaction
		return true;
	}

	/*
	 * Process release tags of the repository and put them into a DB.
	 */
	private boolean processTags(RepositoryService repoService, CommitService commitService, PreparedStatement setTag)
			throws IOException, SQLException {
		Repository repoObj = repoService.getRepository(repoOwner, repo);
		List<RepositoryTag> repoTags = repoService.getTags(repoObj);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1 * daysToProcess + timeInterval);
		java.util.Date limit = calendar.getTime();
		for (RepositoryTag tag : repoTags) {
			String tagName = tag.getName();
			TypedResource commit = tag.getCommit();
			RepositoryCommit commitObj = commitService.getCommit(repoObj, commit.getSha());
			java.util.Date date = commitObj.getCommit().getCommitter().getDate();
			if (date.compareTo(limit) < 0) {
				//System.out.printf("skip tag, date:%s by name:%s\n", date.toGMTString(), tagName);
				continue;
			}
			setTag.setDate(1, new java.sql.Date(date.getTime()));
			setTag.setString(2, tagName);
			System.out.println(setTag.toString());
			setTag.executeUpdate();
			System.out.printf("Insert tag, date:%s by name:%s\n", date.toGMTString(), tagName);
		}
		return true;
	}

	/*
	 * Process commits in the repository and calculate active team size vs time metric.
	 */
	private boolean processCommits(RepositoryService repoService, CommitService commitService,
			PreparedStatement setTeamSizeVSTime) throws IOException, SQLException, InterruptedException {
		// Set up attributes - data structures
		contributorsQueue = new LinkedList<>();
		contributorsTable = new HashMap<>();
		teamSize = 0;

		// Assuming commits are returned in a chronological order with most recent first
		Calendar calendar = Calendar.getInstance();
		java.util.Date today = calendar.getTime();
		java.util.Date last_date = calendar.getTime();
		calendar.add(Calendar.DATE, -1 * daysToProcess);
		java.util.Date limit = calendar.getTime();
		Repository repoObj = repoService.getRepository(repoOwner, repo);
		PageIterator<RepositoryCommit> commitPageIter = commitService.pageCommits(repoObj);
		outer_wh_1: while (true) {
			try {
				Thread.sleep(100 * exponentialBackoffTime);
				Collection<RepositoryCommit> commits = commitPageIter.next();
				for (RepositoryCommit commit : commits) {
					java.util.Date commitDate = commit.getCommit().getCommitter().getDate();
					System.out.println("Commit date: " + commitDate);
					if (addDates(commitDate, timeInterval).compareTo(limit) < 0) {
						System.out.printf("Stopped for date:%s by limit:%s\n", commitDate, limit);
						break outer_wh_1;
					}
					System.out.println("Commit date: " + commitDate);
					while (last_date.compareTo(commitDate) > 0) {
						System.out.println("Last date : " + last_date);
						pruneContributors(last_date);
						updateTeamSize();
						if (addDates(last_date, timeInterval).compareTo(today) <= 0) {
							setTeamSizeVSTime.setDate(1,
									new java.sql.Date(addDates(last_date, timeInterval).getTime()));
							setTeamSizeVSTime.setInt(2, teamSize);
							setTeamSizeVSTime.executeUpdate();
						}
						last_date = addDates(last_date, -1);
					}

					String authorName = "unnamed";
					try {
						authorName = commit.getAuthor().getLogin();
						if (authorName == null) {
							authorName = commit.getCommitter().getLogin();
						}
						if (authorName == null) {
							authorName = "unnamed";
						}
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
					if (!contributorsTable.containsKey(authorName)) {
						contributorsTable.put(authorName, 0);
					}
					contributorsTable.put(authorName, contributorsTable.get(authorName) + 1);
					contributorsQueue.add(new Contributor(commitDate, authorName));
				}
			} catch (NoSuchElementException e) {
				System.out.printf("Stopped for error  limit:%s\n", limit);
				e.printStackTrace();
				break outer_wh_1;
			}
		}
		return true;
	}

	/*
	 * Helper function to normalize the data structures.
	 */
	private void pruneContributors(java.util.Date latestDate) {
		while (!contributorsQueue.isEmpty()) {
			Contributor contributor = contributorsQueue.peek();
			// assuming sorted order, which it is
			if (contributor.date.compareTo(addDates(latestDate, timeInterval)) <= 0) {
				break;
			}
			contributorsQueue.poll();
			contributorsTable.put(contributor.login, contributorsTable.get(contributor.login) - 1);
		}
	}

	/*
	 * Helper function for adding Java dates.
	 */
	private java.util.Date addDates(java.util.Date date, int num) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, num);
		return calendar.getTime();
	}

	/*
	 * Helper function to update the current team size.
	 */
	private void updateTeamSize() {
		int res = 0;
		for (String contibutor : contributorsTable.keySet()) {
			if (contributorsTable.get(contibutor) > commitThreshold) {
				res++;
			}
		}
		teamSize = res;
	}

	/*
	 * Dummy mainline.
	 */
	public static void main(String[] args) {
		System.out.println("Not implemented!!");
	}

}
