package com.reactive.live.ch05;

import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;

@Slf4j
public class Ob {
    public static void main(String[] args) {

        Iterable<Integer> iter = () ->
                new Iterator<>() {
                    int i = 0;
                    final static int MAX = 10;

                    @Override
                    public boolean hasNext() {
                        return i < MAX;
                    }
                    @Override
                    public Integer next() {
                        return ++i;
                    }
                };

        // for-each는 Iterator를 구현한 객체를 넣는다!
        for(Integer i : iter) {
            System.out.println(i);
        }

        // java 5 이전
        for(Iterator<Integer> it = iter.iterator(); it.hasNext();)
            System.out.println(it.next());
    }
}