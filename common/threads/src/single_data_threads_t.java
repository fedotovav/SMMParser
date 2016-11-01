import java.util.ArrayList;
import java.util.List;

/**
 * Created by anton on 21.10.16.
 */
public class single_data_threads_t<TI, TR> {
    public single_data_threads_t() {
        threads_ = new ArrayList<thread_t<TI, TR>>();
    }

    /**
     * Process the data in multiple threads
     * @param fnc - function, that process a unit of data
     * @param data - processing data as list
     * @param threads_cnt - count of parallel threads
     * @return
     */
    public List<TR> run(functor_t fnc, TI data, int data_len, int max_data_per_thread, int threads_cnt ){
        if (threads_cnt > data_len)
            threads_cnt = data_len;

        int data_per_thread = data_len / threads_cnt,
            last_interval = data_per_thread % max_data_per_thread,
            intervals_per_thread = (data_per_thread / max_data_per_thread) + ((last_interval > 0) ? 1 : 0);

        List<TR> res = new ArrayList<TR>();

        List<List<interval_t>> intervals = new ArrayList<List<interval_t>>();

        for (int i = 0; i < threads_cnt; ++i){
            intervals.add(new ArrayList<interval_t>());

            for (int j = 0; j < intervals_per_thread - 2; ++j)
                intervals.get(i).add(new interval_t(i * data_per_thread + j * max_data_per_thread, i * data_per_thread + (j + 1) * max_data_per_thread));

            int j = intervals_per_thread - 1;

            if (last_interval == 0){
                intervals.get(i).add(new interval_t(i * data_per_thread + j * max_data_per_thread, i * data_per_thread + (j + 1) * max_data_per_thread));
            } else{
                intervals.get(i).add(new interval_t(i * data_per_thread + j * max_data_per_thread, i * data_per_thread + j * max_data_per_thread + last_interval));
            }

        }

        for (int i = 0; i < threads_cnt; ++i) {
            threads_.add(new thread_t(i, data, intervals.get(i), fnc));
            threads_.get(i).start();
        }

        wait_for_threads_stop();

        for (int i = 0; i < threads_cnt; ++i)
            res.addAll(threads_.get(i).get_result());

        return res;
    }

    public int progress(){ return progress_; }

    private List<thread_t<TI, TR>> threads_;
    //private TR res_; // i think that is unusable
    private int progress_;

    /**
     * Interface for using functor, that get single data
     * @param <TI> - input type (thread manager get TI)
     * @param <TR> - return type (thread manager return List<TR>)
     */
    public static interface functor_t<TI, TR>{
        public TR func(TI data, int from, int to);
    }

    private class thread_t<TI, TR> extends Thread {
        public thread_t(int id, TI data, List<interval_t> interval, functor_t functor ){
            id_ = id;
            functor_ = functor;
            is_ready_ = false;
            progress_ = 0;
            data_ = data;
            res_ = new ArrayList<TR>();
            interval_ = interval;
        }

        public void run() {
            for (interval_t item : interval_)
                res_.add((TR) functor_.func(data_, item.from, item.to));

            is_ready_ = true;
        }

        public List<TR> get_result(){ return res_; }
        public boolean is_ready(){ return is_ready_; }
        public int progress(){ return progress_; }

        private functor_t functor_;

        private List<TR> res_;

        private TI data_;

        private int id_;

        private List<interval_t> interval_;

        private boolean is_ready_;

        private int progress_;
    }

    private void wait_for_threads_stop(){
        boolean is_ready = false;

        int threads_cnt = threads_.size();

        while (!is_ready){
            int n = 0;

            progress_ = 0;

            for (int i = 0; i < threads_cnt; ++i){
                if (threads_.get(i).is_ready())
                    n++;

                progress_ += threads_.get(i).progress();
            }

            if (n == threads_cnt)
                is_ready = true;
        }
    }

    public class interval_t{
        public interval_t( int from, int to ){ this.from = from; this.to = to; }
        public int from, to;
    }
}
