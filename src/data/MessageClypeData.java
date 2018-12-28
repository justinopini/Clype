package data;

import java.io.Serializable;

/**
 * @author Justin Mekenye Opini
 * @author Hannah Defazio
 * @version 1.0
 */

public class MessageClypeData extends ClypeData implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	private String message;

    /**
     * @param userName For super constructor
     * @param message String	representing	instant	message
     * @param type For super constructor
     */
    public MessageClypeData(String	userName,String	message,int	type){
        super(userName, type);
        this.message = message;
    }

    /**
     *
     * @param userName for super constructor
     * @param message instant message
     * @param key encryption key
     * @param type for super consntructor
     */
    public MessageClypeData(String	userName,String	message,String key,int type){
        super(userName, type);
        this.message = super.encrypt(message,key);
    }
    public MessageClypeData(){
        super();
        this.message = null;
    }
    @Override
    public Object getData(){
        return this.message;
    }

    /**
     *
     * @param key decryption key
     * @return decrypted message
     */
    @Override
    public String getData(String key){
        return super.decrypt(this.message,key);
    }

    /**
     * @return unique hashcode value
     * Overrides super class
     */
    @Override
    public int hashCode(){
        int result = 17;
        result = 37*result + super.getType();
        result = 37*result + super.getUserName().hashCode();
        result = 37*result + super.getDate().hashCode();
        result = 37*result + this.message.hashCode();
        return result;
    }

    /**
     * @param other Object to compare with
     * @return true or false if equal or nor
     * Overrides super class
     */
    @Override
    public boolean equals(Object other){
        if (!(other instanceof ClypeData)){
            return false;
        }
        MessageClypeData otherMessage = (MessageClypeData) other;
        return super.getDate() == otherMessage.getDate() && super.getType() == otherMessage.getType() && super.getUserName() == otherMessage.getUserName() && this.message == otherMessage.message;
    }

    /**
     * @return formartted description of class using its variables
     * Overrides super class
     */
    @Override
    public String toString(){
        return  "Username: " + super.getUserName() + "\nType: " + super.getType() + "\nDate: " + super.getDate() + "\nMessage: " + this.message;
    }
}
