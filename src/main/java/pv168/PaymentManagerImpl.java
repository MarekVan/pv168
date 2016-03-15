package pv168;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xvancik on 3/8/16.
 */
public class PaymentManagerImpl implements PaymentManager {

    public void createPayment(Payment payment){}

    public void deletePayment(Payment payment){}

    public void updatePayment(Payment payment){}

    public Payment findPaymentById(Long id){
        return new Payment();
    }

    public List<Payment> findAllPayments(){
        return new ArrayList<Payment>();
    }

}
