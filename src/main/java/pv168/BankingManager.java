package pv168;


import java.util.List;

/**
 * Created by xvancik on 3/8/16.
 */
public interface BankingManager {

    /**
     * 
     * Reduces balance of Account payment.fromAcc by payment.balance and adds it 
     * to payment.toAcc.balance, updates all information in the database. 
     * 
     * 
     * @param payment 
     * Parameter payment must be payment instance with all set atribuites, in other case
     * IllegalArgumentException is thrown (passing null causes also IllegalArgumentException).
     * Also payment.fromAcc.balance must be greater or equal to payment.amount, otherwise 
     * ??!![IllegalArgumentException (or should we define new exception?)]!!?? is thrown.
     */
    
    public void executePayment(Payment payment);
    
    /**
     * Retrieves all payments from database which have specified account as its target. 
     * 
     * 
     * @param account
     * Account with all specified atributes.
     * 
     * @return 
     * Returns List of payments where specified account occures as payment.toAcc.
     * Returns empty List if no such payment exists.
     * 
     */
    
    public List<Payment> findAllIncomingPaymentsToAccount(Account account);
    
    /**
     *Retrieves all payments which have specified account as the sender.
     * @param account
     * Account with all specified atributes.
     * 
     * @return 
     * Returns List of payments where specified account occures as payment.fromAcc.
     * Returns empty List if no such payment exists.
     */
    
    public List<Payment> findOutgoingPaymentsToAccount(Account account);
}
