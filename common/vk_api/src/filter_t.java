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

        user_ = new user_t(main_obj);
    }

    private class field_t<T>{
        field_t(){
            is_used_ = false;
        }

        public boolean is_used_;
        public T val;
    }

    user_t user_;

    public class user_t{
        user_t( JSONObject obj ){
            if (obj.containsKey("excluded-users")){
                JSONArray arr = (JSONArray) obj.get("excluded-users");

                user_.excluded_users_.val = new ArrayList<String>();

                for (int i = 0; i < arr.size(); ++i)
                    user_.excluded_users_.val.add(arr.get(i).toString());

                user_.excluded_users_.is_used_ = true;
            }

            if (obj.containsKey("bdate-range")){
                JSONArray arr = (JSONArray) obj.get("bdate-range");

                user_.bdate_range_.val = new ArrayList<List<Integer>>();

                for (int i = 0; i < arr.size(); ++i)
                    user_.bdate_range_.val.add(utils_t.date_to_int(arr.get(i).toString()));

                user_.bdate_range_.is_used_ = true;
            }
        }

        List<String> apply( List<String> users ){
            for (String user: users){
                String response = vk_api_t.call_method("users.get", "fields=bdate");

                JSONParser parser = new JSONParser();

                JSONObject main_obj = null;
                try {
                    main_obj = (JSONObject) (parser.parse(response));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // check birthday
                JSONObject arr = (JSONObject)(((JSONObject)(main_obj.get("response"))).get("bdate"));
            }
        }

        private field_t<List<String>> excluded_users_;
        private field_t<List<List<Integer>>> bdate_range_;
    }
}
