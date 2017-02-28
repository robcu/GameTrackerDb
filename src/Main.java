import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static HashMap<String, User> users = new HashMap<>();

    public static void insertGame(Connection conn, String name, String genre, String platform, int year) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO games VALUES (NULL, name, genre, platform, year);");
        stmt.execute();
    }

    public static void deleteGame(Connection conn, int index) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM games WHERE id = index;");
        stmt.execute();
    }

    public static ArrayList<Game> selectGames(Connection conn) throws SQLException {
        ArrayList<Game> games = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM games");
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            int id = results.getInt("id");
            String name = results.getString("name");
            String genre = results.getString("genre");
            String platform = results.getString("platform");
            int year = results.getInt("releaseYear");
            games.add(new Game(name, genre, platform, year));
        }
        return games;

    }

    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("/jdbc:h2:./main");
        PreparedStatement stmt = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS games (id IDENTITY, name VARCHAR, genre VARCHAR, platform VARCHAR, releaseYear INT)");
        stmt.execute();


        System.out.println("Starting GameTracker...");
        Spark.init();
        Spark.get("/", (
                        (request, response) -> {
                            Session session = request.session();
                            String name = session.attribute("userName");
                            User user = users.get(name);

                            HashMap m = new HashMap();
                            if (user == null) {
                                return new ModelAndView(m, "login.html");
                            } else {
                                return new ModelAndView(user, "home.html");
                            }
                        }),
                new MustacheTemplateEngine()
        );

        Spark.post("/delete-game", (request, response) -> {
            Session session = request.session();

            //int index =
            //todo: write code!
            //deleteGame(conn, index);

            response.redirect("/");
            return "";
        });

        Spark.post("/create-game", (request, response) -> {
            Session session = request.session();
            String name = session.attribute("userName");
            User user = users.get(name);
            if (user == null) {
                throw new Exception("User not logged in");
            }
            String gameName = request.queryParams("gameName");
            String gameGenre = request.queryParams("gameGenre");
            String gamePlatform = request.queryParams("gamePlatform");
            int gameYear = Integer.parseInt(request.queryParams("gameYear"));

            //Game game = new Game(gameName, gameGenre, gamePlatform, gameYear);
            //user.games.add(game);
            insertGame(conn, gameName, gameGenre, gamePlatform, gameYear);

            response.redirect("/");

            return "";
        });

        Spark.post("/logout", (request, response) -> {
            Session session = request.session();
            session.invalidate();
            response.redirect("/");
            return "";
        });

        Spark.post("/login", (request, response) -> {
            String name = request.queryParams("loginName");
            User user = users.get(name);
            if (user == null) {
                user = new User(name);
                users.put(name, user);
            }
            Session session = request.session();
            session.attribute("userName", name);

            response.redirect("/");
            return "";
        });
    }
}
