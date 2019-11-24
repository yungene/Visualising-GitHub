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
	// Prepared statements
	PreparedStatement setTag;
	PreparedStatement setTeamSizeVSTime;
	// Configurations
	int exponentialBackoffTime = 1;
	int exponentialBackoffMultiplier = 2;
	int exponentialBackoffLimit = (int) Math.pow(exponentialBackoffMultiplier, 8);
	int timeInterval = 7; // days
	int commitThreshold = 3; // commits
	int daysToProcess = 400; // days to process

	static class Contributor {
		java.util.Date date;
		String login;

		public Contributor(Date date, String login) {
			super();
			this.date = date;
			this.login = login;
		}
	}

	public DataCollector(Connection conn, GitHubClient client, String repo, String repoOwner) throws SQLException {
		super();
		this.conn = conn;
		this.client = client;
		this.repo = repo;
		this.repoOwner = repoOwner;
		this.setTag = conn.prepareStatement(
				String.format("INSERT IGNORE INTO release_table" + "VALUES(%s,%s,?,?);", repo, repoOwner));
		this.setTeamSizeVSTime = conn
				.prepareStatement(String.format("INSERT IGNORE INTO active_team_size_vs_time" + "VALUES(%s,%s,?,?,%d);",
						repo, repoOwner, timeInterval));
		System.out.printf("Created a new DataCollector for repo:%s by user:%s\n", repo,repoOwner);
	}

	/*
	 * Process the repo provided and put all the results into the DB Use a big
	 * single transaction to ensure atomicity
	 */
	public boolean process() {
		// Start transaction
		RepositoryService repoService = new RepositoryService(client);
		CommitService commitService = new CommitService(client);
		exponentialBackoffTime = 1;
		while (exponentialBackoffTime < exponentialBackoffLimit) {
			try {
				processTags(repoService, commitService);
				conn.commit();
			} catch (SQLException | IOException ex) {
				exponentialBackoffTime *= exponentialBackoffMultiplier;
				try {
					if (conn != null)
						conn.rollback();
				} catch (SQLException e) {
					System.err.println(e.getMessage());
				}
			}
		}
		if(exponentialBackoffTime >= exponentialBackoffLimit) {
			return false;
		}
		exponentialBackoffTime = 1;
		while (exponentialBackoffTime < exponentialBackoffLimit) {
			try {
				processCommits(repoService, commitService);
				conn.commit();
			} catch (SQLException | IOException ex) {
				exponentialBackoffTime *= exponentialBackoffMultiplier;
				try {
					if (conn != null)
						conn.rollback();
				} catch (SQLException e) {
					System.err.println(e.getMessage());
				}
			}
		}
		if(exponentialBackoffTime >= exponentialBackoffLimit) {
			return false;
		}
		// End transaction
		return true;
	}

	private boolean processTags(RepositoryService repoService, CommitService commitService)
			throws IOException, SQLException {
		Repository repoObj = repoService.getRepository(repoOwner, repo);
		List<RepositoryTag> repoTags = repoService.getTags(repoObj);
		for (RepositoryTag tag : repoTags) {
			setTag.setString(2, tag.getName());
			TypedResource commit = tag.getCommit();
			RepositoryCommit commitObj = commitService.getCommit(repoObj, commit.getSha());
			setTag.setDate(1, new java.sql.Date(commitObj.getAuthor().getCreatedAt().getTime()));
			setTag.execute();
		}
		return true;
	}

	private boolean processCommits(RepositoryService repoService, CommitService commitService)
			throws IOException, SQLException {
		// Set up attributes - data structures
		contributorsQueue = new LinkedList<>();
		contributorsTable = new HashMap<>();
		teamSize = 0;

		// Assuming commits are returned in a chronological order with most recent first
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1 * daysToProcess);
		java.util.Date limit = calendar.getTime();
		Repository repoObj = repoService.getRepository(repoOwner, repo);
		PageIterator<RepositoryCommit> commitPageIter = commitService.pageCommits(repoObj);
		outer_wh_1: while (true) {
			Collection<RepositoryCommit> commits = commitPageIter.next();
			for (RepositoryCommit commit : commits) {
				java.util.Date commitDate = commit.getCommit().getAuthor().getDate();
				if (commitDate.compareTo(limit) < 0) {
					break outer_wh_1;
				}
				String authorName = commit.getAuthor().getLogin();
				if (!contributorsTable.containsKey(authorName)) {
					contributorsTable.put(authorName, 0);
				}
				contributorsTable.put(authorName, contributorsTable.get(authorName) + 1);
				contributorsQueue.add(new Contributor(commitDate, authorName));

				pruneContributors(commitDate);
				updateTeamSize();
				setTeamSizeVSTime.setDate(1, new java.sql.Date(commitDate.getTime()));
				setTeamSizeVSTime.setInt(2, teamSize);
			}
		}
		return true;
	}

	private void pruneContributors(java.util.Date latestDate) {
		while (!contributorsQueue.isEmpty()) {
			Contributor contributor = contributorsQueue.peek();
			// assuming sorted order
			if (contributor.date.compareTo(addDates(latestDate, daysToProcess)) <= 0) {
				break;
			}
			contributorsQueue.poll();
			contributorsTable.put(contributor.login, contributorsTable.get(contributor.login) - 1);
		}
	}

	private java.util.Date addDates(java.util.Date date, int num) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, num);
		return calendar.getTime();
	}

	private void updateTeamSize() {
		int res = 0;
		for (String contibutor : contributorsTable.keySet()) {
			if (contributorsTable.get(contibutor) > commitThreshold) {
				res++;
			}
		}
		teamSize = res;
	}

	public static void main(String[] args) {

		// "tetris+language:assembly&sort=stars&order=desc"

		// GitHubClient client = new GitHubClient().setOAuth2Token(readToken());
		// RepositoryService service = new RepositoryService(client);
//		try {
//			for (Repository repo : service.getRepositories("yungene")) {
//				if(!repo.getOwner().getLogin().equals("yungene")) {
//					continue;
//				}
//				System.out.println(repo.getName() + " Watchers: " + repo.getWatchers());
//				//Repository repo = service.getRepository("yungene", "OnlineJudges");
//				CommitService commServ = new CommitService(client);
//				 PageIterator<RepositoryCommit> pageIter = commServ.pageCommits(repo);
//				 Collection<RepositoryCommit> coll = pageIter.next();
//				 for(RepositoryCommit commit: coll) {
//					 RepositoryCommit commitFull = new CommitService(client).getCommit(repo, commit.getSha());
//					 if(commitFull.getStats()!= null) {
//					 System.out.println("Total is " + commitFull.getStats().getTotal());
//					 }
//				 }
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	// private void readInTop
}
