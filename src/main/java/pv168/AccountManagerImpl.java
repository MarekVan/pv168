package pv168;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/**
 * Created by xvancik on 3/8/16.
 */
public class AccountManagerImpl implements AccountManager {

    private final DataSource dataSource;

    public AccountManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createAccount(Account account) {

        validate(account);
        if (account.getId() != null)
            throw new IllegalArgumentException("Passed account.id must not be set!");


        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStatement = connection.prepareStatement
                     ("INSERT INTO account (owner, balance) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS)
        ) {

            prepStatement.setString(1, account.getOwner());
            prepStatement.setBigDecimal(2, account.getBalance());

            int addedRows = prepStatement.executeUpdate();
            if (addedRows != 1) {
                throw new ServiceFailureException("Only one row should be affected by inserting"
                        + "record into database, but " + addedRows + " have been affected!");
            }

            ResultSet keyRs = prepStatement.getGeneratedKeys();
            account.setId(getKey(keyRs, account));


        } catch (SQLException ex) {
            throw new ServiceFailureException("Failed to create database record of an account: " + account, ex);
        }

    }

    @Override
    public void deleteAccount(Account account) {

        if (account == null)
            throw new IllegalArgumentException("Passed account is null!");
        if (account.getId() == null)
            throw new IllegalArgumentException("Id of account to be deleted must be set!");


        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStatement = connection.prepareStatement(""
                     + "DELETE FROM account WHERE id = ?")
        ) {

            prepStatement.setLong(1, account.getId());

            int updated = prepStatement.executeUpdate();

            if (updated == 0) {
                throw new EntityNotFoundException("Account" + account + "was not found in the database!");
            }
            if (updated != 1) {
                throw new ServiceFailureException("Only one row should be affected, but update affected " + updated + "!");
            }


        } catch (SQLException ex) {
            throw new ServiceFailureException("Failed to delete database record"
                    + " of account " + account, ex);
        }

    }

    @Override
    public void updateAccount(Account account) {

        validate(account);
        if (account.getId() == null)
            throw new IllegalArgumentException("Id of account to be updated must be set!");


        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStatement = connection.prepareStatement(""
                     + "UPDATE account SET owner = ?, balance = ? WHERE id = ?")
        ) {
            prepStatement.setString(1, account.getOwner());
            prepStatement.setBigDecimal(2, account.getBalance());
            prepStatement.setLong(3, account.getId());


            int updated = prepStatement.executeUpdate();

            if (updated == 0)
                throw new EntityNotFoundException("Account " + account + "was not found in the database!");
            if (updated != 1)
                throw new ServiceFailureException("Only one row should be affected, but update affected " + updated + "!");


        } catch (SQLException ex) {
            throw new ServiceFailureException("Failed to update database record of "
                    + "account" + account + "!", ex);
        }


    }

    @Override
    public Account findAccountById(Long id) {

        if (id == null)
            throw new IllegalArgumentException("Passed id is null, cannot find account without id!");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStatement = connection.prepareStatement(""
                     + "SELECT * FROM account WHERE id = ?")
        ) {
            prepStatement.setLong(1, id);

            ResultSet rs = prepStatement.executeQuery();

            if (rs.next()) {
                Account result = resultSetToAccount(rs);

                if (rs.next())
                    throw new ServiceFailureException("Operation retrieved more than one row!"
                            + "More than one entity with id " + id + "found!");

                return result;
            } else {
                return null;
            }


        } catch (SQLException ex) {
            throw new ServiceFailureException("Failed to retrieve account with id " + id + " from the database!");
        }

    }

    @Override
    public List<Account> findAllAccounts() {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStatement = connection.prepareStatement("SELECT * FROM account")
        ) {
            ResultSet rs = prepStatement.executeQuery();

            List<Account> result = new ArrayList<>();

            while (rs.next()) {
                result.add(resultSetToAccount(rs));
            }

            return result;

        } catch (SQLException ex) {
            throw new ServiceFailureException("Failed to retrieve all acount record!", ex);
        }


    }


    private void validate(Account account) {
        if (account == null)
            throw new IllegalArgumentException("Passed account is null!");
        if (account.getOwner() == null)
            throw new IllegalArgumentException("Owner must be set!");
        if (account.getBalance() == null)
            throw new IllegalArgumentException("Balance must be set!");

    }

    private Long getKey(ResultSet keyRs, Account account) throws SQLException {

        if (keyRs.next()) {
            if (keyRs.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: ResultSet with "
                        + "generated keys does not contain one collumn but contains "
                        + keyRs.getMetaData().getColumnCount() + "Occured after "
                        + "inserting account: \n" + account);
            }

            Long result = keyRs.getLong(1);

            if (keyRs.next()) {
                throw new ServiceFailureException("Internal Error: More than one key was "
                        + "retrieved when one record was inserted!" + "Occured after "
                        + "inserting account: \n" + account);
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: No key retrieved when one "
                    + "record was inserted!" + "Occured after "
                    + "inserting account: \n" + account);
        }

    }

    public static Account resultSetToAccount(ResultSet rs) throws SQLException {
        Account result = new Account();

        result.setId(rs.getLong("id"));
        result.setOwner(rs.getString("owner"));
        result.setBalance(rs.getBigDecimal("balance"));

        return result;
    }


}
