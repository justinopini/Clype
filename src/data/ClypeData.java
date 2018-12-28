package data;
import java.util.Date;
import java.io.Serializable;

/**
 * @author Justin Mekenye Opini
 * @author Hannah Defazio
 * @version 1.0
 * superclass for data package
 */
public abstract class ClypeData implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userName;
    private final int type;
    private Date date;

    /**
     *
     * @param userName String	representing	name	of	client	user
     * @param type The	variable	type	represents	the	kind	of	data exchanged	between	the	client	and	the	server.	It	is
    meant	to	enable	the	server	to	provide	the	client	with	targeted	service.	The	variable	type	can
    take	on	the	following	values:
    0:	give	a	listing	of	all	users	connected	to	this	session
    1:	log	out,	i.e.,	close this	client’s	connection
    2:	send	a	file
    3:	send	a	message
    ClypeData	objects	of	type	0,	1,	and	3,	will	get	instantiated	as	MessageClypeData.	ClypeData
    objects	of	type	2	will	get	instantiated	as	FileClypeData.
     */
    public ClypeData(String userName,int type ){
        this.userName = userName;
        this.type = type;
        this.date = new Date();
    }

    /**
     *
     * @param type Represents	the	kind	of	data exchanged	between	the	client	and	the	server.
     */
    public ClypeData(int type ){
        this("Anon",type);
    }

    public ClypeData(){
        this(3);
    }

    /**
     *
     * @return type
     */
    public int getType() {
        return type;
    }

    /**
     *
     * @return userName
     */
    public String getUserName(){
        return userName;
    }

    /**
     *
     * @return date object	representing	date	when	ClypeData	object	was	created
     */
    public Date getDate(){
        return date;
    }

    public abstract Object getData();

    public abstract Object getData(String key);

    /**
     *
     * @param inputStringToEncrypt string to encrypt
     * @param key key to encrypt Vignere cipher
     * @return encrypeted key
     */
    protected	String	encrypt(	String	inputStringToEncrypt,	String	key ){
        int keylen = key.length();
        int strlen = inputStringToEncrypt.replace(" ","").length();
        int repeat = strlen/keylen;
        String reapeatedKey =  new String(new char[repeat]).replace("\0", key);//repeats key wholly
        int diff = strlen - reapeatedKey.length();
        String alphabetCaps = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String alphabetSmall = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder encrypted = new StringBuilder(inputStringToEncrypt);
        if (diff > 0)
            reapeatedKey += key.substring(0,diff);//repeats fraction of key
        for (int i = 0, j =0; i < inputStringToEncrypt.length(); i++ ){
             int ascii = (int) inputStringToEncrypt.charAt(i);
             if (ascii>=65 && ascii<=90){
                 ascii-=65;
                 ascii = (ascii+alphabetCaps.indexOf(reapeatedKey.charAt(j)))%26;
                 ascii+=65;
                 encrypted.setCharAt(i,(char)ascii);
                 j++;
             }else if (ascii>=97 && ascii<=122){
                 ascii-=97;
                 ascii = (ascii+alphabetSmall.indexOf(reapeatedKey.charAt(j)))%26;
                 ascii+=97;
                 encrypted.setCharAt(i,(char)ascii);
                 j++;
             }
        }
        return encrypted.toString();
    }

    /**
     *
     * @param inputStringToDecrypt string to decrepyt
     * @param key key to decrypt Vignere cipher
     * @return decrypeted key
     */
    protected	String	decrypt(	String	inputStringToDecrypt,	String key ){
        int keylen = key.length();
        int strlen = inputStringToDecrypt.replace(" ","").length();
        int repeat = strlen/keylen;
        String reapeatedKey =  new String(new char[repeat]).replace("\0", key);
        int diff = strlen - reapeatedKey.length();
        String alphabetCaps = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String alphabetSmall = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder decrypted = new StringBuilder(inputStringToDecrypt);
        if (diff > 0)
            reapeatedKey += key.substring(0,diff);
        for (int i = 0, j =0; i < inputStringToDecrypt.length(); i++ ){
            int ascii = (int) inputStringToDecrypt.charAt(i);
            if (ascii>=65 && ascii<=90){
                ascii-=65;
                ascii = ((ascii-alphabetCaps.indexOf(reapeatedKey.charAt(j)))+26)%26; // Convert to positive modulus from rem
                ascii+=65;
                decrypted.setCharAt(i,(char)ascii);
                j++;
            }else if (ascii>=97 && ascii<=122){
                ascii-=97;
                ascii = ((ascii-alphabetSmall.indexOf(reapeatedKey.charAt(j)))+26)%26;
                ascii+=97;
                decrypted.setCharAt(i,(char)ascii);
                j++;
            }
        }
        return decrypted.toString();
    }

}
