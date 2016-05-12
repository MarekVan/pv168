package pv168;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xvancik on 3/8/16.
 */
public class AccountManagerImpl implements AccountManager {

    private final DataSource dataSource;
    final static Logger log = LoggerFactory.getLogger(AccountManagerImpl.class);

    public AccountManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createAccount(Account account) throws ServiceFailureException {

        checkAccountForCreateAccount(account);

        try (Connection connection = dataSource.getConnection()) {
            createAccountInnerProcess(connection, account);
            log.info("account {} was created", account);
        } catch (SQLException ex) {
            log.error("account {} coudl not be created", account);
            throw new ServiceFailureException("Failed to create database record of an account: " + account, ex);
        }
    }

    @Override
    public void createAccount(Account account, Connection con) throws ServiceFailureException {
        checkAccountForCreateAccount(account);
        createAccountInnerProcess(con, account);
        log.info("account {} was created", account);
    }

    @Override
    public void deleteAccount(Account account) throws ServiceFailureException {

        checkAccountForDeleteAccount(account);

        try (Connection connection = dataSource.getConnection()) {
            deleteAccountInnerProcess(connection, account);
            log.info("account {} was deleted", account);
        } catch (SQLException ex) {
            log.error("account {} coudl not be deleted", account);
            throw new ServiceFailureException("Failed to delete database record"
                    + " of account " + account, ex);
        }
    }

    @Override
    public void deleteAccount(Account account, Connection con) throws ServiceFailureException {
        checkAccountForDeleteAccount(account);
        deleteAccountInnerProcess(con, account);
         log.info("account {} was deleted", account);
    }

    @Override
    public void updateAccount(Account account) throws ServiceFailureException {

        checkAccountForUpdateAccount(account);

        try (Connection connection = dataSource.getConnection();) {
            updateAccountInnerProcess(connection, account);
             log.info("account {} was updated", account);
        } catch (SQLException ex) {
            log.error("account {} coudl not be updated", account);
            throw new ServiceFailureException("Failed to update database record of "
                    + "account" + account + "!", ex);
        }
    }

    @Override
    public void updateAccount(Account account, Connection con) throws ServiceFailureException {
        checkAccountForUpdateAccount(account);
        updateAccountInnerProcess(con, account);
        log.info("account {} was updated", account);
    }

    @Override
    public Account findAccountById(Long id) throws ServiceFailureException {

        checkIdNotNull(id);

        try (Connection connection = dataSource.getConnection();) {
            return findAccountByIdInnerProcess(connection, id);
        } catch (SQLException ex) {
            log.error("account with id {} could not be find", id);
            throw new ServiceFailureException("Failed to retrieve account with id " + id + " from the database!", ex);
        }

    }

    @Override
    public Account findAccountById(Long id, Connection con) throws ServiceFailureException {
        checkIdNotNull(id);
        return findAccountByIdInnerProcess(con, id);
    }

    @Override
    public List<Account> findAllAccounts() throws ServiceFailureException {

        try (Connection connection = dataSource.getConnection();) {
            return findAllAccountsInnerProcess(connection);
        } catch (SQLException ex) {
            log.error("Failed to retrieve all acounts record");
            throw new ServiceFailureException("Failed to retrieve all acount record!", ex);
        }
    }

    @Override
    public List<Account> findAllAccounts(Connection con) throws ServiceFailureException {
        return findAllAccountsInnerProcess(con);
    }

//------------------------------------------------------------------------------
    public static Long getKey(ResultSet keyRs, Account account) throws SQLException {

        if (keyRs.next()) {
            if (keyRs.getMetaData().getColumnCount() != 1) {
                log.warn("Internal Error: generated keys does not contain one collumn. Occured after inserting account {}", account);
                throw new ServiceFailureException("Internal Error: ResultSet with "
                        + "generated keys does not contain one collumn but contains "
                        + keyRs.getMetaData().getColumnCount() + "Occured after "
                        + "inserting account: \n" + account);
            }

            Long result = keyRs.getLong(1);

            if (keyRs.next()) {
                log.warn("Internal Error: More than one key was retrieved. Occured after inserting account {}", account);
                throw new ServiceFailureException("Internal Error: More than one key was "
                        + "retrieved when one record was inserted!" + "Occured after "
                        + "inserting account: \n" + account);
            }
            return result;
        } else {
            log.warn("Internal Error: No key retrieved. Occured after inserting account {}", account);
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

//------------------------------------------------------------------------------
    private void validate(Account account) {
        if (account == null) {
            log.warn("Operation failed: account is null!");
            throw new IllegalArgumentException("Passed account is null!");
        }
        if (account.getOwner() == null) {
            log.warn("Operation failed: owner is null!");
            throw new IllegalArgumentException("Owner must be set!");
        }
        if (account.getBalance() == null) {
            log.warn("Operation failed: balance is null!");
            throw new IllegalArgumentException("Balance must be set!");
        }

    }

    private void checkAccountForCreateAccount(Account account) {
        validate(account);
        if (account.getId() != null) {
            log.warn("Operation failed: passed id is null!");
            throw new IllegalArgumentException("Passed account.id must not be set!");
        }
    }

    private void createAccountInnerProcess(Connection con, Account account) {
        try (
                PreparedStatement prepStatement = con.prepareStatement("INSERT INTO account (owner, balance) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS)) {
            prepStatement.setString(1, account.getOwner());
            prepStatement.setBigDecimal(2, account.getBalance());

            int addedRows = prepStatement.executeUpdate();
            if (addedRows != 1) {
                log.error(addedRows + " have been affected!");
                throw new ServiceFailureException("Only one row should be affected by inserting"
                        + "record into database, but " + addedRows + " have been affected!");
            }

            ResultSet keyRs = prepStatement.getGeneratedKeys();
            account.setId(getKey(keyRs, account));

        } catch (SQLException ex) {
            log.error("account {} coudl not be created", account);
            throw new ServiceFailureException("Failed to create database record of an account: " + account, ex);
        }
    }

    private void checkAccountForDeleteAccount(Account account) {
        if (account == null) {
            log.warn("Operation failed: passed account is null!");
            throw new IllegalArgumentException("Passed account is null!");
        }
        if (account.getId() == null) {
            log.warn("Operation failed: passed id is null!");
            throw new IllegalArgumentException("Id of account to be deleted must be set!");
        }
    }

    private void deleteAccountInnerProcess(Connection con, Account account) {

        try (PreparedStatement prepStatement = con.prepareStatement(
                "DELETE FROM account WHERE id = ?")) {
            prepStatement.setLong(1, account.getId());

            int deleted = prepStatement.executeUpdate();

            if (deleted == 0) {
                log.error("Entity {} not found", account);
                throw new EntityNotFoundException("Account" + account + "was not found in the database!");
            }
            if (deleted != 1) {
                log.error(deleted + " rows have been affected!");
                throw new ServiceFailureException("Only one row should be affected, but delete affected " + deleted + "!");
            }

        } catch (SQLException ex) {
            log.error("account {} coudl not be deleted", account);
            throw new ServiceFailureException("Failed to delete database record"
                    + " of account " + account, ex);
        }
    }

    private void checkAccountForUpdateAccount(Account account) {
        validate(account);
        if (account.getId() == null) {
            log.error("Operation failed: Id is null!");
            throw new IllegalArgumentException("Id of account to be updated must be set!");
        }
    }

    private void updateAccountInnerProcess(Connection con, Account account) {

        try (PreparedStatement prepStatement = con.prepareStatement(""
                + "UPDATE account SET owner = ?, balance = ? WHERE id = ?")) {

            prepStatement.setString(1, account.getOwner());
            prepStatement.setBigDecimal(2, account.getBalance());
            prepStatement.setLong(3, account.getId());

            int updated = prepStatement.executeUpdate();

            if (updated == 0) {
                log.error("Entity {} not found", account);
                throw new EntityNotFoundException("Account " + account + "was not found in the database!");
            }
            if (updated != 1) {
                log.error(updated + " rows have been affected!");
                throw new ServiceFailureException("Only one row should be affected, but update affected " + updated + "!");
            }

        } catch (SQLException ex) {
            log.error("account {} could not be updated", account);
            throw new ServiceFailureException("Failed to update database record of "
                    + "account" + account + "!", ex);
        }

    }

    private void checkIdNotNull(Long id) {
        if (id == null) {
            log.error("Operation failed: Id is null!");
            throw new IllegalArgumentException("Passed id is null, cannot find account without id!");
        }
    }

    private Account findAccountByIdInnerProcess(Connection con, Long id) {

        try (PreparedStatement prepStatement = con.prepareStatement(""
                + "SELECT * FROM account WHERE id = ?")) {

            prepStatement.setLong(1, id);

            ResultSet rs = prepStatement.executeQuery();

            if (rs.next()) {
                Account result = resultSetToAccount(rs);

                if (rs.next()) {
                    log.error("found more than one entity with id {}", id);
                    throw new ServiceFailureException("Operation retrieved more than one row!"
                            + "More than one entity with id " + id + "found!");
                }
                log.info("account {} was found", result);
                return result;
            } else {
                log.info("account with id {} was not found", id);
                return null;
            }

        } catch (SQLException ex) {
            log.error("Failed to retrieve account with id ()", id);
            throw new ServiceFailureException("Failed to retrieve account with id " + id + " from the database!", ex);
        }

    }

    private List<Account> findAllAccountsInnerProcess(Connection con) {

        try (PreparedStatement prepStatement = con.prepareStatement("SELECT * FROM account")) {
            ResultSet rs = prepStatement.executeQuery();
            List<Account> result = new ArrayList<>();

            while (rs.next()) {
                result.add(resultSetToAccount(rs));
            }
            log.info("all accounts was successfully retrieved", result);
            return result;
        } catch (SQLException ex) {
            log.error("Listing of all acounts failed");
            throw new ServiceFailureException("Failed to retrieve all acount record!", ex);
        }
    }
}
