/**
 * Subtitles downloaded from OpenSubtitles.org
 *
 * Author: Casper Renman
 */

import java.io.File;
import javax.swing.JFileChooser;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.FileWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import org.apache.commons.codec.binary.Base64;

public class DownloadSubtitles {

    private static final String OPSUB_SERVER = "http://api.opensubtitles.org/xml-rpc";
    private static XmlRpcClient server = null;
    private static String token = null;
    private static Object USERNAME = "";
    private static Object PASSWORD = "";
    private static Object LANGUAGE = "eng";
    private static final Object USERAGENT = "repsacc";

    public DownloadSubtitles(File file) {
        try {
            getSubtitles(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getSubtitles(File videoFile) throws Exception {

        // ------------- Login ------------- //
        String absolutePath = videoFile.getAbsolutePath();
        String nameOfFile = absolutePath.substring(absolutePath.lastIndexOf(File.separator)+1, absolutePath.length());
        String nameOfFileNoExt = nameOfFile.substring(0, nameOfFile.lastIndexOf("."));
        String filePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator)) + File.separator;

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(OPSUB_SERVER));
        XmlRpcClient server = new XmlRpcClient();
        server.setConfig(config);
        Object[] params = new Object[] {USERNAME, PASSWORD, LANGUAGE, USERAGENT};

        HashMap<?, ?> result = (HashMap<?, ?>) server.execute("LogIn", params);
        token = (String) result.get("token");

        // ------------- Hash ------------- //
        String movieHash = OpenSubtitlesHasher.computeHash(videoFile);
        Long videoLength = videoFile.length();
        String moviebytesize = videoLength.toString();

        // ------------- Prepare for searching for the subtitles ------------- //
        Map<String, Object> mapQuery = new HashMap<String, Object>();
        mapQuery.put("sublanguageid", "eng");
        mapQuery.put("moviehash", movieHash);
        mapQuery.put("moviebytesize", moviebytesize);

        // ------------- Do the search ------------- //
        params = new Object[]{token, new Object[]{mapQuery}};
        result = (HashMap<?, ?>) server.execute("SearchSubtitles", params);
        Object[] dataList = (Object[]) result.get("data");
        HashMap<?, ?> data = (HashMap<?, ?>) dataList[0];

        // ------------- Get subtitle id ------------- //
        Object subtitleID = data.get("IDSubtitleFile");

        // ------------- Download subtitles ------------- //
        Map listOfSubtitles = new HashMap();
        listOfSubtitles.put("data", subtitleID);
        params = new Object[] {token, listOfSubtitles};
        result = (HashMap<?, ?>) server.execute("DownloadSubtitles", params);

        // ------------- Extract subtitles ------------- //
        dataList = (Object[]) result.get("data");
        data = (HashMap<?, ?>) dataList[0];

        // ------------- Decode subtitles ------------- //
        byte[] gzip = Base64.decodeBase64((String) data.get("data"));
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(gzip));
        StringWriter sw = new StringWriter();

        IOUtils.copy(gis, sw);
        sw.flush();
        sw.close();
        gis.close();

        // ------------- Write subtitles to file and place file in correct dir ------------- //
        File file = new File(filePath + nameOfFileNoExt + ".srt");
        FileWriter fw = new FileWriter(file);

        // fw.write("1\n");
        // fw.write("00:00:01,000 --> 00:00:07,074\n");
        // fw.write("Subtitles added by Casper Renman :)\n");
        fw.write(sw.toString());

        System.out.println("Subtitle for " + nameOfFileNoExt + " downloaded.");

        // ------------- Log out ------------- //
        params = new Object[] {token};
        server.execute("LogOut", params);
    }
}
