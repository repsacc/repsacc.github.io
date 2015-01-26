import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.lang.InterruptedException;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Casub {

    private static File next = null;

    private static void openChooser(File f) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Set english as language
                    Locale.setDefault(Locale.ENGLISH);
                    JFileChooser.setDefaultLocale(Locale.ENGLISH);
                    JFileChooser chooser = new JFileChooser(f);
                    chooser.setDialogTitle("Casub");

                    // Add file extension filter
                    FileFilter filter = new FileNameExtensionFilter("Video files", "mp4", "mkv", "avi");
                    chooser.addChoosableFileFilter(filter);
                    chooser.setFileFilter(filter);
                    chooser.setAcceptAllFileFilterUsed(false);
                    chooser.validate();
                    File file = null;

                    int ret = chooser.showDialog(null, "Choose file");
                    int choice = -1;
                    String absolutePath = "";
                    String filePath = "";

                    if (ret == JFileChooser.APPROVE_OPTION) {
                        file = chooser.getSelectedFile();
                        DownloadSubtitles sub = new DownloadSubtitles(file);
                        choice = JOptionPane.showConfirmDialog(null,
                                "The subtitle was successfully added!\nWould you like to go to the folder?", "Success", JOptionPane.YES_NO_OPTION);
                        absolutePath = file.getAbsolutePath();
                        // Path of the selected file
                        filePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator)) + File.separator;
                        if (choice == 0) {
                            try {
                                Desktop.getDesktop().open(new File(filePath));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        // Remove last file separator
                        String nextPath = filePath.substring(0, filePath.lastIndexOf(File.separator)-1);
                        // Set new path to parent directory of selected file
                        nextPath = nextPath.substring(0, nextPath.lastIndexOf(File.separator)) + File.separator;
                        next = new File(nextPath);
                        openChooser(next);
                    } else {
                        System.exit(0);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public static void main(String[] args) {
        openChooser(new File(File.separator + "Desktop"));
    }
}
