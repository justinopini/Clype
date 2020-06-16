package data;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.List;

/** Represents an image sent from one user to another over Clype. */
public class PictureCypeData extends ClypeData<PictureCypeData.ClypeImage> {
  private final EncryptedData image;

  /** Instantiates a {@link PictureCypeData} instance of the provided user name and message. */
  public PictureCypeData(String sender, List<String> recipients, String fileName)
      throws GeneralSecurityException, IOException {
    super(sender, recipients, Type.IMAGE);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(ImageIO.read(new File(fileName)), "png", baos);
    image = super.encrypt(new ClypeImage(baos.toByteArray()));
    baos.close();
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
