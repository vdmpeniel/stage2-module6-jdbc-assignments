package jdbc;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Getter
@Setter
@NoArgsConstructor
public class SimpleJDBCRepository {

    private final Logger LOGGER = Logger.getLogger("CustomConnector");
    private final CustomDataSource ds = CustomDataSource.getInstance();
    private final String ID = "id";
    private final String FIRST_NAME = "firstname";
    private final String LAST_NAME = "lastname";
    private final String AGE = "age";

    private static final String CREATE_USER_SQL = "INSERT INTO myusers(id, firstname, lastname, age) VALUES(?, ?, ?, ?)";
    private static final String UPDATE_USER_SQL = "UPDATE myusers SET firstname=?, lastname=?, age=? WHERE id=?";
    private static final String DELETE_USER = "DELETE FROM myusers WHERE id=?";
    private static final String FIND_USER_BY_ID_SQL = "SELECT * FROM myusers WHERE id=?";
    private static final String FIND_USER_BY_NAME_SQL = "SELECT * FROM myusers WHERE firstname=?";
    private static final String FIND_ALL_USER_SQL = "SELECT * FROM myusers";


    public Long createUser(User user) {
        try(Connection connection = ds.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(CREATE_USER_SQL, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, user.getId());
                ps.setString(2, user.getFirstName());
                ps.setString(3, user.getLastName());
                ps.setInt(4, user.getAge());

                int affectedRows = ps.executeUpdate();
                if(affectedRows > 0) {
                    try(ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if(generatedKeys.next()) {
                            return generatedKeys.getLong(1);
                        }
                    }
                } else { throw new SQLException("Unable to create user."); }
            }
        } catch(SQLException se){
            LOGGER.info(se.getMessage());
        }
        return null;
    }

    public User findUserById(Long userId) {
        try(Connection connection = ds.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(FIND_USER_BY_ID_SQL)) {
                ps.setLong(1, userId);
                try(ResultSet resultset = ps.executeQuery()){
                    if(resultset.next()) {
                        return User.builder()
                            .id(resultset.getLong(ID))
                            .firstName(resultset.getString(FIRST_NAME))
                            .lastName(resultset.getString(LAST_NAME))
                            .age(resultset.getInt(AGE))
                            .build();

                    } else { throw new SQLException("User with id:" + userId + " not found."); }
                }
            }

        } catch(SQLException se){
            LOGGER.info(se.getMessage());
        }
        return null;
    }

    public User findUserByName(String userName) {
        try(Connection connection = ds.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(FIND_USER_BY_NAME_SQL)){
                ps.setString(1, userName);

                try(ResultSet resultset = ps.executeQuery()){
                    if(resultset.next()) {
                        return User.builder()
                            .id(resultset.getLong(ID))
                            .firstName(resultset.getString(FIRST_NAME))
                            .lastName(resultset.getString(LAST_NAME))
                            .age(resultset.getInt(AGE))
                            .build();
                    } else { throw new SQLException("User with name:" + userName + " not found."); }
                }
            }
        } catch(SQLException se){
            LOGGER.info(se.getMessage());
        }
        return null;
    }

    public List<User> findAllUser() {
        try(Connection connection = ds.getConnection()){
            try(Statement statement = connection.createStatement()){
                try(ResultSet resultSet = statement.executeQuery(FIND_ALL_USER_SQL)){
                    List<User> userList = new ArrayList<>();
                    while(resultSet.next()){
                        userList.add(
                            User.builder()
                                .id(resultSet.getLong(ID))
                                .firstName(resultSet.getString(FIRST_NAME))
                                .lastName(resultSet.getString(LAST_NAME))
                                .age(resultSet.getInt(AGE))
                                .build()
                        );
                    }
                    return userList;
                }
            }
        } catch(SQLException se){ LOGGER.info(se.getMessage()); }
        return new ArrayList<>();
    }

    public User updateUser(User user) {
        try(Connection connection = ds.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(UPDATE_USER_SQL, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getFirstName());
                ps.setString(2, user.getLastName());
                ps.setInt(3, user.getAge());
                ps.setLong(4, user.getId());

                int affectedRows = ps.executeUpdate();
                if(affectedRows > 0) {
                    try(ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if(generatedKeys.next()) {
                            return findUserById(generatedKeys.getLong(1));
                        }
                    }
                } else { throw new SQLException("Unable to create user."); }
            }
        } catch(SQLException se){
            LOGGER.info(se.getMessage());
        }
        return null;

    }

    public void deleteUser(Long userId) {
        try(Connection connection = ds.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(DELETE_USER, Statement.RETURN_GENERATED_KEYS)){
                ps.setLong(1, userId);
                int affectedRows = ps.executeUpdate();
                if(affectedRows == 0){
                    throw new SQLException("No record was deleted.");
                }
            }
        } catch(SQLException se){
            LOGGER.info(se.getMessage());
        }
    }

}
