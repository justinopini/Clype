package data;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.List;

/** Represents a video sent from one user to another over Clype. */
public class VideoClypeData extends ClypeData<VideoClypeData.ClypeVideo> {
  private final EncryptedData video;

  /** Instantiates a {@link VideoClypeData} instance of the provided user name and message. */
  public VideoClypeData(String sender, List<String> recipients, String fileName)
          throws IOException, NoSuchAlgorithmException, NoSuchPaddingException,
          IllegalBlockSizeException, BadPaddingException, ShortBufferException,
          InvalidKeyException, InvalidKeySpecException {
    super(sender, recipients, Type.VIDEO);
    File file = new File(fileName);
    FileInputStream fin = new FileInputStream(file);
    byte[] video = new byte[(int) file.length()];
    fin.read(video);
    fin.close();
    this.video = super.encrypt(new ClypeVideo(video));
  }

  public VideoClypeData(String sender, String recipient, String fileName)
          throws NoSuchPaddingException, ShortBufferException, NoSuchAlgorithmException, IOException,
          BadPaddingException, IllegalBlockSizeException, InvalidKeyException, InvalidKeySpecException {
    this(sender, Collections.singletonList(recipient), fileName);
  }

  @Override
  public EncryptedData message() {
    return video;
  }

  @Override
  public byte[] toBytes(ClypeVideo data) {
    return data.video;
  }

  @Override
  public ClypeVideo fromBytes(byte[] data) {
    return new ClypeVideo(data);
  }

  /** Actual video data to be sent over Clype. */
  public class ClypeVideo implements Serializable {
    public final byte[] video;

    public ClypeVideo(byte[] video) {
      this.video = video;
    }
  }
}
