import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by anton on 20.09.16.
 */
public class vk_api_t {
    public static String call_method( String method, String ... params ) throws Exception {
        String request = "https://api.vk.com/method/" + method + "?";

        for (int i = 0; i < params.length; ++i)
            request += params[i] + "&";

        request += "v=5.53";

        URLConnection connection = new URL(request).openConnection();

        InputStream is = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(is);
        char[] buffer = new char[256];
        int rc;

        StringBuilder sb = new StringBuilder();

        while ((rc = reader.read(buffer)) != -1)
            sb.append(buffer, 0, rc);

        reader.close();

        return sb.toString();
    }

    public static void export_freq_map( String file_name,  Map<String, Integer> freq_map ){
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(file_name, "utf-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        for (String unit : freq_map.keySet())
            writer.write(unit + ":" + freq_map.get(unit) + "\n");
    }

    public static void export_list( String file_name,  List<String> item ){
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(file_name, "utf-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        for (String unit : item)
            writer.write(unit + "\n");
    }

    public static Map<String, Integer> import_freq_map( String file_name ){
        List<String> lines = new ArrayList<>();

        BufferedReader crunchifyBufferReader = null;

        try {
            crunchifyBufferReader = Files.newBufferedReader(Paths.get(file_name));

        } catch (IOException e) {
            e.printStackTrace();
        }

        lines = crunchifyBufferReader.lines().collect(Collectors.toList());

        Map<String, Integer> res = new HashMap<String, Integer>();

        for (String line : lines){
            int double_dots_index = line.indexOf(":");

            String item = line.substring(0, double_dots_index);
            int frequency = Integer.parseInt(line.substring(double_dots_index + 1));

            res.put(item, frequency);
        }

        return res;
    }

    public static Map<String, Integer> import_freq_map( String file_name, int count_of_first_items ){
        Map<String, Integer> res = new HashMap<String, Integer>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file_name));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < count_of_first_items; ++i){
            String line = null;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            int double_dots_index = line.indexOf(":");

            String item = line.substring(0, double_dots_index);
            int frequency = Integer.parseInt(line.substring(double_dots_index + 1));

            res.put(item, frequency);
        }

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    public static Map<String, Integer> update_freq_map( Map<String, Integer> origin, List<String> appendix ){
        for (int j = 0; j < appendix.size(); ++j){
            String group_id = appendix.get(j);

            if (origin.get(group_id) == null)
                origin.put(group_id, 1);
            else {
                origin.put(group_id, origin.get(group_id) + 1);
            }
        }

        return origin;
    }

    public static Map<String, Integer> unite_freq_map ( Map<String, Integer> origin, Map<String, Integer> appendix ){
        for (String key : appendix.keySet()){
            if (origin.get(key) == null)
                origin.put(key, 1);
            else {
                origin.put(key, origin.get(key) + appendix.get(key));
            }
        }

        return origin;
    }

}
