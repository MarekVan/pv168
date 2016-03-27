package pv168;

import java.sql.Connection;
import java.util.List;

/**
 * Created by xvancik on 3/8/16.
 */
public interface PaymentManager {


    public void createPayment(Payment payment);
    
    public void createPayment(Payment payment, Connection con);

    public void deletePayment(Payment payment);
    
    public void deletePayment(Payment payment, Connection con);

    public void updatePayment(Payment payment);
    
    public void updatePayment(Payment payment, Connection con);

    public Payment findPaymentById(Long id);
    
    public Payment findPaymentById(Long id, Connection con);

    public List<Payment> findAllPayments();
    
    public List<Payment> findAllPayments(Connection con);

}
