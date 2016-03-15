package pv168;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xvancik on 3/8/16.
 */
public class BankingManagerImpl implements BankingManager {

    public BankingManagerImpl(){}

    public void executePayment(Payment paayment){}
    public List<Payment> findAllIncomingPaymentsToAccount(Account account){
        return new ArrayList<Payment>();
    }
    public List<Payment> findOutgoingPaymentsToAccount(Account account){
        return new ArrayList<Payment>();
    }
}
