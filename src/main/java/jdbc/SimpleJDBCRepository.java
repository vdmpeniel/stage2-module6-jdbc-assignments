package jdbc;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Getter
@Setter

public class SimpleJDBCRepository {

    private final CustomDataSource dataSource = CustomDataSource.getInstance();
    private PreparedStatement preparedStatement = null;
    private Statement statement = null;

    private static final String ID = "id";
    private static final String FIRST_NAME = "firstname";
    private static final String LAST_NAME = "lastname";
    private static final String AGE = "age";

    private static final String TABLE_EXIST_SQL = "SELECT 1 FROM myusers LIMIT 1";
    private static final String CREATE_TALE_SQL = "CREATE TABLE myusers(id bigint UNIQUE NOT NULL PRIMARY KEY, firstname varchar(255) NOT NULL, lastname varchar(255) NOT NULL, age int NOT NULL)";

    private static final String CREATE_USER_SQL = "INSERT INTO myusers(id, firstname, lastname, age) VALUES(?, ?, ?, ?)";
    private static final String UPDATE_USER_SQL = "UPDATE myusers SET firstname = ?, lastname = ?, age = ? WHERE id = ?";
    private static final String DELETE_USER = "DELETE FROM myusers WHERE id = ?";
    private static final String FIND_USER_BY_ID_SQL = "SELECT id, firstname, lastname, age FROM myusers WHERE id = ?";
    private static final String FIND_USER_BY_NAME_SQL = "SELECT id, firstname, lastname, age FROM myusers WHERE firstname = ?";
    private static final String FIND_ALL_USER_SQL = "SELECT id, firstname, lastname, age FROM myusers";


    public SimpleJDBCRepository(){
        try(Connection connection = dataSource.getConnection()) {
            if (!tableExists(connection)){
                log.info("Table myusers is missing");
                createTable(connection);
            }
            log.info("Table myusers is ready.");

        } catch (SQLException se){
            log.info(se.getMessage());
        }
    }

    public Long createUser(User user) {
        try(Connection connection = dataSource.getConnection()){
            preparedStatement = connection.prepareStatement(CREATE_USER_SQL, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setLong(1, user.getId());
            preparedStatement.setString(2, user.getFirstName());
            preparedStatement.setString(3, user.getLastName());
            preparedStatement.setInt(4, user.getAge());
            log.info(preparedStatement.toString());

            if (preparedStatement.executeUpdate() > 0) { // returns affected rows
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            } else { throw new SQLException("Unable to create user."); }

        } catch(SQLException se){
            log.info(se.getMessage());
        }
        return null;
    }

    public User findUserById(Long userId) {
        try (Connection connection = dataSource.getConnection()) {
            preparedStatement = connection.prepareStatement(FIND_USER_BY_ID_SQL);
            preparedStatement.setLong(1, userId);
            log.info(preparedStatement.toString());

            ResultSet resultset = preparedStatement.executeQuery();
            if (resultset.next()) {
                return buildUserFromResultSer(resultset);
            } else { throw new SQLException("User with id:" + userId + " not found."); }

        }    catch(SQLException se){
            log.info(se.getMessage());
        }
        return null;
    }

    public User findUserByName(String userName) {
        try (Connection connection = dataSource.getConnection()) {
            preparedStatement = connection.prepareStatement(FIND_USER_BY_NAME_SQL);
            preparedStatement.setString(1, userName);
            log.info(preparedStatement.toString());

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return buildUserFromResultSer(resultSet);
            } else { throw new SQLException("User with name:" + userName + " not found."); }

        } catch (SQLException se){
            log.info(se.getMessage());
        }
        return null;
    }

    public List<User> findAllUser() {
        try (Connection connection = dataSource.getConnection()) {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(FIND_ALL_USER_SQL);
            log.info(FIND_ALL_USER_SQL);

            List<User> userList = new ArrayList<>();
            while(resultSet.next()){
                userList.add(buildUserFromResultSer(resultSet));
            }
            return userList;

        } catch (SQLException se) { log.info(se.getMessage()); }
        return new ArrayList<>();
    }

    public User updateUser(User user) {
        try(Connection connection = dataSource.getConnection()){
            preparedStatement = connection.prepareStatement(UPDATE_USER_SQL, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setInt(3, user.getAge());
            preparedStatement.setLong(4, user.getId());
            log.info(preparedStatement.toString());

            if (preparedStatement.executeUpdate() > 0) { // returns affected rows
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if(generatedKeys.next()) {
                    return findUserById(generatedKeys.getLong(1));
                }
            } else { throw new SQLException("Unable to create user."); }

        } catch (SQLException se) { log.info(se.getMessage()); }
        return null;
    }

    public void deleteUser(Long userId) {
        try (Connection connection = dataSource.getConnection()) {
            preparedStatement = connection.prepareStatement(DELETE_USER, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setLong(1, userId);
            log.info(preparedStatement.toString());

            if (preparedStatement.executeUpdate() == 0){
                throw new SQLException("UserId " + userId + " not found or an error occurred. No record was deleted.");
            }

        } catch (SQLException se) {
            log.info(se.getMessage());
        }
    }

    private User buildUserFromResultSer(ResultSet resultSet) throws SQLException{
        return User.builder()
            .id(resultSet.getLong(ID))
            .firstName(resultSet.getString(FIRST_NAME))
            .lastName(resultSet.getString(LAST_NAME))
            .age(resultSet.getInt(AGE))
            .build();
    }


    /* Sanity Check!
    * */

    private boolean tableExists(Connection connection) throws SQLException {
        try{
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(TABLE_EXIST_SQL);
            return resultSet.next();

        } catch (SQLException e) {
            return false; // Table does not exist
        }
    }

    private void createTable(Connection connection) throws SQLException {
        statement = connection.createStatement();
        int result = statement.executeUpdate(CREATE_TALE_SQL);
    }














}
