package data;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Scanner;

/** Represents a rich-text file to be sent from one user to another via Clype. */
public class FileClypeData extends ClypeData<FileClypeData.ClypeFile> {
  private final String fileName;
  EncryptedData file;

  /** Instantiates a {@link FileClypeData} using the provided user name anf file name. */
  public FileClypeData(String sender, List<String> recipients, String fileName) throws GeneralSecurityException, FileNotFoundException {
    super(sender, recipients, Type.FILE);
    this.fileName = fileName;
    this.file = super.encrypt(new ClypeFile(fileName));
  }

  @Override
  public EncryptedData message() {
    return file;
  }

  @Override
  public byte[] toBytes(ClypeFile data) {
    return data.fileContents.getBytes();
  }

  @Override
  public ClypeFile fromBytes(byte[] data) {
    return new ClypeFile(fileName, new String(data));
  }

  /** Represents the actual data to be sent from one user to another user. */
  public static class ClypeFile implements Serializable {
    private final String fileName;
    private final String fileContents;

    private ClypeFile(String fileName) throws FileNotFoundException {
      this.fileName = fileName;
      this.fileContents = readFileContents(fileName);
    }

    private ClypeFile(String fileName, String fileContents) {
      this.fileName = fileName;
      this.fileContents = fileContents;
    }

    private static String readFileContents(String fileName) throws FileNotFoundException {
      Scanner scanner = new Scanner(new File(fileName));
      String buffer = scanner.useDelimiter("\\A").next();
      scanner.close();
      return buffer;
    }

    /** Writes the contents of this file out to its respective file name. */
    public void writeFileContents() throws IOException {
      BufferedWriter out = new BufferedWriter(new FileWriter(this.fileName));
      out.write(this.fileContents);
      out.close();
    }

    public String getFileName() {
      return fileName;
    }
  }
}
