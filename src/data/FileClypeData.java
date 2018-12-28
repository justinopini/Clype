package data;

import java.io.*;
import java.util.Scanner;

/**
 * @author Justin Mekenye Opini
 * @author Hannah Defazio
 * @version 1.0
 * subclass for data package
 */

public class FileClypeData extends ClypeData implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 3L;
	private String fileName;
    private String fileContents;

    /**
     * @param userName For super constructor
     * @param fileName For super constructor
     * @param type For super constructor
     */
    public FileClypeData(String userName,String fileName,int type){
        super(userName,type);
        this.fileName = fileName;
        this.fileContents = null;
    }
    public FileClypeData(){
        super();
        this.fileName = null;
        this.fileContents = null;
    }

    /**
     * @param fileName String	representing	name	of	file
     */
    public void setFileName(String fileName){
        this.fileName = fileName;
    }
    public String getFileName(){
        return this.fileName;
    }
    @Override
    public String getData(){
        return fileContents;
    }
    /**
     *
     * @param key decryption key
     * @return decrypted filecontents
     */
    @Override
    public String getData(String key){
        return super.decrypt(this.fileContents,key);
    }

    /**
     *
     */
    public void readFileContents() {
        try {
            Scanner scanner = new Scanner( new File(this.fileName) );
            this.fileContents = scanner.useDelimiter("\\A").next();
            scanner.close();
        }catch (IOException ioe){
            System.err.println("Issue reading: \0".replace("\0",ioe.getMessage()));
        }

    }

    /**
     *
     * @param key encryyption key
     */
    public void readFileContents(String key) {
        try {
            Scanner scanner = new Scanner( new File(this.fileName) );
            String buffer = scanner.useDelimiter("\\A").next();
            scanner.close();
            this.fileContents = super.encrypt(buffer,key);
        }catch (IOException ioe){
            System.err.println("Issue reading: \0".replace("\0",ioe.getMessage()));
        }
    }
    /**
     *
     * @throws IOException if file does not open or error reading
     */
    public void writeFileContents() throws IOException{
        try {
            BufferedWriter out = new BufferedWriter( new FileWriter(this.fileName) );
            out.write(this.fileContents);
            out.close();
        }catch (IOException ioe){
            System.err.println("Issue writing: \0".replace("\0",ioe.getMessage()));
        }

    }
    /**
     *
     * @param key encryyption key
     * @throws Exception if file does not open or error reading
     */
    public void writeFileContents(String key) throws Exception{
        try {
            BufferedWriter out = new BufferedWriter( new FileWriter(this.fileName) );
            out.write(super.decrypt(this.fileContents,key));
            out.close();
        }catch (IOException ioe){
            System.err.println("Issue writing: \0".replace("\0",ioe.getMessage()));
        }
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
        if (this.fileName != null)
            result = 37*result + this.fileName.hashCode();
        if (this.fileContents != null)
            result = 37*result + this.fileContents.hashCode();
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
        FileClypeData otherMessage = (FileClypeData) other;
        return super.getDate() == otherMessage.getDate() && super.getType() == otherMessage.getType() && super.getUserName() == otherMessage.getUserName() && this.fileName == otherMessage.fileName && this.fileContents == otherMessage.fileContents;
    }

    /**
     * @return formartted description of class using its variables
     * Overrides super class
     */
    @Override
    public String toString(){
        return  "Username: " + super.getUserName() + "\nType: " + super.getType() + "\nDate: " + super.getDate() + "\nFilename: " + this.fileContents + "\nFile Contents: " + this.fileContents;
    }

}
