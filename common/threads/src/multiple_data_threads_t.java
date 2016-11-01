import java.util.*;

public class multiple_data_threads_t<TI, TR> {

    public multiple_data_threads_t() {
        threads_ = new ArrayList<list_of_data_thread_t<TI, TR>>();
    }

    /**
     * Process the list of data in multiple threads
     * @param fnc - function, that process a unit of data
     * @param data - processing data as list
     * @param threads_cnt - count of parallel threads
     * @return
     */
    public List<TR> run( list_of_data_functor_t fnc, List<TI> data, int threads_cnt ){
        int data_len = data.size();

        if (threads_cnt > data_len)
            threads_cnt = data_len;

        int remainder = data_len % threads_cnt, step = data_len / threads_cnt;

        for (int i = 0; i < threads_cnt; ++i){
            int from = step * i, to = step * (i + 1);

            threads_.add(new list_of_data_thread_t(i, data.subList(from, to), fnc));

            threads_.get(i).start();
        }

        if (remainder > 0){
            int from = data_len - remainder, to = data_len;

            threads_.add(new list_of_data_thread_t(threads_cnt, data.subList(from, to), fnc));

            threads_.get(threads_cnt).start();
        }

        boolean is_ready = false;

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

        List<TR> res = new ArrayList<TR>();

        for (int i = 0; i < threads_cnt; ++i)
            res.add(threads_.get(i).get_result());

        return res;
    }

    public int progress(){ return progress_; }

    private List<list_of_data_thread_t<TI, TR>> threads_;
    //private TR res_; // i think that is unusable
    private int progress_;

    //////////////////////////////////////////////////////////////////////////////////////////
    // SUPPORT OBJECTS
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Interface for using functor, that get list of data
     * @param <TI> - input type (thread manager get List<TI>)
     * @param <TR> - return type (thread manager return List<TR>)
     */
    public static interface list_of_data_functor_t<TI, TR>{
        public TR func(TI data);
    }

    private class list_of_data_thread_t<TI, TR> extends Thread {
        public list_of_data_thread_t( int id, List<TI> data, list_of_data_functor_t functor ){
            id_ = id;
            functor_ = functor;
            is_ready_ = false;
            progress_ = 0;
            data_ = data;
            res_ = null;
        }

        public void run() {
            res_ = (TR) functor_.func(data_);

            is_ready_ = true;
        }

        public TR get_result(){ return res_; }
        public boolean is_ready(){ return is_ready_; }
        public int progress(){ return progress_; }

        private list_of_data_functor_t functor_;

        private TR res_;

        private List<TI> data_;

        private int id_;

        private boolean is_ready_;

        private int progress_;
    }
}
