/**
 * @author Justin Mekenye Opini
 * @author Hannah Defazio
 * @version 2.0
 */

package data;
import java.io.*;

public class VideoClypeData extends ClypeData implements Serializable {
    private transient FileInputStream fin = null;;
    private byte[] video;
    private static final long serialVersionUID = 5L;


    public VideoClypeData(String fileName,String username,int type) throws IOException {
        super(username,type);
        File file = new File(fileName);
        try {
	        fin = new FileInputStream(file);
	        this.video= new byte[(int)file.length()];
	        fin.read(this.video);
        }catch (Exception e) {
        	
        }finally{
        	fin.close();
        }
    }

    @Override
    public byte[] getData() {
        return this.video;
    }
    @Override
    public byte[] getData(String Key) {
        return this.video;
    }

}
