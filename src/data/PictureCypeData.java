package data;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.List;

/** Represents an image sent from one user to another over Clype. */
public class PictureCypeData extends ClypeData<PictureCypeData.ClypeImage> {
  private final EncryptedData image;

  /** Instantiates a {@link PictureCypeData} instance of the provided user name and message. */
  public PictureCypeData(String sender, List<String> recipients, String fileName)
          throws NoSuchAlgorithmException, NoSuchPaddingException, IOException,
          IllegalBlockSizeException, BadPaddingException, ShortBufferException,
          InvalidKeyException, InvalidKeySpecException {
    super(sender, recipients, Type.IMAGE);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(ImageIO.read(new File(fileName)), "png", baos);
    image = super.encrypt(new ClypeImage(baos.toByteArray()));
    baos.close();
  }

  public PictureCypeData(String sender, String recipients, String fileName)
          throws NoSuchPaddingException, ShortBufferException, NoSuchAlgorithmException, IOException,
          BadPaddingException, IllegalBlockSizeException, InvalidKeyException, InvalidKeySpecException {
    this(sender, Collections.singletonList(recipients), fileName);
  }

  @Override
  public EncryptedData message() {
    return image;
  }

  @Override
  public byte[] toBytes(ClypeImage data) {
    return data.image;
  }

  @Override
  public ClypeImage fromBytes(byte[] data) {
    return new ClypeImage(data);
  }

  /** Actual image data to be sent over Clype. */
  public class ClypeImage implements Serializable {
    public final byte[] image;

    public ClypeImage(byte[] image) {
      this.image = image;
    }
  }
}
