package bbd.sina.source; /**
 * Created by rex on 17-3-6.
 */

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zx.soft.utils.config.ConfigUtil;
import zx.soft.utils.log.LogbackUtil;
import zx.soft.utils.threads.ApplyThreadPool;

public class FindSource {
    private static ThreadPoolExecutor pool = ApplyThreadPool.getThreadPoolExector(128);
    private static Logger logger = LoggerFactory.getLogger(FindSource.class);
    private HttpUtil http = new HttpUtil();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> pool.shutdown()));
    }

    private static long start=0;
    private static double end=0;
    private static int interval=1000;
    private static int thread = 0;

    public FindSource() {
        Properties props = ConfigUtil.getProps("source.properties");
        start = Long.parseLong(props.getProperty("start"));
        end=Double.parseDouble(props.getProperty("end"));
        interval=Integer.parseInt(props.getProperty("interval"));
        thread=Integer.parseInt(props.getProperty("thread"));
    }

    public static void main(String[] args) throws InterruptedException {

        long time=System.currentTimeMillis();
        FindSource findSource = new FindSource();
        logger.info("Start From {} TO {}",start,end);
        CountDownLatch countDownLatch = new CountDownLatch(thread);
        for(;start<end;start+=interval){
            findSource.getSource(start,countDownLatch);
        }

        countDownLatch.await();

        logger.info("the interval time is {}",System.currentTimeMillis()-time);
    }

    private void getSource(final long temp, final CountDownLatch countDownLatch) {

        pool.execute(() -> {

            long source = temp;
            for (; source < temp + interval; source++){
                try {
                    String url = "https://api.weibo.com/2/statuses/user_timeline/ids.json?source=" + source + "&uid=2871675252&page=1&count=1";
                    String return_value = http.get(url);
                    if (return_value.contains("marks")) {
                        logger.info("{}", String.valueOf(source));
                    }
                }catch (Exception e){
                    logger.info(LogbackUtil.expection2Str(e));
                }
            }
            countDownLatch.countDown();
        }
        );
    }
}
