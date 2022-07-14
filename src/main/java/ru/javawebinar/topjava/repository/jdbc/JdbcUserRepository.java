package ru.javawebinar.topjava.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.javawebinar.topjava.model.Role;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.repository.UserRepository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository
@Transactional(readOnly = true)
public class JdbcUserRepository implements UserRepository {

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
        return user;
    }

    @Override
    @Transactional
    public boolean delete(int id) {
        return jdbcTemplate.update("DELETE FROM users WHERE id=?", id) != 0;
    }

    @Override
    public User get(int id) {
        SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT users.*, user_roles.role FROM users \n" +
                " LEFT JOIN user_roles ON users.id = user_roles.user_id WHERE users.id =?", id);

        return mappingUsersWithRoles(rs).stream().findFirst().orElse(null);
    }

    @Override
    public User getByEmail(String email) {
        SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT users.*, user_roles.role FROM users \n" +
                "LEFT JOIN user_roles ON users.id = user_roles.user_id WHERE users.email=?", email);
        return mappingUsersWithRoles(rs).stream().findFirst().get();
    }

    @Override
    public List<User> getAll() {
        SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT users.*, user_roles.role FROM users \n" +
                "LEFT JOIN user_roles ON users.id = user_roles.user_id ORDER BY users.name, users.email");
        return mappingUsersWithRoles(rs);
    }

    private List<User> mappingUsersWithRoles(SqlRowSet rs) {
        Map<Integer, User> usersMap = new LinkedHashMap<>();
        while (rs.next()) {
            int id = rs.getInt("id");
            if (usersMap.containsKey(id)) {
                usersMap.get(id).getRoles().add(Role.valueOf(rs.getString("role")));
            } else {
                String name = rs.getString("name");
                String email = rs.getString("email");
                String password = rs.getString("password");
                Date registered = rs.getDate("registered");
                boolean enabled = rs.getBoolean("enabled");
                int caloriesPerDay = rs.getInt("calories_per_day");
                Set<Role> roles = new HashSet<>();
                String role = rs.getString("role");
                if (Objects.nonNull(role)) {
                    roles.add(Role.valueOf(rs.getString("role")));
                }
                User user = new User(id, name, email, password, caloriesPerDay, enabled, registered, roles);
                usersMap.put(id, user);
            }
        }
        return new ArrayList<>(usersMap.values());
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
}
