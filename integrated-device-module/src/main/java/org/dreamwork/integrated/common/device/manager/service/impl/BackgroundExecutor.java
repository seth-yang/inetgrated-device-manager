package org.dreamwork.integrated.common.device.manager.service.impl;

import org.dreamwork.util.IDisposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.*;

@Resource
public class BackgroundExecutor implements IDisposable {
    private static final Runnable QUIT = () -> {};

    private final Logger logger = LoggerFactory.getLogger (BackgroundExecutor.class);

    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<> (32);
    private final Future<?> future;
    private transient boolean running = true;

    public BackgroundExecutor () {
        ExecutorService executor = Executors.newSingleThreadExecutor ();
        future = executor.submit (() -> {
            while (running) {
                try {
                    Runnable runner = queue.take ();
                    if (QUIT == runner) {
                        break;
                    }

                    try {
                        runner.run ();
                    } catch (Exception ex) {
                        logger.warn (ex.getMessage (), ex);
                    }
                } catch (InterruptedException ex) {
                    logger.warn (ex.getMessage (), ex);
                }
            }

            logger.info ("background executor stopped.");
        });
        executor.shutdown ();
    }

    /**
     * 销毁后台任务执行器
     */
    @PreDestroy
    public void dispose () {
        if (logger.isTraceEnabled ()) {
            logger.trace ("disposing background executor");
        }
        running = false;
        queue.clear ();     // 跳过未完成的任务
        queue.offer (QUIT);
        if (future != null) {
            future.cancel (true);
        }
    }

    public void post (Runnable runner) {
        if (!running) {
            throw new IllegalStateException ("cannot post task to a disposed executor!");
        }
        if (!queue.offer (runner)) {
            throw new IllegalStateException ("the task count of executor exceed the max limit.");
        }
    }
}