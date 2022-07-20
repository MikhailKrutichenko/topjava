package ru.javawebinar.topjava.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.javawebinar.topjava.model.Role;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.repository.UserRepository;
import ru.javawebinar.topjava.util.ValidationUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@Transactional(readOnly = true)
public class JdbcUserRepository implements UserRepository {

    private static final RowMapper<User> ROW_MAPPER = BeanPropertyRowMapper.newInstance(User.class);

    private static final UserResultSetExecutor userResultSetExecutor = new UserResultSetExecutor();

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final SimpleJdbcInsert insertUser;

    @Autowired
    public JdbcUserRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.insertUser = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    @Transactional
    public User save(User user) {
        ValidationUtil.validate(user);
        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(user);
        if (user.isNew()) {
            Number newKey = insertUser.executeAndReturnKey(parameterSource);
            user.setId(newKey.intValue());
        } else if (namedParameterJdbcTemplate.update("""
                   UPDATE users SET name=:name, email=:email, password=:password, 
                   registered=:registered, enabled=:enabled, calories_per_day=:caloriesPerDay WHERE id=:id
                """, parameterSource) == 0) {
            return null;
        } else {
            jdbcTemplate.update("DELETE FROM user_roles WHERE user_id=?", user.id());
        }
        saveRoles(user);
        return user;
    }

    @Override
    @Transactional
    public boolean delete(int id) {
        return jdbcTemplate.update("DELETE FROM users WHERE id=?", id) != 0;
    }

    @Override
    public User get(int id) {
        List<User> users = jdbcTemplate.query("SELECT users.*, user_roles.role FROM users " +
                " LEFT JOIN user_roles ON users.id = user_roles.user_id WHERE users.id =?", userResultSetExecutor, id);
        return DataAccessUtils.singleResult(users);
    }

    @Override
    public User getByEmail(String email) {
        List<User> users = jdbcTemplate.query("SELECT users.*, user_roles.role FROM users " +
                "LEFT JOIN user_roles ON users.id = user_roles.user_id WHERE users.email=?", userResultSetExecutor, email);
        return DataAccessUtils.singleResult(users);
    }

    @Override
    public List<User> getAll() {
        String query = "SELECT users.*, user_roles.role FROM users " +
                "LEFT JOIN user_roles ON users.id = user_roles.user_id ORDER BY users.name, users.email";
        return jdbcTemplate.query(query, userResultSetExecutor);
    }

    private void saveRoles(User user) {
        Set<Role> setRoles = user.getRoles();
        if (setRoles != null && !setRoles.isEmpty()) {
            List<Role> roles = new ArrayList<>(setRoles);
            String query = "INSERT INTO user_roles (user_id, role) VALUES(?,?)";
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
    }

    private static class UserResultSetExecutor implements ResultSetExtractor<List<User>> {

        @Override
        public List<User> extractData(ResultSet rs) throws SQLException {
            Map<Integer, User> users = new LinkedHashMap<>();
            while (rs.next()) {
                User user = ROW_MAPPER.mapRow(rs, 0);
                if (users.computeIfPresent(user.id(), (k, v) -> {
                    try {
                        v.getRoles().add(Role.valueOf(rs.getString("role")));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return v;
                }) != null) {
                } else {
                    user.setRoles(new HashSet<>());
                    String role = rs.getString("role");
                    if (role != null) {
                        user.getRoles().add(Role.valueOf(rs.getString("role")));
                    }
                    users.put(user.id(), user);
                }
            }
            return new ArrayList<>(users.values());
        }
    }
}
