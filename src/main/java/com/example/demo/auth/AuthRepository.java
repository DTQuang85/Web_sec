package com.example.demo.auth;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class AuthRepository {

    private final DataSource dataSource;

    public AuthRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<UserResponse> loginVulnerable(String username, String password) throws SQLException {
        // INTENTIONALLY INSECURE: direct string concatenation allows SQL Injection.
        String sql = "SELECT id, username, role FROM users WHERE username='" + username
                + "' AND password='" + password + "'";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return Optional.of(new UserResponse(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("role")
                ));
            }
            return Optional.empty();
        }
    }

    public Optional<UserResponse> loginSafe(String username, String password) throws SQLException {
        String sql = "SELECT id, username, role FROM users WHERE username = ? AND password = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new UserResponse(
                            resultSet.getInt("id"),
                            resultSet.getString("username"),
                            resultSet.getString("role")
                    ));
                }
                return Optional.empty();
            }
        }
    }
}

