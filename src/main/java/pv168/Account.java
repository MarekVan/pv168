package pv168;


import java.math.BigDecimal;

/**
 * Created by xvancik on 3/8/16.
 */
public class Account {

    
    private Long id;
    
    private String owner;
    
    private BigDecimal balance;
    
    
    public Account(){   
    }
    
    


    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    
    public void setId(Long id){
        this.id = id;
    }

    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * @return the balance
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * @param balance the balance to set
     */
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    /**
     * 
     * @param o
     * Object which is tested for equality
     * @return 
     * Returns true, if and only if o and calling object are the same type and all
     * of their atributes are equal
     */
    @Override
    public boolean equals(Object o){
    if(this == o) return true;
    if(!(o instanceof Account)) return false;
    
    Account account = (Account) o;
    
    if(getId() != null ? !getId().equals(account.getId()) : account.getId() != null) return false;
    if(!account.owner.equals(this.owner))return false;
    if(!account.balance.equals(this.balance))return false;
    
    return true;
    }
    
    
    /**
     * 
     * @return
     * Hashcode int value based on atributes values.
     */
    
    @Override
    public int hashCode(){
     
    int result = (getId() != null) ? getId().hashCode() : 0;
    result = 31 * result + ((owner != null) ? owner.hashCode() : 0);
    result = 31 * result + ((balance != null) ? balance.hashCode() : 0);
    
    return result;
    }
    
    
    /**
     * 
     * @return
     * String representation of this object.
     */
    @Override
    public String toString(){
    String str = "";
    
    if(id != null){
    str += "ID: " + id + "\n";
    }
    else{
    str += "ID: not assigned yet\n";
    }
    
    if(owner != null){
    str += "Owner: " + owner+ "\n";
    }
    else{
    str += "Owner: not assigned yet\n";
    }
    
    if(balance != null){
    str += "Balance: " + balance + "\n";
    }
    else{
    str += "Balance: not assigned yet \n";
    }
    
    return str;
    }
    
    
    /**
     * 
     * @param owner
     * Value to be set for account.owner
     * @param balance
     * Value to be set for account.balance
     * @return 
     * Returns new Account with already set parameters.
     */
    
    public static Account newAccount(String owner, BigDecimal balance){
        Account account = new Account();
        account.setOwner(owner);
        account.setBalance(balance);
        
        return account;
    }
}
