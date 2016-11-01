import org.apache.commons.cli.*;

import java.io.*;
import java.util.*;

public class main {
    public static Map<String, Integer> group_freq_concat( Map<String, Integer> group_freq1, Map<String, Integer> group_freq2 ){
        for (String key : group_freq2.keySet()){
            if (group_freq1.get(key) == null)
                group_freq1.put(key, 1);
            else {
                group_freq1.put(key, group_freq1.get(key) + group_freq2.get(key));
            }
        }

        return group_freq1;
    }

    public static void main(String[] args) throws Exception {
        Options opts = new Options();

        opts.addOption("g", true, "group id for parsing");
        opts.addOption("o", true, "groups frequency output file");
        opts.addOption("t", true, "threads count");

        HelpFormatter formatter = new HelpFormatter();

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(opts, args);

        if (args.length == 0){
            System.err.println("No arguments was passed! Usage:");
            formatter.printHelp(" ", opts);

            return;
        }

        int threads_cnt;

        String group_name, output_file;

        if (cmd.hasOption("g"))
            group_name = cmd.getOptionValue("g");
        else {
            System.err.println("Group id is not defined!");
            formatter.printHelp(" ", opts);

            return;
        }

        if (cmd.hasOption("o"))
            output_file = cmd.getOptionValue("o");
        else {
            System.err.println("Output file name doesn't defined!");
            formatter.printHelp(" ", opts);

            return;
        }

        if (cmd.hasOption("t"))
            threads_cnt = Integer.parseInt(cmd.getOptionValue("t"));
        else {
            System.err.println("Threads number was not defined! Process was started in single process");

            threads_cnt = 1;
        }

        System.out.println("Start parsing group users");

        long start = System.currentTimeMillis();

        List<String> group_users = group_t.get_subscribers(group_name);

        long loop = System.currentTimeMillis() - start;

        System.out.println("Group users was parsed (" + (loop  / 1000) + ") sec");

        Map<String, Integer> groups_freq = new HashMap<String, Integer>();

        System.out.println("Start parsing users groups");

        start = System.currentTimeMillis();

        groups_freq = user_t.get_subscriptions(group_users, threads_cnt);

        loop = System.currentTimeMillis() - start;

        System.out.println("\nUsers groups was parsed (" + (loop / 1000) + ") sec");

        System.out.println("Assembly top of groups");

        start = System.currentTimeMillis();

        List<Map.Entry<String, Integer>> entries =
                new ArrayList<Map.Entry<String, Integer>>(groups_freq.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b){
                return b.getValue().compareTo(a.getValue());
            }
        });

        Map<String, Integer> top_groups = new LinkedHashMap<String, Integer>();

        for (Map.Entry<String, Integer> entry : entries) {
            top_groups.put(entry.getKey(), entry.getValue());
        }

        loop = System.currentTimeMillis() - start;

        System.out.println("Done assembly top of groups (" + (loop / 1000) + ") sec");
        System.out.println("Output groups frequency");

        PrintWriter writer = new PrintWriter(output_file, "utf-8");

        vk_api_t.export_freq_map(output_file, top_groups);
    }
}
