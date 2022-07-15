package ru.javawebinar.topjava.repository.jdbc;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.javawebinar.topjava.model.Role;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.repository.UserRepository;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

@Repository
@Transactional(readOnly = true)
public class JdbcUserRepository implements UserRepository {

    private static final Logger log = getLogger(JdbcUserRepository.class);

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final SimpleJdbcInsert insertUser;

    private final Validator validator;

    @Autowired
    public JdbcUserRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.insertUser = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Override
    @Transactional
    public User save(User user) {
        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(user);

        if (user.isNew()) {
            Number newKey = insertUser.executeAndReturnKey(parameterSource);
            user.setId(newKey.intValue());
            saveRoles(user);
        } else if (namedParameterJdbcTemplate.update("""
                   UPDATE users SET name=:name, email=:email, password=:password, 
                   registered=:registered, enabled=:enabled, calories_per_day=:caloriesPerDay WHERE id=:id
                """, parameterSource) == 0) {
            return null;
        } else {
            jdbcTemplate.update("DELETE FROM user_roles WHERE user_id=?", user.id());
            saveRoles(user);
        }
        return isValid(Collections.singletonList(user)) ? user : null;
    }

    @Override
    @Transactional
    public boolean delete(int id) {
        return jdbcTemplate.update("DELETE FROM users WHERE id=?", id) != 0;
    }

    @Override
    public User get(int id) {
        List<User> users = jdbcTemplate.query("SELECT users.*, user_roles.role FROM users \n" +
                " LEFT JOIN user_roles ON users.id = user_roles.user_id WHERE users.id =?", new UserResultSetExecutor(), id);
        return isValid(users) ? users.stream().findFirst().orElse(null) : null;
    }

    @Override
    public User getByEmail(String email) {
        List<User> users = jdbcTemplate.query("SELECT users.*, user_roles.role FROM users \n" +
                "LEFT JOIN user_roles ON users.id = user_roles.user_id WHERE users.email=?", new UserResultSetExecutor(), email);
        return isValid(users) ? users.stream().findFirst().orElse(null) : null;
    }

    @Override
    public List<User> getAll() {
        String query = "SELECT users.*, user_roles.role FROM users \n" +
                "LEFT JOIN user_roles ON users.id = user_roles.user_id ORDER BY users.name, users.email";
        List<User> users = jdbcTemplate.query(query, new UserResultSetExecutor());
        return isValid(users) ? users : Collections.emptyList();
    }

    private void saveRoles(User user) {
        String query = "INSERT INTO user_roles (user_id, role) VALUES(?,?)";
        List<Role> roles = new ArrayList<>(user.getRoles());
        jdbcTemplate.batchUpdate(query, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, user.id());
                ps.setString(2, roles.get(i).name());
            }

            @Override
            public int getBatchSize() {
                return roles.size();
            }
        });
    }

    private class UserResultSetExecutor implements ResultSetExtractor<List<User>> {

        @Override
        public List<User> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<User> users = new ArrayList<>();
            int usersSize = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                if (usersSize != 0 && users.get(usersSize - 1).id() == id) {
                    users.get(usersSize - 1).getRoles().add(Role.valueOf(rs.getString("role")));
                } else {
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    String password = rs.getString("password");
                    Date registered = rs.getDate("registered");
                    boolean enabled = rs.getBoolean("enabled");
                    int caloriesPerDay = rs.getInt("calories_per_day");
                    Set<Role> roles = new HashSet<>();
                    String role = rs.getString("role");
                    if (role != null) {
                        roles.add(Role.valueOf(role));
                    }
                    User user = new User(id, name, email, password, caloriesPerDay, enabled, registered, roles);
                    users.add(user);
                    usersSize++;
                }
            }
            return users;
        }
    }

    private boolean isValid(List<User> users) {
        for (User user : users) {
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            if (!violations.isEmpty()) {
                violations.forEach(v -> log.info(v.getMessage()));
                return false;
            }
        }
        return true;
    }
}
