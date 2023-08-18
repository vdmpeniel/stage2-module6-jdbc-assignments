package jdbc;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

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
    private static final String TABLE_NAME = "myusers";

    private static final String TABLE_EXIST_SQL = String.format("SELECT table_name FROM information_schema.tables WHERE table_name = '%s'", TABLE_NAME);
    private static final String CREATE_TABLE_SQL = String.format("CREATE TABLE %s(id bigint UNIQUE NOT NULL PRIMARY KEY, firstname varchar(255) NOT NULL, lastname varchar(255) NOT NULL, age int NOT NULL)", TABLE_NAME);

    private static final String CREATE_USER_SQL = String.format("INSERT INTO %s(id, firstname, lastname, age) VALUES(?, ?, ?, ?)", TABLE_NAME);
    private static final String UPDATE_USER_SQL = String.format("UPDATE %s SET firstname = ?, lastname = ?, age = ? WHERE id = ?", TABLE_NAME);
    private static final String DELETE_USER = String.format("DELETE FROM %s WHERE id = ?", TABLE_NAME);
    private static final String FIND_USER_BY_ID_SQL = String.format("SELECT id, firstname, lastname, age FROM %s WHERE id = ?", TABLE_NAME);
    private static final String FIND_USER_BY_NAME_SQL = String.format("SELECT id, firstname, lastname, age FROM %s WHERE firstname = ?", TABLE_NAME);
    private static final String FIND_ALL_USER_SQL = String.format("SELECT id, firstname, lastname, age FROM %s", TABLE_NAME);

    public SimpleJDBCRepository(){
        createTableIfNotExist();
    }

    public Long createUser(User user) {
        try(Connection connection = dataSource.getConnection()){
            preparedStatement = connection.prepareStatement(CREATE_USER_SQL, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setLong(1, user.getId());
            preparedStatement.setString(2, user.getFirstName());
            preparedStatement.setString(3, user.getLastName());
            preparedStatement.setInt(4, user.getAge());
            log.info(preparedStatement.toString());

            if (preparedStatement.executeUpdate() > 0) { // affected rows
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else { throw new SQLException("Unable to create user."); }
            } else { throw new SQLException("Unable to create user."); }

        } catch(SQLException se){
            log.info(se.getMessage());
            return null;
        }

    }

    public User findUserById(Long userId) {
        try (Connection connection = dataSource.getConnection()) {
            preparedStatement = connection.prepareStatement(FIND_USER_BY_ID_SQL);
            preparedStatement.setLong(1, userId);
            log.info(preparedStatement.toString());

            ResultSet resultset = preparedStatement.executeQuery();
            if (resultset.next()) {
                return buildUserFromResultSer(resultset);
            } else { throw new SQLException("User with id: " + userId + " not found."); }

        }    catch(SQLException se){
            log.info(se.getMessage());
            return null;
        }

    }

    public User findUserByName(String userName) {
        try (Connection connection = dataSource.getConnection()) {
            preparedStatement = connection.prepareStatement(FIND_USER_BY_NAME_SQL);
            preparedStatement.setString(1, userName);
            log.info(preparedStatement.toString());

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return buildUserFromResultSer(resultSet);
            } else { throw new SQLException("User with name: " + userName + " not found."); }

        } catch (SQLException se){
            log.info(se.getMessage());
            return null;
        }
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

        } catch (SQLException se) {
            log.info(se.getMessage());
            return new ArrayList<>();
        }

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
                } else { throw new SQLException("Unable to create user."); }
            } else { throw new SQLException("Unable to create user."); }

        } catch (SQLException se) {
            log.info(se.getMessage());
            return null;
        }
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
    private void createTableIfNotExist(){
        try(Connection connection = dataSource.getConnection()) {
            if (!doesTableExists(connection)){
                log.info(String.format("Table %s is missing", TABLE_NAME));
                createTable(connection);
            }

        } catch (Exception e){
            log.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private boolean doesTableExists(Connection connection) throws SQLException {
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(TABLE_EXIST_SQL);
            return resultSet.next();

        } catch(Exception e) {
            log.info(e.getMessage());
            return false;
        }
    }

    private void createTable(Connection connection) throws Exception {
        statement = connection.createStatement();
        statement.executeUpdate(CREATE_TABLE_SQL);

        if (doesTableExists(connection)){
            log.info(String.format("Table %s is ready.", TABLE_NAME));
        } else { throw new SQLException(String.format("Unable to create table %s", TABLE_NAME)); }
    }


    public static void main(String[] args) {
        SimpleJDBCRepository repo = new SimpleJDBCRepository();
        Random random = new Random();
        IntStream.rangeClosed(0, 9).forEach(i -> repo.createUser(
            User.builder()
                .id(Math.abs(random.nextLong(10000)))
                .firstName("put a name here")
                .lastName("lastname")
                .age(Math.abs(random.nextInt(120)))
                .build()
        ));
        repo.findAllUser().forEach(user -> log.info(user.toString()));
    }

}
