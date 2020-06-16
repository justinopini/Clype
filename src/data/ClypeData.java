package data;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Date;
import java.util.List;

/**
 * Represents an abstract piece of data to be sent from a user on Clype.
 *
 * By default, the data stored should be encrypted in AES using the provided {@link #encrypt(Object) and {@link #decrypt(EncryptedData)} methods.
 *
 * @param <T> The core piece of data to be encrypted and decrypted.
 */
public abstract class ClypeData<T> implements Serializable {
  private final List<String> recipients;
  private final String sender;
  private final Type type;
  private final Date date;
  // Default encryption and decryption key to be used.
  private final Key key;
  private final transient Cipher cipher;

  /** Instantiates an instance of {@link ClypeData} of the provided user name and {@link Type}. */
  public ClypeData(String sender, List<String> recipients, Type type)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException {
    this.recipients = recipients;
    this.sender = sender;
    this.type = type;
    this.date = new Date();
    this.key = generateRandomKey(sender);
    this.cipher = Cipher.getInstance("AES");
  }

  private static Key generateRandomKey(String username)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];
    random.nextBytes(salt);
    KeySpec spec = new PBEKeySpec(username.toCharArray(), salt, 65536, 256); // AES-256
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
  }

  /** Gets the {@link Type} of this instance. */
  public Type getType() {
    return type;
  }

  /** Gets the user name for the user who is sending this {@link ClypeData}. */
  public String getSender() {
    return sender;
  }

  /** Gets the user name(s) for the users who are on the receiving end of this {@link ClypeData}. */
  public List<String> getRecipients() {
    return recipients;
  }

  /** Returns date when {@link ClypeData} object was created. */
  public Date getDate() {
    return date;
  }

  /** The encrypted message being sent by the user. */
  public abstract EncryptedData message();

  /** Gets the decrypted data being sent or received. */
  public T getData()
      throws IllegalBlockSizeException, BadPaddingException, ShortBufferException,
          InvalidKeyException {
    return decrypt(message());
  }

  /**
   * Translates the provided piece of data into a bytes array format for encryption and decryption.
   */
  public abstract byte[] toBytes(T data);

  /** Translates the provided piece of data from b */
  public abstract T fromBytes(byte[] data);

  /** Encrypts the provided piece of data using the encryption key using AES. */
  protected EncryptedData encrypt(T input)
      throws InvalidKeyException, ShortBufferException, IllegalBlockSizeException,
          BadPaddingException {
    byte[] inputBytes = toBytes(input);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    byte[] encrypted = new byte[cipher.getOutputSize(inputBytes.length)];
    int encryptedLen = cipher.update(inputBytes, 0, inputBytes.length, encrypted, 0);
    encryptedLen += cipher.doFinal(encrypted, encryptedLen);
    return new EncryptedData(encrypted, encryptedLen);
  }

  /** Decrypts the provided piece of data using the encryption key using AES. */
  protected T decrypt(EncryptedData encryptedInput)
      throws InvalidKeyException, ShortBufferException, BadPaddingException,
          IllegalBlockSizeException {
    cipher.init(Cipher.DECRYPT_MODE, key);
    byte[] decryptedOutput = new byte[cipher.getOutputSize(encryptedInput.len)];
    int decryptedLen =
        cipher.update(encryptedInput.data, 0, encryptedInput.len, decryptedOutput, 0);
    cipher.doFinal(decryptedOutput, decryptedLen);
    return fromBytes(decryptedOutput);
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 37 * result + getType().hashCode();
    result = 37 * result + getSender().hashCode();
    result = 37 * result + getDate().hashCode();
    result = 37 * result + message().hashCode();
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ClypeData)) {
      return false;
    }
    MessageClypeData otherMessage = (MessageClypeData) other;
    try {
      return getDate() == otherMessage.getDate()
          && getType() == otherMessage.getType()
          && getSender().equals(otherMessage.getSender())
          && getData().equals(otherMessage.getData());
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public String toString() {
    return String.format(
        "Username: %s%nType: %s%nDate: %s%nMessage: %s",
        getSender(), getType(), getDate(), message());
  }

  /**
   * The variable type represents the kind of data exchanged between the client and the server. It
   * is meant to enable the server to provide the client with targeted services.
   */
  public enum Type {
    // List all users currently active.
    LIST_USERS,
    // Log out, i.e. close this client's connection
    LOG_OUT,
    // Send a message.
    MESSAGE,
    // Send a file.
    FILE,
    // Send an image.
    IMAGE,
    // Send a video.
    VIDEO
  }

  /** Represents an encrypted piece of data. */
  public static class EncryptedData implements Serializable {
    private final byte[] data;
    private final int len;

    public EncryptedData(byte[] encrypted, int encryptedLen) {
      this.data = encrypted;
      this.len = encryptedLen;
    }
  }
}
