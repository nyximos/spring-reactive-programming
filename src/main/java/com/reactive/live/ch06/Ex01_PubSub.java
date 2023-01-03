package com.reactive.live.ch06;

import lombok.extern.slf4j.Slf4j;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
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
//        Publisher<String> mapPub = mapPub(pub, s -> "["+ s +"]");
//        Publisher<List> mapPub = mapPub(pub, s -> Collections.singletonList(s));
//        Publisher<Integer> mapPub2 = mapPub(mapPub, s -> -s);
//        Publisher<Integer> sumPub = sumPup(pub);
//        Publisher<Integer> reducePub = reducePub(pub, 0, (BiFunction<Integer, Integer, Integer>)(a, b) -> a+b);
        Publisher<StringBuilder> reducePub = reducePub(pub, new StringBuilder(), (a, b) -> a.append(b + ","));
        reducePub.subscribe(logSub());
    }

    private static <T,R> Publisher<R> reducePub(Publisher<T> pub, R init, BiFunction<R, T, R> bf) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> sub) {
                pub.subscribe(new DelegateSub<T, R>(sub){
                    R result = init;

                    @Override
                    public void onNext(T i) {
                        result = bf.apply(result, i);
                    }

                    @Override
                    public void onComplete() {
                        sub.onNext(result);
                        sub.onComplete();
                    }
                });
            }
        };
    }

    /*
    private static Publisher<Integer> sumPup(Publisher<Integer> pub) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                pub.subscribe(new DelegateSub(sub){
                    int sum = 0;

                    @Override
                    public void onNext(Integer i) {
                        sum += i;
                    }

                    @Override
                    public void onComplete() {
                        super.onNext(sum);
                        sub.onComplete();
                    }
                });
            }
        };
    }
     */

    // T -> R
    private static <T, R> Publisher<R> mapPub(Publisher<T> pub, Function<T, R> f) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> sub) {
                pub.subscribe(new DelegateSub<T,R>(sub){
                    @Override
                    public void onNext(T i) {
                        sub.onNext(f.apply(i));
                    }
                });
            }
        };
    }

    private static <T> Subscriber<T> logSub() {
        return new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription s) {
                log.debug("onSubscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T item) {
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