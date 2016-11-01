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
public class group_t extends vk_api_t {
    /**
     * Parse group subscribers by group id
     * @param group_id - group id
     * @return
     * @throws Exception
     */
    public static ArrayList<String> get_subscribers( String group_id ) {
        int members_cnt = get_group_members_cnt(group_id);

        int max_groups_per_request = 200;

        ArrayList<String> res = new ArrayList<String>();

        if (members_cnt <= max_groups_per_request){
            try{
                res.addAll(group_members_request(group_id, 0, members_cnt));
            } catch (Exception err){
                System.err.println("Some error: " + err);
            }

            return res;
        }

        boolean need_incomplete_request = (members_cnt % max_groups_per_request) > 0;

        int request_cnt = members_cnt / max_groups_per_request + (need_incomplete_request ? 1 : 0);

        for (int i = 0; i < request_cnt - 1; ++i)
            res.addAll(group_members_request(group_id, i * max_groups_per_request, (i + 1) * max_groups_per_request));

        if (!need_incomplete_request){
            return res;
        }

        res.addAll(group_members_request(group_id, request_cnt * max_groups_per_request, members_cnt));

        return res;
    }

    /**
     * Parse subscribers from a list of groups
     * @param groups_ids - List<String> of groups id's
     * @return Map<String, Integer> - frequency map of groups subscribers
     * @throws Exception
     */
    public static Map<String, Integer> get_subscribers( List<String> groups_ids ) {
        Map<String, Integer> res = new HashMap<String, Integer>();

        for (int i = 0; i < groups_ids.size(); ++i){
            ArrayList<String> group_subscribers = new ArrayList<String>();

            try{
                group_subscribers.addAll(get_subscribers(groups_ids.get(i)));
            } catch(Exception err) {
                System.err.println("Some error");
            }

            update_freq_map(res, group_subscribers);
        }

        return res;
    }

    /*
    MULTIPLE THREADS METHODS
     */

    /**
     * Parse group subscribers by group id in multiple threads
     * @param group_id - group id
     * @param threads_cnt - threads count
     * @return
     * @throws Exception
     */
    public static List<String> get_subscribers( String group_id, int threads_cnt ) {
        int members_cnt = get_group_members_cnt(group_id);

        int max_groups_per_request = 200;

        List<String> res = new ArrayList<String>();

        if (members_cnt <= max_groups_per_request){
            try{
                res.addAll(group_members_request(group_id, 0, members_cnt));
            } catch (Exception err){
                System.err.println("Some error: " + err);
            }

            return res;
        }

        boolean need_incomplete_request = (members_cnt % max_groups_per_request) > 0;

        int request_cnt = members_cnt / max_groups_per_request + (need_incomplete_request ? 1 : 0);

        if (threads_cnt > request_cnt)
            threads_cnt = request_cnt;

        single_data_threads_t<String, List<String>> threads = new single_data_threads_t<String, List<String>>();

        List<List<String>> tmp_res = threads.run(new single_data_threads_t.functor_t<String, List<String>>(){
            public List<String> func( String data, int from, int to ) { return group_t.group_members_request(data, from, to); }
        }, group_id, members_cnt, max_groups_per_request, threads_cnt);

        for (List<String> item : tmp_res)
            res.addAll(item);

        if (!need_incomplete_request){
            return res;
        }

        res.addAll(group_members_request(group_id, request_cnt * max_groups_per_request, members_cnt));

        return res;
    }

    /**
     * Parse subscriptions from a list of users in multiple threads
     * @param groups_ids - List<String> of users id's
     * @return Map<String, Integer> - frequency map of groups subscribers
     * @throws Exception
     */
    public static Map<String, Integer> get_subscribers( List<String> groups_ids, int threads_cnt ) {
        Map<String, Integer> freq = new HashMap<String, Integer>();

        for (String group_id : groups_ids)
            update_freq_map(freq, get_subscribers(group_id, threads_cnt));

//        multiple_data_threads_t<String, Map<String, Integer>> threads = new multiple_data_threads_t<String,Map<String,Integer>>();

//        List<Map<String, Integer>> res = threads.run(new multiple_data_threads_t.list_of_data_functor_t<List<String>, Map<String, Integer>>() {
//            public Map<String, Integer> func(List<String> data) {
//                return vk_api_t.get_subscribers(data);
//            }
//        }, groups_ids, threads_cnt);
//
//        for (Map<String, Integer> cur_freq : res) unite_freq_map(freq, cur_freq);

        return freq;
    }

    /**
     * Method, that return count of group subscribers
     * @param group_id - group id
     * @return
     * @throws Exception
     */
    public static int get_group_members_cnt( String group_id ) {
        JSONParser parser = new JSONParser();

        JSONObject main_obj;

        try{
            String response = call_method("groups.getMembers", "group_id=" + group_id, "count=1");

            main_obj = (JSONObject) (parser.parse(response));
        }catch (Exception err){
            System.err.println("Some error: " + err);

            return 0;
        }

        int res = Integer.parseInt(((JSONObject)(main_obj.get("response"))).get("count").toString());

        return res;
    }

    public static ArrayList<String> group_members_request ( String group_id, int start_idx, int end_idx ) {
        JSONArray arr;

        ArrayList<String> res = new ArrayList<String>();

        try {
            String response = call_method("groups.getMembers", "group_id=" + group_id, "count=200", "offset=" + start_idx);

            JSONParser parser = new JSONParser();

            JSONObject main_obj = (JSONObject) (parser.parse(response));

            arr = (JSONArray) ((JSONObject) (main_obj.get("response"))).get("items");
        } catch (Exception err){
            System.err.println("Some error: " + err);

            return res;
        }

        for (int i = 0; i < (end_idx - start_idx); ++i)
            res.add(arr.get(i).toString());

        return res;
    }
}
