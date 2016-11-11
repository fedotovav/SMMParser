import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anton on 10.11.16.
 */
public class filter_t {
    public filter_t( String input_file ){
        String content = null;

        try {
            content = new String(Files.readAllBytes(Paths.get(input_file)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONParser parser = new JSONParser();

        JSONObject main_obj;

        try {
            main_obj = (JSONObject) (parser.parse(content));
        } catch (ParseException e) {
            e.printStackTrace();

            return;
        }

        if (main_obj.containsKey("excluded-users")){
            JSONArray arr = (JSONArray) main_obj.get("excluded-users");

            excluded_users_.val = new ArrayList<String>();

            for (int i = 0; i < arr.size(); ++i)
                excluded_users_.val.add(arr.get(i).toString());

            excluded_users_.is_used_ = true;
        }

        if (main_obj.containsKey("bdate-range")){
            JSONArray arr = (JSONArray) main_obj.get("bdate-range");

            bdate_range_.val = new ArrayList<List<Integer>>();

            for (int i = 0; i < arr.size(); ++i)
                bdate_range_.val.add(utils_t.date_to_int(arr.get(i).toString()));

            bdate_range_.is_used_ = true;
        }
    }

    boolean user_is_not_excluded( String user_id ){
        if (excluded_users_.val.contains(user_id))
            return false;

        return true;
    }

    private class field_t<T>{
        field_t(){
            is_used_ = false;
        }

        public boolean is_used_;
        public T val;
    }

    private field_t<List<String>> excluded_users_;
    private field_t<List<List<Integer>>> bdate_range_;
}
