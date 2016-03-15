package pv168;


import java.util.List;

/**
 * Created by xvancik on 3/8/16.
 */
public interface BankingManager {

    public void executePayment(Payment paayment);
    public List<Payment> findAllIncomingPaymentsToAccount(Account account);
    public List<Payment> findOutgoingPaymentsToAccount(Account account);
}
