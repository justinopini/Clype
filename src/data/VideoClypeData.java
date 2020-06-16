package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.List;

/** Represents a video sent from one user to another over Clype. */
public class VideoClypeData extends ClypeData<VideoClypeData.ClypeVideo> {
  private final EncryptedData video;

  /** Instantiates a {@link VideoClypeData} instance of the provided user name and message. */
  public VideoClypeData(String sender, List<String> recipients, String fileName)
      throws GeneralSecurityException, IOException {
    super(sender, recipients, Type.VIDEO);
    File file = new File(fileName);
    FileInputStream fin = new FileInputStream(file);
    byte[] video = new byte[(int) file.length()];
    fin.read(video);
    fin.close();
    this.video = super.encrypt(new ClypeVideo(video));
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
