import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.UserService;

/*
 * @author Jevgenijus Cistiakovas cistiakj@tcd.ie
 * This is a mainline file for network graph.
 */

public class GraphMain {
	static String insertNodeSQL = "INSERT IGNORE INTO nodes" + " VALUES(?,?,?,?);";
	static String insertEdgeSQL = "INSERT IGNORE INTO edges" + " VALUES(?,?,?);";
	static String startUserConst = "yungene";
	static int depthLimitConst = 2;

	Connection conn;
	GitHubClient client;
	String startUser;
	int depthLimit;
	Set<Node> nodes;
	Set<Edge> edges;
	int exponentialBackoffTime = 1;
	int exponentialBackoffMultiplier = 2;
	int exponentialBackoffLimit = (int) Math.pow(exponentialBackoffMultiplier, 8);

	public GraphMain(Connection conn, GitHubClient client, String startUser, int depthLimit) {
		super();
		this.conn = conn;
		this.client = client;
		this.startUser = startUser;
		this.depthLimit = depthLimit;
		this.edges = new HashSet<>();
		this.nodes = new HashSet<>();
	}

	static class Edge {
		int u;
		int v;
		int weight;

		public Edge(int u, int v, int weight) {
			super();
			this.u = u;
			this.v = v;
			this.weight = weight;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + u;
			result = prime * result + v;
			result = prime * result + weight;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Edge other = (Edge) obj;
			if (u != other.u)
				return false;
			if (v != other.v)
				return false;
			if (weight != other.weight)
				return false;
			return true;
		}
		
		

	}

	static class Node {
		int id;
		String user;
		int followers;
		int following;

		public Node(int id, String user, int followers, int following) {
			super();
			this.id = id;
			this.user = user;
			this.followers = followers;
			this.following = following;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + followers;
			result = prime * result + following;
			result = prime * result + id;
			result = prime * result + ((user == null) ? 0 : user.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Node other = (Node) obj;
			if (followers != other.followers)
				return false;
			if (following != other.following)
				return false;
			if (id != other.id)
				return false;
			if (user == null) {
				if (other.user != null)
					return false;
			} else if (!user.equals(other.user))
				return false;
			return true;
		}

	}

	private void crawl() throws InterruptedException, IOException {
		UserService userService = new UserService(client);
		int id = 1;
		// do a bfs
		int level = 0;
		Queue<String> queue = new LinkedList();
		Queue<String> nextLevel = new LinkedList();
		Map<String, Integer> idMap = new HashMap<>();
		queue.add(startUser);
		Set<String> visitedUsers = new HashSet();
		while (!queue.isEmpty() && level < depthLimit && idMap.size() < 700 && visitedUsers.size() < 700) {
			System.out.println("level" + level);
			for (String userLogin : queue) {
				System.out.println(idMap.size());
				System.out.println("Visiting user:" + userLogin);
				// String userLogin = queue.poll();
				if (visitedUsers.contains(userLogin)) {
					continue;
				}
				visitedUsers.add(userLogin);
				if (!idMap.containsKey(userLogin)) {
					idMap.put(userLogin, id++);
				}
				User user = null;
				while (exponentialBackoffTime < exponentialBackoffLimit) {
					try {
						user = userService.getUser(userLogin);
						break;
					} catch (IOException e) {
						System.out.printf("Executing tag backoff for user:%s\n", userLogin);
						Thread.sleep(1000 * exponentialBackoffTime);
						exponentialBackoffTime *= exponentialBackoffMultiplier;
						if (exponentialBackoffTime > exponentialBackoffLimit) {
							return;
						}
						e.printStackTrace();
					}
				}
				exponentialBackoffTime = 1;
				Node node = new Node(idMap.get(userLogin), userLogin, user.getFollowers(), user.getFollowing());
				System.out.printf("user %s has %d followers and %d followings\n", node.user, node.followers,
						node.following);
				if(!nodes.contains(node)) {
				nodes.add(node);
				}
				PageIterator<User> followersIter = userService.pageFollowers(userLogin);
				Collection<User> followers = followersIter.next();
				int count = 0; // limit the growth for now
				for (User follower : followers) {
					count++;
					if (count > 40) {
						break;
					}
					String v = follower.getLogin();
					if (!idMap.containsKey(v)) {
						idMap.put(v, id++);
					}
					Edge e = new Edge(idMap.get(userLogin), idMap.get(v), 1);
					if (!visitedUsers.contains(v)) {
						nextLevel.add(v);
					}
					edges.add(e);
					Node node1 = new Node(idMap.get(v), v, follower.getFollowers(), follower.getFollowing());
					System.out.printf("user %s has %d followers and %d followings\n", node.user, node.followers,
							node.following);
					if(!nodes.contains(node1)) {
					nodes.add(node1);
					}
				}
				PageIterator<User> followingsIter = userService.pageFollowing(userLogin);
				Collection<User> followings = followingsIter.next();
				count = 0;
				for (User following : followings) {
					count++;
					if (count > 40) {
						break;
					}
					String u = following.getLogin();
					if (!idMap.containsKey(u)) {
						idMap.put(u, id++);
					}
					Edge e = new Edge(idMap.get(u), idMap.get(userLogin), 1);
					if (!visitedUsers.contains(u)) {

						nextLevel.add(u);
					}
					edges.add(e);
					Node node1 = new Node(idMap.get(u), u, following.getFollowers(), following.getFollowing());
					System.out.printf("user %s has %d followers and %d followings\n", node.user, node.followers,
							node.following);
					if(!nodes.contains(node1)) {
					nodes.add(node1);
					}
				}
			}
			level++;
			queue = nextLevel;
			nextLevel = new LinkedList();
		}

	}

	public static void main(String[] args) {
		GitHubClient client = new GitHubClient().setOAuth2Token(Main.readToken());
		try (Connection conn = MySQLJDBCUtil.getConnection();
				PreparedStatement insertNode = conn.prepareStatement(insertNodeSQL);
				PreparedStatement insertEdge = conn.prepareStatement(insertEdgeSQL);
				AutoCloseable finish = conn::rollback) {

			conn.setAutoCommit(false);
			// do work
			GraphMain gm = new GraphMain(conn, client, startUserConst, depthLimitConst);
			gm.crawl();
			for (Edge e : gm.edges) {
				insertEdge(conn, insertEdge, e.u, e.v, e.weight);
			}

			for (Node n : gm.nodes) {
				insertNode(conn, insertNode, n.id, n.user, n.followers, n.following);
			}
			// commit
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void insertNode(Connection conn, PreparedStatement insertNode, int id, String user, int followers,
			int following) throws SQLException {
		insertNode.setInt(1, id);
		insertNode.setString(2, user);
		insertNode.setInt(3, followers);
		insertNode.setInt(4, following);
		System.out.println(insertEdgeSQL.toString());
		insertNode.executeUpdate();
	}

	private static void insertEdge(Connection conn, PreparedStatement insertEdge, int u, int v, int weight)
			throws SQLException {
		insertEdge.setInt(1, u);
		insertEdge.setInt(2, v);
		insertEdge.setInt(3, weight);
		System.out.println(insertEdge.toString());
		insertEdge.executeUpdate();
	}

}
