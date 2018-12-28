/**
 * @author Justin Mekenye Opini
 * @author Hannah Defazio
 * @version 2.0
 */

package data;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class PictureCypeData extends ClypeData implements Serializable {
    private transient ByteArrayOutputStream baos = null;
    private byte[] image;
    private static final long serialVersionUID = 4L;


    public PictureCypeData(String fileName,String username,int type) {
        super(username,type);
        try {
            baos = new ByteArrayOutputStream();
            ImageIO.write(ImageIO.read(new File(fileName)), "png", baos);
            image = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public byte[] getData() {
        return this.image;
    }
    @Override
    public byte[] getData(String Key) {
        return this.image;
    }
}
