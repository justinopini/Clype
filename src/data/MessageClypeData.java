package data;

import java.security.GeneralSecurityException;
import java.util.List;

/** Represents a rich text message sent from one user to another via Clype. */
public class MessageClypeData extends ClypeData<String> {
  private final EncryptedData message;

  /** Instantiates a {@link MessageClypeData} instance of the provided user name and message. */
  public MessageClypeData(String sender, List<String> recipient, String message)
      throws GeneralSecurityException {
    super(sender, recipient, Type.MESSAGE);
    this.message = super.encrypt(message);
  }

  /** Initialized a system messages by the server. */
  public MessageClypeData(Type type, String message) throws GeneralSecurityException {
    super(type);
    this.message = super.encrypt(message);
  }

  @Override
  public EncryptedData message() {
    return message;
  }

  @Override
  public byte[] toBytes(String data) {
    return data.getBytes();
  }

  @Override
  public String fromBytes(byte[] data) {
    return new String(data);
  }
}
