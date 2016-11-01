import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anton on 31.10.16.
 */
public class user_t extends vk_api_t {
    /**
     * Method, that parse user groups
     * @param user_id - user id
     * @return ArrayList<String> - list of subscriptions
     * @throws Exception
     */
    public static ArrayList<String> get_subscriptions(String user_id ) throws Exception {
        int groups_cnt = get_user_groups_cnt(user_id);

        int max_groups_per_request = 200;

        ArrayList<String> res = new ArrayList<String>();

        if (groups_cnt <= max_groups_per_request){
            res.addAll(get_subscriptions(user_id, 0, groups_cnt));

            return res;
        }

        boolean need_incomplete_request = (groups_cnt % max_groups_per_request) > 0;

        int request_cnt = groups_cnt / max_groups_per_request + (need_incomplete_request ? 1 : 0);

        for (int i = 0; i < request_cnt - 1; ++i)
            res.addAll(get_subscriptions(user_id, i * max_groups_per_request, (i + 1) * max_groups_per_request));

        if (!need_incomplete_request){
            return res;
        }

        res.addAll(get_subscriptions(user_id, request_cnt * max_groups_per_request, groups_cnt));

        return res;
    }

    /**
     * Parse subscriptions from a list of users
     * @param users_ids - List<String> of users id's
     * @return Map<String, Integer> - frequency map of users subscriptions
     * @throws Exception
     */
    public static Map<String, Integer> get_subscriptions(List<String> users_ids ) {
        Map<String, Integer> res = new HashMap<String, Integer>();

        for (int i = 0; i < users_ids.size(); ++i){
            ArrayList<String> user_subscriptions = new ArrayList<String>();

            try{
                user_subscriptions.addAll(get_subscriptions(users_ids.get(i)));
            } catch(Exception err) {
                System.err.println("Some error");
            }

            update_freq_map(res, user_subscriptions);
        }

        return res;
    }

    /**
     * Parse subscriptions from a list of users in multiple threads
     * @param users_ids - List<String> of users id's
     * @return Map<String, Integer> - frequency map of users subscriptions
     * @throws Exception
     */
    public static Map<String, Integer> get_subscriptions(List<String> users_ids, int threads_cnt ) throws Exception {
        Map<String, Integer> freq = new HashMap<String, Integer>();

        multiple_data_threads_t<String, Map<String, Integer>> threads = new multiple_data_threads_t<String,Map<String,Integer>>();

        List<Map<String, Integer>> res =  threads.run(new multiple_data_threads_t.list_of_data_functor_t<List<String>, Map<String, Integer>>() {
            public Map<String, Integer> func(List<String> data) {
                return user_t.get_subscriptions(data);
            }
        }, users_ids, threads_cnt);

        for (Map<String, Integer> cur_freq : res){
            unite_freq_map(freq, cur_freq);
        }

        return freq;
    }

    /**
     * Method, that parse subscriptions of single user
     * @param user_id - user id
     * @param start_idx - start index of subscriptions
     * @param end_idx - end index of subscriptions
     * @return ArrayList<String> - list of subscriptions
     * @throws Exception
     */
    private static ArrayList<String> get_subscriptions(String user_id, int start_idx, int end_idx ) throws Exception{
        ArrayList<String> res = new ArrayList<String>();

        String response = call_method("users.getSubscriptions", "user_id=" + user_id, "count=200", "offset=" + start_idx);

        JSONParser parser = new JSONParser();

        JSONObject main_obj = (JSONObject) (parser.parse(response));

        JSONArray arr = (JSONArray) ((JSONObject)((JSONObject)(main_obj.get("response"))).get("groups")).get("items");

        for (int i = 0; i < arr.size(); ++i)
            res.add(arr.get(i).toString());

        return res;
    }

    public static int get_user_groups_cnt( String user_id ) throws Exception {
        String response = call_method("users.getSubscriptions", "user_id=" + user_id, "count=1");

        JSONParser parser = new JSONParser();

        JSONObject main_obj = (JSONObject) (parser.parse(response));

        int res = Integer.parseInt(((JSONObject)((JSONObject)(main_obj.get("response"))).get("groups")).get("count").toString());

        return res;
    }
}
