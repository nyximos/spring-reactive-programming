package com.reactive.live.ch05;

import lombok.extern.slf4j.Slf4j;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

    /*
        Iterable ←→ Observable (duality)
        Iterable : Pull 다음값을 끌어옴
        Observable : 데이터/이벤트를 가지고 있는 소스에서 Push 해줌
        Observer : 관찰자. 뭔일있으면 정보달라함

        Observable(Source) -> Event/Data -> Observer
     */

@SuppressWarnings("deprecation")
@Slf4j
public class Ex02_Ob {

    // Source 데이터 만드는 쪽
    // Subject
    // Publisher
    static class InitObservable extends Observable implements Runnable {

        @Override
        public void run() {
            for (int i = 1; i <= 10; i++) {
                setChanged();          // 변화가 생겼을 때 호출
                notifyObservers(i);    // push 값을 던질 수 있음
                // int i = it.next();  // pull
            }
        }
    }

    // Data 데이터 받는 쪽
    // Observer
    // Subscriber
    public static void main(String[] args) {

        Observer ob = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                System.out.println(Thread.currentThread().getName() + " " + arg);
            }
        };

        InitObservable io = new InitObservable();
        io.addObserver(ob);     // Observer 등록

        // 별도의 쓰레드에서 비동기적으로 동작
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(io);

        System.out.println(Thread.currentThread().getName() + " " + "EXIT");
        es.shutdown();

    }
}