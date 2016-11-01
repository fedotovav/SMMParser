import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class main {
    public static void main(String[] args) {
        Options opts = new Options();

        OptionGroup groups_represent = new OptionGroup();
        OptionGroup group_list = new OptionGroup();

        groups_represent.addOption(new Option("g", "group_id", true, "Group id that members will be parsed"));
        groups_represent.addOption(new Option("l", "groups_list", true, "List of groups in text file that will be parsed"));

        opts.addOptionGroup(groups_represent);

        opts.addOption("o", "output_file", true, "groups frequency output file");
        opts.addOption("s", "sorted_groups_list", false, "Sorted by decrease of frequency list of groups in text file that will be parsed. For generate list use 'top_groups' application");
        opts.addOption("t", "threads_cnt", true, "Threads count. Increase this parameter for reduce parsing time");
        opts.addOption("c", "count", true, "Count of groups from top of list for parsing members");

        HelpFormatter formatter = new HelpFormatter();

        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = null;

        try {
            cmd = parser.parse(opts, args);
        } catch (ParseException e) {
            e.printStackTrace();

            return;
        }

        if (args.length == 0){
            System.err.println("No arguments was passed! Usage:");
            formatter.printHelp(" ", opts);

            return;
        }

        String group_id, groups_list_file, output_file;

        if (opts.hasOption("o"))
            output_file = cmd.getOptionValue("o");
        else{
            System.err.println("Output file name doesn't defined! Use \'-o\' option!");
            formatter.printHelp(" ", opts);

            return;
        }

        int threads_cnt = 1;

        if (cmd.hasOption("t")){
            threads_cnt = Integer.parseInt(cmd.getOptionValue("t"));
        }
        else {
            System.err.println("Threads number was not defined! Process was started in single process");

        }

        if (cmd.hasOption("g")) {
            group_id = cmd.getOptionValue("g");

            List<String> members = group_t.get_subscribers(group_id, threads_cnt);

            vk_api_t.export_list(output_file, members);

            return;
        }
        else if (cmd.hasOption("l")){
            groups_list_file = cmd.getOptionValue("l");
        }
        else {
            System.err.println("Group id or groups list file is not defined! Please set value for options \'-g\' or \'-l\'!");
            formatter.printHelp(" ", opts);

            return;
        }

        Map<String, Integer> groups_freq = new HashMap<String, Integer>();

        if (cmd.hasOption("c")){
            int groups_cnt = Integer.parseInt(cmd.getOptionValue("c"));

            groups_freq = vk_api_t.import_freq_map(groups_list_file, groups_cnt);
        } else{
            groups_freq = vk_api_t.import_freq_map(groups_list_file);

            System.err.println("Sorted list flag (\'-c\') was set, but doesn't set count of groups (\'-c\'). All groups from list will be parsed!");
        }

        Map<String, Integer> subscribers = group_t.get_subscribers(new ArrayList<String>(groups_freq.keySet()), threads_cnt);

        vk_api_t.export_freq_map(output_file, subscribers);
    }
}
