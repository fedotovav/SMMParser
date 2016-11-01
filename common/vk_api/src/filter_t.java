import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by anton on 31.10.16.
 */
public class filter_t {
    public filter_t(){
    }

    protected void parse( String input_file ){
        String content = null;

        try {
            content = new String(Files.readAllBytes(Paths.get(input_file)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONParser parser = new JSONParser();

        try {
            JSONObject main_obj = (JSONObject) (parser.parse(content));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
