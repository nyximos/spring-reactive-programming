package com.reactive.live.ch05;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Flow.Publisher;
import static java.util.concurrent.Flow.Subscriber;
import static java.util.concurrent.Flow.Subscription;

/*
    1. Complete 어떻게 시킬것인가??
    2. Error Exception 발생했을 땐 어떻게 하나요 ? → 비동기일때 예외 처리가 패턴에 녹아있지 않다
*/
@Slf4j
public class Ex03_PubSub {
    public static void main(String[] args) throws InterruptedException {
        // Publisher <- Observable
        // Subscriber <- Observer

        Iterable<Integer> iter = Arrays.asList(1,2,3,4,5);
        ExecutorService es = Executors.newCachedThreadPool();

        Publisher p = new Publisher() {
            @Override
            public void subscribe(Subscriber subscriber) {
                Iterator<Integer> it = iter.iterator();

                subscriber.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        es.execute(() -> {
                            int i = 0;
                            while (i++ < n ) {
                                if (it.hasNext()) {
                                    subscriber.onNext(it.next());
                                } else {
                                    subscriber.onComplete();
                                    break;
                                }
                            }
                        });
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };

        Subscriber<Integer> s = new Subscriber<Integer>() {
            Subscription subscription;
            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println(Thread.currentThread().getName() + " onSubscribe");
                //subscription.request(Long.MAX_VALUE);
                this.subscription = subscription;
                this.subscription.request(1);
            }
            @Override
            public void onNext(Integer item) {
                System.out.println(Thread.currentThread().getName() + "onNext "+item);
                subscription.request(1);
            }
            @Override
            public void onError(Throwable throwable) {
                System.out.println("onError" + throwable.getMessage());
            }
            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        };
        p.subscribe(s);

        es.awaitTermination(10, TimeUnit.HOURS);
        es.shutdown();
    }

}