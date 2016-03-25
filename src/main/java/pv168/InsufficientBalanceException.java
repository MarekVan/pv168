package pv168;

/**
 *
 * @author Vašek & Vítek
 */
public class InsufficientBalanceException extends RuntimeException {
    
    public InsufficientBalanceException(String msg){
        super(msg);
    }
    
}
