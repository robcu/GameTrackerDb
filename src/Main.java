import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void insertGame(Connection conn, String name, String genre, String platform, int year) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO games VALUES (NULL, ?, ?, ?, ?);");
        stmt.setString(1, name);
        stmt.setString(2, genre);
        stmt.setString(3, platform);
        stmt.setInt(4, year);
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
            games.add(new Game(id, name, genre, platform, year));
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
                            ArrayList<Game> homeGames = selectGames(conn);
                            HashMap m = new HashMap();
                            m.put("homeGames", homeGames);
                            return new ModelAndView(m, "home.html");
                        }),
                new MustacheTemplateEngine()
        );

        Spark.post("/delete-game", (request, response) -> {
            int index = Integer.parseInt(request.queryParams("DeleteIndex"));
            deleteGame(conn, index);

            response.redirect("/");
            return "";
        });

        Spark.post("/create-game", (request, response) -> {

            String gameName = request.queryParams("gameName");
            String gameGenre = request.queryParams("gameGenre");
            String gamePlatform = request.queryParams("gamePlatform");
            int gameYear = Integer.parseInt(request.queryParams("gameYear"));

            insertGame(conn, gameName, gameGenre, gamePlatform, gameYear);

            response.redirect("/");
            return "";
        });

        Spark.post("/edit-game", (request, response) -> {

            //todo: write method updateGame(); then call it here

            response.redirect("/");
            return "";
        });
    }
}
