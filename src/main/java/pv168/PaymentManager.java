package pv168;

import java.util.List;

/**
 * Created by xvancik on 3/8/16.
 */
public interface PaymentManager {


    public void createPayment(Payment payment);

    public void deletePayment(Payment payment);

    public void updatePayment(Payment payment);

    public Payment findPaymentById(Long id);

    public List<Payment> findAllPayments();

}
