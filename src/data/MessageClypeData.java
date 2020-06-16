package data;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.List;

/** Represents a rich text message sent from one user to another via Clype. */
public class MessageClypeData extends ClypeData<String> {
  private final EncryptedData message;

  /** Instantiates a {@link MessageClypeData} instance of the provided user name and message. */
  public MessageClypeData(String sender, List<String> recipient, String message)
          throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
          BadPaddingException, ShortBufferException, InvalidKeyException, InvalidKeySpecException {
    super(sender, recipient, Type.MESSAGE);
    this.message = super.encrypt(message);
  }

  public MessageClypeData(String sender, String recipient, String message)
          throws NoSuchPaddingException, ShortBufferException, NoSuchAlgorithmException,
          IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
    this(sender, Collections.singletonList(recipient), message);
  }

  @Override
  public EncryptedData message() {
    return message;
  }

  @Override
  public String getData()
      throws IllegalBlockSizeException, BadPaddingException, ShortBufferException,
          InvalidKeyException {
    return decrypt(message);
  }

  @Override
  public byte[] toBytes(String data) {
    return data.getBytes();
  }

  @Override
  public String fromBytes(byte[] data) {
    return data.toString();
  }
}
