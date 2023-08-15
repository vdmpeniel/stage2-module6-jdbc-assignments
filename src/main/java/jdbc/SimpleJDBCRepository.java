package jdbc;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleJDBCRepository {

    private Logger logger = Logger.getLogger("CustomConnector");
    private CustomDataSource ds = CustomDataSource.getInstance();
    private Connection connection = null;
    private PreparedStatement ps = null;
    private Statement st = null;

    private static final String createUserSQL = "INSERT INTO myusers(first_name, last_name, age) VALUES('Ana', 'Valdes', 26)";
    private static final String createUserWithPreparedSQL = "INSERT INTO myusers(first_name, last_name, age) VALUES(?, ?, ?) RETURNING id";
    private static final String updateUserSQL = "UPDATE myusers SET first_name='Yuni', last_name='Garcia', age=32 WHERE id=11";
    private static final String deleteUser = "DELETE FROM myusers WHERE id=?";
    private static final String findUserByIdSQL = "SELECT * FROM myusers WHERE id=?";
    private static final String findUserByNameSQL = "SELECT * FROM myusers WHERE first_name=?";
    private static final String findAllUserSQL = "SELECT * FROM myusers";

    public Long createUser() {

        try(Connection connection = ds.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(createUserSQL, Statement.RETURN_GENERATED_KEYS)) {
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
            logger.info(se.getMessage());
        }
        return null;
    }

    public Long createUser(String firstName, String lastName, int age) {
        try(Connection connection = ds.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(createUserWithPreparedSQL, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, firstName);
                ps.setString(2, lastName);
                ps.setInt(3, age);

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
            logger.info(se.getMessage());
        }
        return null;
    }

    public User findUserById(Long userId) {
        try(Connection connection = ds.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(findUserByIdSQL)) {
                ps.setLong(1, userId);
                try(ResultSet resultset = ps.executeQuery()){
                    if(resultset.next()) {
                        return User.builder()
                            .id(resultset.getLong("id"))
                            .firstName(resultset.getString("first_name"))
                            .lastName(resultset.getString("last_name"))
                            .age(resultset.getInt("age"))
                            .build();

                    } else { throw new SQLException("User with id:" + userId + " not found."); }
                }
            }

        } catch(SQLException se){
            logger.info(se.getMessage());
        }
        return null;
    }

    public User findUserByName(String userName) {
        try(Connection connection = ds.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(findUserByNameSQL)){
                ps.setString(1, userName);

                try(ResultSet resultset = ps.executeQuery()){
                    if(resultset.next()) {
                        return User.builder()
                            .id(resultset.getLong("id"))
                            .firstName(resultset.getString("first_name"))
                            .lastName(resultset.getString("last_name"))
                            .age(resultset.getInt("age"))
                            .build();                    } else { throw new SQLException("User with name:" + userName + " not found."); }
                }
            }
        } catch(SQLException se){
            logger.info(se.getMessage());
        }
        return null;
    }

    public List<User> findAllUser() {
        try(Connection connection = ds.getConnection()){
            try(Statement statement = connection.createStatement()){
                try(ResultSet resultSet = statement.executeQuery(findAllUserSQL)){
                    List<User> userList = new ArrayList<>();
                    while(resultSet.next()){
                        userList.add(
                            User.builder()
                                .id(resultSet.getLong("id"))
                                .firstName(resultSet.getString("first_name"))
                                .lastName(resultSet.getString("last_name"))
                                .age(resultSet.getInt("age"))
                                .build()
                        );
                    }
                    return userList;
                }
            }
        } catch(SQLException se){ logger.info(se.getMessage()); }
        return new ArrayList<User>();
    }

    public User updateUser() {
        try(Connection connection = ds.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(updateUserSQL, Statement.RETURN_GENERATED_KEYS)) {
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
            logger.info(se.getMessage());
        }
        return null;

    }

    private void deleteUser(Long userId) {
        try(Connection connection = ds.getConnection()){
            try(PreparedStatement ps = connection.prepareStatement(deleteUser, Statement.RETURN_GENERATED_KEYS)){
                ps.setLong(1, userId);
                int affectedRows = ps.executeUpdate();
                if(affectedRows == 0){
                    throw new SQLException("No record was deleted.");
                }
            }
        } catch(SQLException se){
            logger.info(se.getMessage());
        }
    }


    public static void main(String[] args) {
        SimpleJDBCRepository repository = new SimpleJDBCRepository();

        // create new user and get it by id
        //User newUser = repository.findUserById(repository.createUser("Yuya", "Perez", 52));
        //repository.logUserData(newUser);

        // get user by name
        //repository.logUserData(repository.findUserByName("Gumersindo"));

        // update user
        //repository.logUserData(repository.updateUser());

        // delete user
        repository.deleteUser(39l);

        // get all users
        repository.findAllUser().forEach(user -> {
            repository.logUserData(user);
        });
    }
    private void logUserData(User user){
        System.out.println(
            user.getId() + " | " +
            user.getFirstName() + " | " +
            user.getLastName() +  " | " +
            user.getAge()
        );
    }
}
