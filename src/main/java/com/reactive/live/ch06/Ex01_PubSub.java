package com.reactive.live.ch06;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class Ex01_PubSub {
    /*
        Transform
        - Publisher → Data → Subscriber
        - Publisher → [Data1] → Operator → [Data2] → Op2 → [Data3] → Subscriber
        1. map (d1 → f → d2)
        - pub → [Data1] → mapPub → [Data2] → logSub
                        ← subscribe(logSub)
                        → onSubscribe(s)
                        → onNext
                        → onNext
                        → onComplete
     */

    public static void main(String[] args) {
        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a + 1).limit(10).collect(Collectors.toList()));
        Publisher<Integer> mapPub = mapPub(pub, (Function<Integer, Integer>) s -> s * 10);
        Publisher<Integer> mapPub2 = mapPub(mapPub, (Function<Integer, Integer>) s -> -s);
        mapPub2.subscribe(logSub());
    }

    private static Publisher<Integer> mapPub(Publisher<Integer> pub, Function<Integer, Integer> f) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                pub.subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        sub.onSubscribe(s);
                    }

                    @Override
                    public void onNext(Integer i) {
                        sub.onNext(f.apply(i));
                    }

                    @Override
                    public void onError(Throwable t) {
                        sub.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        sub.onComplete();
                    }
                });
            }
        };
    }

    private static Subscriber<Integer> logSub() {
        return new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                log.debug("onSubscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer item) {
                log.debug("onNext:{}", item);
            }

            @Override
            public void onError(Throwable throwable) {
                log.debug("onError:{}", throwable);
            }

            @Override
            public void onComplete() {
                log.debug("onComplete");
            }
        };
    }

    private static Publisher<Integer> iterPub(List<Integer> iter) {
        return new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> sub) { // 구독이 일어나는 action
                sub.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) { // 한번에 데이터 얼만큼??
                        try {
                            iter.forEach(s -> sub.onNext(s));
                            sub.onComplete();
                        } catch (Throwable t) {
                            sub.onError(t);
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };
    }
}